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
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.UserRole;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.SharedPrefHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Home panel
    private TextView textViewWelcome, tvTeamCount, tvGamesCount;
    private Button btnForms, btnPrediction;

    // Profile panel
    private TextView tvProfileName, tvProfileEmail, tvProfileRole;
    private Button buttonLogout, btnAdminPanel;
    private LinearLayout layoutAssignments;   // shown for SCOUTER
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
    }

    // ==================== DASHBOARD ====================

    private void loadDashboardStats() {
        AppCache cache = AppCache.getInstance();
        long teamCount  = cache.getTeamCount();
        int totalGames  = cache.getTotalGames();
        tvTeamCount.setText(teamCount  > 0 ? String.valueOf(teamCount)  : "—");
        tvGamesCount.setText(totalGames > 0 ? String.valueOf(totalGames) : "—");
    }

    // ==================== PROFILE PANEL ====================

    private void setupProfilePanel() {
        tvProfileName.setText(prefs.getFullName());
        tvProfileEmail.setText(prefs.getEmail());

        if (prefs.isAdmin()) {
            // ADMIN — show admin panel button, hide assignment list
            tvProfileRole.setText("🔑 Admin");
            tvProfileRole.setTextColor(0xFFC084FC);
            btnAdminPanel.setVisibility(View.VISIBLE);
            layoutAssignments.setVisibility(View.GONE);
        } else {
            // SCOUTER — show assignment list, hide admin button
            tvProfileRole.setText("👤 Scouter");
            tvProfileRole.setTextColor(0xFF7C6F8E);
            btnAdminPanel.setVisibility(View.GONE);
            layoutAssignments.setVisibility(View.VISIBLE);
            setupAssignmentList();
        }
    }

    private void setupAssignmentList() {
        for(int i = 0; i < 10; i++){
            assignmentList.add(new Assignment(50 + i, 5635));
        }
        assignmentAdapter = new AssignmentAdapter(assignmentList);
        rvAssignments.setLayoutManager(new LinearLayoutManager(context));
        rvAssignments.setAdapter(assignmentAdapter);

        // Tap assignment → open FormsActivity pre-filled
        assignmentAdapter.setOnAssignmentClickListener(assignment -> {
            Intent intent = new Intent(context, FormsActivity.class);
            intent.putExtra("teamNumber", assignment.getTeamNumber());
            intent.putExtra("gameNumber", assignment.getGameNumber());
            intent.putExtra("assignmentKey", assignment.getKey());
            startActivity(intent);
        });

        // Live listener — updates automatically when admin assigns tasks
        DataHelper.getInstance().listenToPendingAssignments(
                prefs.getUserId(),
                new DataHelper.DataCallback<ArrayList<Assignment>>() {
                    @Override
                    public void onSuccess(ArrayList<Assignment> data) {
                        runOnUiThread(() -> {
                            assignmentList.clear();
                            assignmentList.addAll(data);
                            assignmentAdapter.notifyDataSetChanged();
                            tvNoAssignments.setVisibility(
                                    data.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                    @Override
                    public void onFailure(String error) {}
                }
        );
    }

    // ==================== NAVIGATION ====================

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showPanel(panelHome);
            } else if (id == R.id.nav_games) {
                if (InternetUtils.isInternetConnectedWithAlert(context))
                    startActivity(new Intent(context, GamesList.class));
            } else if (id == R.id.nav_stats) {
                if (InternetUtils.isInternetConnectedWithAlert(context))
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
        btnForms.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, FormsActivity.class));
        });

        btnPrediction.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, PredictionScreen.class));
        });

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
        // Home panel
        textViewWelcome = findViewById(R.id.textViewWelcome);
        tvTeamCount     = findViewById(R.id.tvTeamCount);
        tvGamesCount    = findViewById(R.id.tvGamesCount);
        btnForms        = findViewById(R.id.btnForms);
        btnPrediction   = findViewById(R.id.btnPrediction);
        panelHome       = findViewById(R.id.panelHome);

        // Profile panel
        panelProfile      = findViewById(R.id.panelProfile);
        tvProfileName     = findViewById(R.id.tvProfileName);
        tvProfileEmail    = findViewById(R.id.tvProfileEmail);
        tvProfileRole     = findViewById(R.id.tvProfileRole);
        buttonLogout      = findViewById(R.id.buttonLogout);
        btnAdminPanel     = findViewById(R.id.btnAdminPanel);
        layoutAssignments = findViewById(R.id.layoutAssignments);
        rvAssignments     = findViewById(R.id.rvAssignments);
        tvNoAssignments   = findViewById(R.id.tvNoAssignments);
        bottomNav         = findViewById(R.id.bottomNav);

        textViewWelcome.setText("שלום, " + prefs.getFirstName());
    }
}