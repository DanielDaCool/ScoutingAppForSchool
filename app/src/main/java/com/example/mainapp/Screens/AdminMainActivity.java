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

import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.Screens.Predictions.PredictionScreen;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity {

    private TextView textViewWelcome, tvTeamCount, tvGamesCount;
    private Button   btnPrediction, btnAdminPanel;

    private TextView tvProfileName, tvProfileEmail, tvProfileRole;
    private Button   buttonLogout;

    private ScrollView   panelHome;
    private LinearLayout panelProfile;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;

    private Context          context;
    private SharedPrefHelper prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        checkAndHandleNonAuthenticatedUser();
        setContentView(R.layout.activity_admin_main);

        prefs   = SharedPrefHelper.getInstance(context);

        init();
        setupBottomNav();
        setupButtons();
        setupProfilePanel();
    }
    private void checkAndHandleNonAuthenticatedUser(){
        if (!SharedPrefHelper.getInstance(context).isUserLoggedIn()) {
            startActivity(new Intent(context, LoginScreen.class));
            finish();
            return;
        }
        if(!SharedPrefHelper.getInstance(context).isAdmin()){
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
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
    }

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

    private void setupProfilePanel() {
        tvProfileName.setText(prefs.getFullName());
        tvProfileEmail.setText(prefs.getEmail());
        tvProfileRole.setText("🔑 Admin");
        tvProfileRole.setTextColor(0xFFC084FC);
    }

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

    private void setupButtons() {
        btnPrediction.setOnClickListener(v ->
                startActivity(new Intent(context, PredictionScreen.class)));
        btnAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(context, AdminPanelActivity.class)));
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

    private void init() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        tvTeamCount     = findViewById(R.id.tvTeamCount);
        tvGamesCount    = findViewById(R.id.tvGamesCount);
        btnPrediction   = findViewById(R.id.btnPrediction);
        btnAdminPanel   = findViewById(R.id.btnAdminPanel);
        panelHome       = findViewById(R.id.panelHome);
        panelProfile    = findViewById(R.id.panelProfile);
        tvProfileName   = findViewById(R.id.tvProfileName);
        tvProfileEmail  = findViewById(R.id.tvProfileEmail);
        tvProfileRole   = findViewById(R.id.tvProfileRole);
        buttonLogout    = findViewById(R.id.buttonLogout);
        bottomNav       = findViewById(R.id.bottomNav);

        textViewWelcome.setText("שלום, " + prefs.getFirstName());
    }
}