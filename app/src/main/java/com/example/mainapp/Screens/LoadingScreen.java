package com.example.mainapp.Screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Screens.AuthenticationScreens.LoginScreen;
import com.example.mainapp.TBAHelpers.TBAApiManager;

import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadingScreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvPercent;

    // Total steps: teams, all stats, games list, team count, total games
    private static final int TOTAL_STEPS = 5;
    private final AtomicInteger completedSteps = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        progressBar = findViewById(R.id.progressBar);
        tvStatus    = findViewById(R.id.tvStatus);
        tvPercent   = findViewById(R.id.tvPercent);

        progressBar.setMax(100);
        setProgress(0, "מאתחל...");

        try {
            startLoading();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startLoading() throws JSONException, IOException {
        // Step 1: Load all team stats

        TBAApiManager.getInstance().getEventTeams(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.TeamCallback() {
            @Override
            public void onSuccess(ArrayList<Team> teams) {
                AppCache.getInstance().setTeamsAtEvent(teams.toArray(new Team[teams.size()]));
            }

            @Override
            public void onError(Exception e) {

            }
        });

        setProgress(5, "טוען נתוני קבוצות...");
        DataHelper.getInstance().readAllTeamStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> data) {
                AppCache.getInstance().setAllTeamStats(data);
                onStepComplete("נתוני קבוצות נטענו ✓");

                // Step 2: Count teams
                DataHelper.getInstance().countTeams(count -> {
                    AppCache.getInstance().setTeamCount(count);
                    onStepComplete("ספירת קבוצות ✓");
                });

                // Step 3: Count total games
                int totalGames = 0;
                for (TeamStats t : data) {
                    if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                }
                AppCache.getInstance().setTotalGames(totalGames);
                onStepComplete("ספירת משחקים ✓");
            }

            @Override
            public void onFailure(String error) {
                // Still advance — don't block the user
                onStepComplete("נתוני קבוצות (שגיאה)");
                onStepComplete("ספירת קבוצות (שגיאה)");
                onStepComplete("ספירת משחקים (שגיאה)");
            }
        });

        // Step 4: Load games list from TBA
        setProgress(10, "טוען רשימת משחקים...");
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.GameCallback() {
                    @Override
                    public void onSuccess(java.util.ArrayList<com.example.mainapp.Utils.Game> games) {
                        AppCache.getInstance().setGamesList(games);
                        onStepComplete("רשימת משחקים נטענה ✓");
                    }

                    @Override
                    public void onError(Exception e) {
                        onStepComplete("רשימת משחקים (שגיאה)");
                    }
                });

        // Step 5: Load Israeli teams list
        setProgress(15, "טוען קבוצות ישראליות...");
        TBAApiManager.getInstance().getIsraeliTeams(new TBAApiManager.TeamListCallback() {
            @Override
            public void onSuccess(java.util.ArrayList<com.example.mainapp.Utils.TeamUtils.Team> teams) {
                AppCache.getInstance().setIsraeliTeams(teams);
                onStepComplete("קבוצות ישראליות נטענו ✓");
            }
        });
    }

    private void onStepComplete(String message) {
        int done = completedSteps.incrementAndGet();
        int percent = (done * 100) / TOTAL_STEPS;
        setProgress(percent, message);

        if (done >= TOTAL_STEPS) {
            // Small delay so user sees 100%
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
        boolean isLoggedIn = SharedPrefHelper.getInstance(this).isUserLoggedIn();
        Intent intent = isLoggedIn
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginScreen.class);
        startActivity(intent);
        finish();
    }
}