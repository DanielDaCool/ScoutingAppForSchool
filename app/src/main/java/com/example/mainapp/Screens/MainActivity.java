package com.example.mainapp.Screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Home panel
    private TextView textViewWelcome, tvTeamCount, tvGamesCount, tvCurrentEvent;
    private Button   btnForms, btnPrediction;

    // Profile panel
    private TextView tvProfileName, tvProfileEmail, tvProfileRole, tvCurrentDistrict;
    private Button   buttonLogout, btnChangeDistrict;
    private LinearLayout layoutAssignments;
    private RecyclerView rvAssignments;
    private TextView     tvNoAssignments;
    private AssignmentAdapter     assignmentAdapter;
    private ArrayList<Assignment> assignmentList = new ArrayList<>();

    // Navigation
    private ScrollView   panelHome;
    private LinearLayout panelProfile;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;

    private Context          context;
    private SharedPrefHelper prefs;

    // Track which district the Firebase listener is registered for
    // to avoid registering multiple listeners on each onResume
    private boolean firebaseListenerRegistered = false;
    private EVENTS  registeredListenerDistrict = null;

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
        prefs   = SharedPrefHelper.getInstance(context);

        init();
        setupBottomNav();
        setupButtons();
        setupProfilePanel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!prefs.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        loadDashboardStats();
        refreshCacheInBackground();

        // Re-register Firebase listener only if district changed
        EVENTS currentDistrict = prefs.getCurrentDistrict();
        boolean districtChanged = currentDistrict != null
                && !currentDistrict.equals(registeredListenerDistrict);

        if (districtChanged) {
            registeredListenerDistrict  = currentDistrict;
            firebaseListenerRegistered  = false;
            tvCurrentEvent.setText(currentDistrict.toString());
            tvCurrentDistrict.setText("מחוז: " + currentDistrict.toString());
        }

        if (!firebaseListenerRegistered) {
            firebaseListenerRegistered = true;
            listenToFirebaseAssignments();
        }
    }

    // ==================== DASHBOARD ====================

    private void loadDashboardStats() {
        AppCache cache = AppCache.getInstance();
        tvTeamCount.setText(cache.getTeamCount()  > 0 ? String.valueOf(cache.getTeamCount())  : "—");
        tvGamesCount.setText(cache.getTotalGames() > 0 ? String.valueOf(cache.getTotalGames()) : "—");
    }

    private void refreshCacheInBackground() {
        DataHelper.getInstance().readAllTeamStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override public void onSuccess(ArrayList<TeamStats> data) {
                AppCache.getInstance().setAllTeamStats(data);
                int totalGames = 0;
                for (TeamStats t : data) if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                AppCache.getInstance().setTotalGames(totalGames);
                DataHelper.getInstance().countTeams(count -> {
                    AppCache.getInstance().setTeamCount(count);
                    runOnUiThread(() -> loadDashboardStats());
                });
            }
            @Override public void onFailure(String error) {}
        });
    }

    // ==================== PROFILE PANEL ====================

    private void setupProfilePanel() {
        tvProfileName.setText(prefs.getFullName());
        tvProfileEmail.setText(prefs.getEmail());
        tvProfileRole.setText("👤 Scouter");

        EVENTS district = prefs.getCurrentDistrict();
        tvCurrentDistrict.setText("תחרות: " + (district != null ? district.toString() : "—"));

        setupAssignmentList();
    }

    private void showChangeDistrictDialog() {
        EVENTS[] events     = EVENTS.values();
        String[] eventNames = new String[events.length];
        for (int i = 0; i < events.length; i++) eventNames[i] = events[i].toString();

        new AlertDialog.Builder(context)
                .setTitle("שנה מחוז")
                .setItems(eventNames, (dialog, which) -> {
                    prefs.saveDistrict(events[which]);
                    // Reset listener so it re-registers for the new district
                    firebaseListenerRegistered = false;
                    registeredListenerDistrict = null;
                    // Reload LoadingScreen to fetch data for new district
                    startActivity(new Intent(context, LoadingScreen.class));
                    finish();
                })
                .show();
    }

    // ==================== ASSIGNMENTS ====================

    private void setupAssignmentList() {
        assignmentAdapter = new AssignmentAdapter(assignmentList);
        rvAssignments.setLayoutManager(new LinearLayoutManager(context));
        rvAssignments.setAdapter(assignmentAdapter);

        assignmentAdapter.setOnAssignmentClickListener(assignment -> {
            // Remove from UI immediately
            assignmentAdapter.removeByKey(assignment.getKey());
            tvNoAssignments.setVisibility(assignmentList.isEmpty() ? View.VISIBLE : View.GONE);

            // Open form pre-filled, passing district via Intent
            EVENTS district = prefs.getCurrentDistrict();
            Intent intent   = new Intent(context, FormsActivity.class);
            intent.putExtra("teamNumber",    assignment.getTeamNumber());
            intent.putExtra("gameNumber",    assignment.getGameNumber());
            intent.putExtra("assignmentKey", assignment.getKey());
            if (district != null) intent.putExtra("districtKey", district.getEventKey());
            startActivity(intent);
        });
    }

    private void listenToFirebaseAssignments() {
        EVENTS district = prefs.getCurrentDistrict();
        if (district == null) return;

        DataHelper.getInstance().listenToPendingAssignments(
                prefs.getUserId(), district,
                new DataHelper.DataCallback<ArrayList<Assignment>>() {
                    @Override public void onSuccess(ArrayList<Assignment> data) {
                        runOnUiThread(() -> {
                            assignmentList.clear();
                            assignmentList.addAll(data);
                            assignmentAdapter.notifyDataSetChanged();
                            tvNoAssignments.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                    @Override public void onFailure(String error) {}
                }
        );
    }

    // ==================== NAVIGATION ====================

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)    showPanel(panelHome);
            else if (id == R.id.nav_games)   startActivity(new Intent(context, GamesList.class));
            else if (id == R.id.nav_stats)   startActivity(new Intent(context, TeamStatsActivity.class));
            else if (id == R.id.nav_profile) showPanel(panelProfile);
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
                startActivity(new Intent(context, FormsActivity.class)));
        btnPrediction.setOnClickListener(v ->
                startActivity(new Intent(context, PredictionScreen.class)));
        btnChangeDistrict.setOnClickListener(v -> showChangeDistrictDialog());
        buttonLogout.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setMessage("אתה בטוח שאתה רוצה להתנתק?")
                        .setPositiveButton("כן", (d, w) -> {
                            DataHelper.getInstance().logoutUser();
                            prefs.logout();
                            startActivity(new Intent(context, LoginScreen.class));
                            finish();
                        })
                        .setNegativeButton("לא, חזור", null)
                        .show()
        );
    }

    // ==================== INIT ====================

    private void init() {
        textViewWelcome   = findViewById(R.id.textViewWelcome);
        tvTeamCount       = findViewById(R.id.tvTeamCount);
        tvGamesCount      = findViewById(R.id.tvGamesCount);
        tvCurrentEvent    = findViewById(R.id.tvCurrentDistrict);
        btnForms          = findViewById(R.id.btnForms);
        btnPrediction     = findViewById(R.id.btnPrediction);
        panelHome         = findViewById(R.id.panelHome);
        panelProfile      = findViewById(R.id.panelProfile);
        tvProfileName     = findViewById(R.id.tvProfileName);
        tvProfileEmail    = findViewById(R.id.tvProfileEmail);
        tvProfileRole     = findViewById(R.id.tvProfileRole);
        tvCurrentDistrict = findViewById(R.id.tvCurrentDistrict);
        btnChangeDistrict = findViewById(R.id.btnChangeDistrict);
        buttonLogout      = findViewById(R.id.buttonLogout);
        layoutAssignments = findViewById(R.id.layoutAssignments);
        rvAssignments     = findViewById(R.id.rvAssignments);
        tvNoAssignments   = findViewById(R.id.tvNoAssignments);
        bottomNav         = findViewById(R.id.bottomNav);

        textViewWelcome.setText("שלום, " + prefs.getFirstName());
        EVENTS district = prefs.getCurrentDistrict();
        tvCurrentEvent.setText(district != null ? district.toString() : "—");
        registeredListenerDistrict = district;
    }
}