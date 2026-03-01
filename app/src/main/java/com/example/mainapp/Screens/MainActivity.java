package com.example.mainapp.Screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.Screens.Predictions.PredictionScreen;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Button buttonGamesList, buttonForms, buttonStats, predictButton, buttonLogout;
    private TextView textViewWelcome, tvProfileName, tvProfileEmail;
    private LinearLayout panelHome, panelProfile;
    private BottomNavigationView bottomNav;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to login if not logged in
        if (!SharedPrefHelper.getInstance(this).isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        init();
        setupBottomNav();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check login state when returning to this screen
        if (!SharedPrefHelper.getInstance(this).isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        }
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showPanel(panelHome);
                return true;
            } else if (id == R.id.nav_games) {
                if (InternetUtils.isInternetConnectedWithAlert(context))
                    startActivity(new Intent(context, GamesList.class));
                return true;
            } else if (id == R.id.nav_forms) {
                if (InternetUtils.isInternetConnectedWithAlert(context))
                    startActivity(new Intent(context, FormsActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                if (InternetUtils.isInternetConnectedWithAlert(context))
                    startActivity(new Intent(context, TeamStatsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                showPanel(panelProfile);
                return true;
            }
            return false;
        });

        // Start on home tab
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupButtons() {
        // Home panel quick-action buttons
        buttonGamesList.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, GamesList.class));
        });
        buttonForms.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, FormsActivity.class));
        });
        buttonStats.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, TeamStatsActivity.class));
        });
        predictButton.setOnClickListener(v -> {
            if (InternetUtils.isInternetConnectedWithAlert(context))
                startActivity(new Intent(context, PredictionScreen.class));
        });

        // Logout button on profile panel
        buttonLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setMessage("אתה בטוח שאתה רוצה להתנתק?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        DataHelper.getInstance().logoutUser();
                        SharedPrefHelper.getInstance(context).logout();
                        startActivity(new Intent(context, LoginScreen.class));
                        finish();
                    })
                    .setNegativeButton("לא, חזור", (dialog, which) -> dialog.cancel())
                    .show();
        });
    }

    private void showPanel(LinearLayout panel) {
        panelHome.setVisibility(android.view.View.GONE);
        panelProfile.setVisibility(android.view.View.GONE);
        panel.setVisibility(android.view.View.VISIBLE);
    }

    private void init() {
        context = MainActivity.this;

        bottomNav       = findViewById(R.id.bottomNav);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        tvProfileName   = findViewById(R.id.tvProfileName);
        tvProfileEmail  = findViewById(R.id.tvProfileEmail);
        panelHome       = findViewById(R.id.panelHome);
        panelProfile    = findViewById(R.id.panelProfile);
        buttonGamesList = findViewById(R.id.buttonGamesList);
        buttonForms     = findViewById(R.id.buttonForms);
        buttonStats     = findViewById(R.id.buttonStats);
        predictButton   = findViewById(R.id.predictButton);
        buttonLogout    = findViewById(R.id.buttonLogout);

        // Populate user info from SharedPrefs
        String fullName = SharedPrefHelper.getInstance(context).getFullName();
        String email    = SharedPrefHelper.getInstance(context).getFullName(); // userName stores email now

        textViewWelcome.setText("שלום, " + fullName);
        tvProfileName.setText(fullName);
        tvProfileEmail.setText(email);
    }
}