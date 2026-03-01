package com.example.mainapp.Screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.Screens.Predictions.PredictionScreen;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private TextView textViewWelcome, tvTeamCount, tvGamesCount;
    private TextView tvProfileName, tvProfileEmail;
    private ScrollView panelHome;
    private LinearLayout panelProfile;
    private Button btnForms, btnPredictAuto, btnPredictManual, buttonLogout;
    private BottomNavigationView bottomNav;
    private Context context;

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
        init();
        loadDashboardStats();
        setupBottomNav();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SharedPrefHelper.getInstance(this).isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        }
    }

    private void loadDashboardStats() {
        // Load team count
        DataHelper.getInstance().countTeams(count -> {
            runOnUiThread(() -> tvTeamCount.setText(String.valueOf(count)));
        });

        // Load total games scouted across all teams

        DataHelper.getInstance().readAllTeamStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> data) {
                AtomicInteger totalGames = new AtomicInteger(0);
                if (data != null) {
                    for (com.example.mainapp.Utils.TeamUtils.TeamStats t : data) {
                        if (t.getAllGames() != null) {
                            totalGames.addAndGet(t.getAllGames().size());
                        }
                    }
                }
                runOnUiThread(() -> tvGamesCount.setText(String.valueOf(totalGames.get())));

            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> tvGamesCount.setText("0"));
            }
        });
    }

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

    private void setupButtons() {
        btnForms.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, FormsActivity.class));
        });

        btnPredictAuto.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context)) {
                Intent intent = new Intent(context, PredictionScreen.class);
                intent.putExtra("mode", "auto");
                startActivity(intent);
            }
        });

        btnPredictManual.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context)) {
                Intent intent = new Intent(context, PredictionScreen.class);
                intent.putExtra("mode", "manual");
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setMessage("אתה בטוח שאתה רוצה להתנתק?")
                        .setPositiveButton("כן", (dialog, which) -> {
                            DataHelper.getInstance().logoutUser();
                            SharedPrefHelper.getInstance(context).logout();
                            startActivity(new Intent(context, LoginScreen.class));
                            finish();
                        })
                        .setNegativeButton("לא, חזור", (dialog, which) -> dialog.cancel())
                        .show()
        );
    }

    private void showPanel(View panel) {
        panelHome.setVisibility(View.GONE);
        panelProfile.setVisibility(View.GONE);
        panel.setVisibility(View.VISIBLE);
    }

    private void init() {
        textViewWelcome  = findViewById(R.id.textViewWelcome);
        tvTeamCount      = findViewById(R.id.tvTeamCount);
        tvGamesCount     = findViewById(R.id.tvGamesCount);
        tvProfileName    = findViewById(R.id.tvProfileName);
        tvProfileEmail   = findViewById(R.id.tvProfileEmail);
        panelHome        = findViewById(R.id.panelHome);
        panelProfile     = findViewById(R.id.panelProfile);
        btnForms         = findViewById(R.id.btnForms);
        btnPredictAuto   = findViewById(R.id.btnPredictAuto);
        btnPredictManual = findViewById(R.id.btnPredictManual);
        buttonLogout     = findViewById(R.id.buttonLogout);
        bottomNav        = findViewById(R.id.bottomNav);

        SharedPrefHelper prefs = SharedPrefHelper.getInstance(context);
        String firstName = prefs.getFirstName();
        String email    = prefs.getEmail();

        textViewWelcome.setText("שלום, " + firstName);
        tvProfileName.setText(firstName);
        tvProfileEmail.setText(email);
    }
}