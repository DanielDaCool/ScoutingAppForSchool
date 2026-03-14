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
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadingScreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus, tvPercent, tvNoInternet;
    private Button btnRetry;
    private SharedPrefHelper prefs;

    private static final int TOTAL_STEPS = 5;
    private final AtomicInteger completedSteps = new AtomicInteger(0);

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
                // Still no internet — shake the button or just ignore
                tvStatus.setText("עדיין אין חיבור...");
                return;
            }
            // Internet is back — hide no-internet UI and start loading
            tvNoInternet.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvPercent.setVisibility(View.VISIBLE);
            completedSteps.set(0);
            setProgress(0, "מאתחל...");
            try {
                startLoading();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        boolean hasInternet     = InternetUtils.isInternetConnected(this);
        boolean hasLaunchedBefore = prefs.hasLaunchedBefore();

        if (!hasInternet && !hasLaunchedBefore) {
            // First launch with no internet — block completely
            showNoInternetState();
            return;
        }

        if (!hasInternet) {
            // Returning user offline — skip loading, use last session's cache
            // (AppCache is empty in memory but user has seen data before)
            navigateNext();
            return;
        }

        // Has internet — load everything fresh
        setProgress(0, "מאתחל...");
        try {
            startLoading();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== NO INTERNET STATE ====================

    private void showNoInternetState() {
        progressBar.setVisibility(View.GONE);
        tvPercent.setVisibility(View.GONE);
        tvStatus.setText("אין חיבור לאינטרנט");
        tvNoInternet.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    // ==================== LOADING ====================

    private void startLoading() throws JSONException, IOException {
        setProgress(5, "טוען נתוני קבוצות...");

        TBAApiManager.getInstance().getEventTeams(Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.TeamCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        AppCache.getInstance().setTeamsAtEvent(teams.toArray(new Team[0]));
                    }
                    @Override
                    public void onError(Exception e) {}
                }
        );

        DataHelper.getInstance().readAllTeamStats(
                new DataHelper.DataCallback<ArrayList<TeamStats>>() {
                    @Override
                    public void onSuccess(ArrayList<TeamStats> data) {
                        AppCache.getInstance().setAllTeamStats(data);
                        onStepComplete("נתוני קבוצות נטענו ✓");

                        DataHelper.getInstance().countTeams(count -> {
                            AppCache.getInstance().setTeamCount(count);
                            onStepComplete("ספירת קבוצות ✓");
                        });

                        int totalGames = 0;
                        for (TeamStats t : data) {
                            if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                        }
                        AppCache.getInstance().setTotalGames(totalGames);
                        onStepComplete("ספירת משחקים ✓");
                    }
                    @Override
                    public void onFailure(String error) {
                        onStepComplete("נתוני קבוצות (שגיאה)");
                        onStepComplete("ספירת קבוצות (שגיאה)");
                        onStepComplete("ספירת משחקים (שגיאה)");
                    }
                }
        );

        setProgress(10, "טוען רשימת משחקים...");
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.GameCallback() {
                    @Override
                    public void onSuccess(ArrayList<com.example.mainapp.Utils.Game> games) {
                        AppCache.getInstance().setGamesList(games);
                        onStepComplete("רשימת משחקים נטענה ✓");
                    }
                    @Override
                    public void onError(Exception e) {
                        onStepComplete("רשימת משחקים (שגיאה)");
                    }
                }
        );

        setProgress(15, "טוען קבוצות ישראליות...");
        TBAApiManager.getInstance().getIsraeliTeams(
                new TBAApiManager.TeamListCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        AppCache.getInstance().setIsraeliTeams(teams);
                        onStepComplete("קבוצות ישראליות נטענו ✓");
                    }
                }
        );
    }

    private void onStepComplete(String message) {
        int done    = completedSteps.incrementAndGet();
        int percent = (done * 100) / TOTAL_STEPS;
        setProgress(percent, message);

        if (done >= TOTAL_STEPS) {
            // Mark that data has been loaded at least once
            prefs.markHasLaunched();
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, 600);
        }
    }

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