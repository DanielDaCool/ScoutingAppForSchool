package com.example.mainapp.Screens;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.AssignmentAdapter;
import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.Screens.Predictions.PredictionScreen;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.InternetReciver;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.LocalDatabase;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Home panel
    private TextView textViewWelcome, tvTeamCount, tvGamesCount, tvCurrentEvent;
    private Button btnForms, btnPrediction;

    // Profile panel
    private TextView tvProfileName, tvProfileEmail, tvProfileRole;
    private Button buttonLogout, btnAdminPanel;
    private LinearLayout layoutAssignments;
    private RecyclerView rvAssignments;
    private TextView tvNoAssignments;
    private AssignmentAdapter assignmentAdapter;
    private ArrayList<Assignment> assignmentList = new ArrayList<>();

    // Navigation
    private ScrollView panelHome;
    private LinearLayout panelProfile;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;

    private Context context;
    private SharedPrefHelper prefs;


    private InternetReciver internetReciver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SharedPrefHelper.getInstance(this).isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        context = this;
        prefs = SharedPrefHelper.getInstance(context);

        init();
        setupBottomNav();
        setupButtons();
        setupProfilePanel();
    }


    @Override
    protected void onStart() {
        super.onStart();
        internetReciver = new InternetReciver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetReciver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(internetReciver);
        } catch (IllegalArgumentException ignored) {}
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!prefs.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        // Always show cached stats immediately
        loadDashboardStats();

        // Silently refresh in background if online
        if (InternetUtils.isInternetConnected(this)) {
            refreshCacheInBackground();
        }
    }

    // ==================== DASHBOARD ====================

    private void loadDashboardStats() {
        AppCache cache = AppCache.getInstance();
        long teamCount = cache.getTeamCount();
        int totalGames = cache.getTotalGames();
        tvTeamCount.setText(teamCount > 0 ? String.valueOf(teamCount) : "—");
        tvGamesCount.setText(totalGames > 0 ? String.valueOf(totalGames) : "—");
    }

    private void refreshCacheInBackground() {
        DataHelper.getInstance().readAllTeamStats(
                new DataHelper.DataCallback<ArrayList<TeamStats>>() {
                    @Override
                    public void onSuccess(ArrayList<TeamStats> data) {
                        AppCache.getInstance().setAllTeamStats(data);
                        int totalGames = 0;
                        for (TeamStats t : data) {
                            if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                        }
                        AppCache.getInstance().setTotalGames(totalGames);
                        DataHelper.getInstance().countTeams(count -> {
                            AppCache.getInstance().setTeamCount(count);
                            runOnUiThread(() -> loadDashboardStats());
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> loadDashboardStats());
                    }
                }
        );
    }

    // ==================== PROFILE PANEL ====================

    private void setupProfilePanel() {
        tvProfileName.setText(prefs.getFullName());
        tvProfileEmail.setText(prefs.getEmail());

        if (prefs.isAdmin()) {
            tvProfileRole.setText("🔑 Admin");
            tvProfileRole.setTextColor(0xFFC084FC);
            btnAdminPanel.setVisibility(View.VISIBLE);
            layoutAssignments.setVisibility(View.GONE);
        } else {
            tvProfileRole.setText("👤 Scouter");
            tvProfileRole.setTextColor(0xFF7C6F8E);
            btnAdminPanel.setVisibility(View.GONE);
            layoutAssignments.setVisibility(View.VISIBLE);
            setupAssignmentList();
        }
    }

    private void setupAssignmentList() {
        assignmentAdapter = new AssignmentAdapter(assignmentList);
        rvAssignments.setLayoutManager(new LinearLayoutManager(context));
        rvAssignments.setAdapter(assignmentAdapter);

        assignmentAdapter.setOnAssignmentClickListener(assignment -> {
            // Always delete from SQLite immediately — UI updates same way online or offline
            new Thread(() ->
                    LocalDatabase.getInstance(context).deleteAssignment(assignment.getKey())
            ).start();

            // Remove from UI list immediately
            assignmentAdapter.removeByKey(assignment.getKey());
            tvNoAssignments.setVisibility(
                    assignmentList.isEmpty() ? View.VISIBLE : View.GONE);

            // Open form pre-filled
            Intent intent = new Intent(context, FormsActivity.class);
            intent.putExtra("teamNumber", assignment.getTeamNumber());
            intent.putExtra("gameNumber", assignment.getGameNumber());
            intent.putExtra("assignmentKey", assignment.getKey());
            startActivity(intent);
        });

        // Step 1 — immediately show from SQLite (instant, no waiting)
        loadAssignmentsFromSQLite();

        // Step 2 — if online, also listen to Firebase for live updates
        // This will refresh the list if admin adds/removes assignments
        if (InternetUtils.isInternetConnected(context)) {
            listenToFirebaseAssignments();
        }
    }

    /**
     * Loads assignments from local SQLite.
     * Always called first — instant, works offline.
     */
    private void loadAssignmentsFromSQLite() {
        new Thread(() -> {
            List<Assignment> local = LocalDatabase.getInstance(context).getLocalAssignments();
            runOnUiThread(() -> updateAssignmentUI(new ArrayList<>(local)));
        }).start();
    }

    private void listenToFirebaseAssignments() {
        Log.d("ASSIGNMENTS", "Starting listener for: " + prefs.getUserId());
        DataHelper.getInstance().listenToPendingAssignments(
                prefs.getUserId(),
                new DataHelper.DataCallback<ArrayList<Assignment>>() {
                    @Override
                    public void onSuccess(ArrayList<Assignment> data) {
                        Log.d("ASSIGNMENTS", "Firebase fired — " + data.size() + " assignments");
                        new Thread(() ->
                                LocalDatabase.getInstance(context).replaceAllAssignments(data)
                        ).start();
                        runOnUiThread(() -> updateAssignmentUI(data));
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("ASSIGNMENTS", "Failed: " + error);
                        loadAssignmentsFromSQLite();
                    }
                }
        );
    }

    private void updateAssignmentUI(ArrayList<Assignment> data) {
        assignmentList.clear();
        assignmentList.addAll(data);
        assignmentAdapter.notifyDataSetChanged();
        tvNoAssignments.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ==================== NAVIGATION ====================

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showPanel(panelHome);
            } else if (id == R.id.nav_games) {
                startActivity(new Intent(context, GamesList.class));
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(context, TeamStatsActivity.class));
            } else if (id == R.id.nav_profile) {
                showPanel(panelProfile);
            }
            return true;
        });
    }

    private void showPanel(View panel) {
        panelHome.setVisibility(View.GONE);
        panelProfile.setVisibility(View.GONE);
        panel.setVisibility(View.VISIBLE);
    }

    // ==================== BUTTONS ====================

    private void setupButtons() {
        btnForms.setOnClickListener(v ->
                startActivity(new Intent(context, FormsActivity.class))
        );

        btnPrediction.setOnClickListener(v ->
                startActivity(new Intent(context, PredictionScreen.class))
        );

        btnAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(context, AdminPanelActivity.class))
        );

        buttonLogout.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setMessage("אתה בטוח שאתה רוצה להתנתק?")
                        .setPositiveButton("כן", (dialog, which) -> {
                            DataHelper.getInstance().logoutUser();
                            prefs.logout();
                            startActivity(new Intent(context, LoginScreen.class));
                            finish();
                        })
                        .setNegativeButton("לא, חזור", (dialog, which) -> dialog.cancel())
                        .show()
        );
    }

    // ==================== INIT ====================

    private void init() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        tvTeamCount = findViewById(R.id.tvTeamCount);
        tvGamesCount = findViewById(R.id.tvGamesCount);
        tvCurrentEvent = findViewById(R.id.tvCurrentEvent);
        btnForms = findViewById(R.id.btnForms);
        btnPrediction = findViewById(R.id.btnPrediction);
        panelHome = findViewById(R.id.panelHome);
        panelProfile = findViewById(R.id.panelProfile);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        buttonLogout = findViewById(R.id.buttonLogout);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        layoutAssignments = findViewById(R.id.layoutAssignments);
        rvAssignments = findViewById(R.id.rvAssignments);
        tvNoAssignments = findViewById(R.id.tvNoAssignments);
        bottomNav = findViewById(R.id.bottomNav);

        textViewWelcome.setText("שלום, " + prefs.getFirstName());
        tvCurrentEvent.setText(Constants.CURRENT_EVENT_ON_APP.toString());
    }
}