package com.example.mainapp.Screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class LoadingScreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus, tvPercent, tvNoInternet;
    private Button btnRetry;
    private SharedPrefHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        progressBar  = findViewById(R.id.progressBar);
        tvStatus     = findViewById(R.id.tvStatus);
        tvPercent    = findViewById(R.id.tvPercent);
        tvNoInternet = findViewById(R.id.tvNoInternet);
        btnRetry     = findViewById(R.id.btnRetry);
        prefs        = SharedPrefHelper.getInstance(this);

        progressBar.setMax(100);

        btnRetry.setOnClickListener(v -> {
            if (!InternetUtils.isInternetConnected(this)) {
                tvStatus.setText("עדיין אין חיבור...");
                return;
            }
            tvNoInternet.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvPercent.setVisibility(View.VISIBLE);
            setProgress(0, "מאתחל...");
            try {
                loadStep1_TBATeams();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        boolean hasInternet       = InternetUtils.isInternetConnected(this);
        boolean hasLaunchedBefore = prefs.hasLaunchedBefore();

        if (!hasInternet && !hasLaunchedBefore) {
            showNoInternetState();
            return;
        }

        if (!hasInternet) {
            // Returning user offline — skip loading
            navigateNext();
            return;
        }

        // Has internet — start sequential loading
        setProgress(0, "מאתחל...");
        try {
            loadStep1_TBATeams();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== NO INTERNET ====================

    private void showNoInternetState() {
        progressBar.setVisibility(View.GONE);
        tvPercent.setVisibility(View.GONE);
        tvStatus.setText("אין חיבור לאינטרנט");
        tvNoInternet.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    // ==================== SEQUENTIAL LOADING STEPS ====================

    // Step 1 — Load TBA event teams
    private void loadStep1_TBATeams() throws JSONException, IOException {
        setProgress(10, "טוען קבוצות מ-TBA...");
        TBAApiManager.getInstance().getEventTeams(
                Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.TeamCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        AppCache.getInstance().setTeamsAtEvent(teams.toArray(new Team[0]));
                        loadStep2_TeamStats();
                    }
                    @Override
                    public void onError(Exception e) {
                        loadStep2_TeamStats(); // continue even on error
                    }
                }
        );
    }

    // Step 2 — Load all team stats from Firebase
    private void loadStep2_TeamStats() {
        setProgress(30, "טוען נתוני סקאוטינג...");
        DataHelper.getInstance().readAllTeamStats(
                new DataHelper.DataCallback<ArrayList<TeamStats>>() {
                    @Override
                    public void onSuccess(ArrayList<TeamStats> data) {
                        AppCache.getInstance().setAllTeamStats(data);

                        // Calculate total games while we have the data
                        int totalGames = 0;
                        for (TeamStats t : data) {
                            if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                        }
                        AppCache.getInstance().setTotalGames(totalGames);
                        loadStep3_TeamCount();
                    }
                    @Override
                    public void onFailure(String error) {
                        loadStep3_TeamCount(); // continue even on error
                    }
                }
        );
    }

    // Step 3 — Count teams in Firebase
    private void loadStep3_TeamCount() {
        setProgress(50, "סופר קבוצות...");
        DataHelper.getInstance().countTeams(count -> {
            AppCache.getInstance().setTeamCount(count);
            loadStep4_Games();
        });
    }

    // Step 4 — Load games list from TBA
    private void loadStep4_Games() {
        setProgress(70, "טוען רשימת משחקים...");
        TBAApiManager.getInstance().getEventGames(
                Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.GameCallback() {
                    @Override
                    public void onSuccess(ArrayList<Game> games) {
                        AppCache.getInstance().setGamesList(games);
                        loadStep5_IsraeliTeams();
                    }
                    @Override
                    public void onError(Exception e) {
                        loadStep5_IsraeliTeams(); // continue even on error
                    }
                }
        );
    }

    // Step 5 — Load all Israeli teams (last step — navigate when done)
    private void loadStep5_IsraeliTeams() {
        setProgress(90, "טוען קבוצות ישראליות...");
        TBAApiManager.getInstance().getIsraeliTeams(
                new TBAApiManager.TeamListCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        AppCache.getInstance().setIsraeliTeams(teams);
                        setProgress(100, "מוכן! ✓");

                        // Mark that app has been successfully loaded at least once
                        prefs.markHasLaunched();

                        // Small delay so user sees 100%
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> navigateNext(), 400);
                    }
                }
        );
    }

    // ==================== HELPERS ====================

    private void setProgress(int percent, String message) {
        runOnUiThread(() -> {
            progressBar.setProgress(percent);
            tvStatus.setText(message);
            tvPercent.setText(percent + "%");
        });
    }

    private void navigateNext() {
        boolean isLoggedIn = prefs.isUserLoggedIn();
        Intent intent = isLoggedIn
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginScreen.class);
        startActivity(intent);
        finish();
    }
}