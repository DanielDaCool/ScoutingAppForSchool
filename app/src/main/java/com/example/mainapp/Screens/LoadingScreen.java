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
import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.TBAHelpers.TBAApiManager;
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
    private TextView    tvStatus, tvPercent, tvNoInternet;
    private Button      btnRetry;
    private SharedPrefHelper prefs;
    private EVENTS districtToLoad;

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

        // Determine which district's data to load
        if (prefs.isAdmin()) {
            districtToLoad = EVENTS.values()[0];
        } else {
            districtToLoad = prefs.getCurrentDistrict();
            if (districtToLoad == null) {
                // Scouter has no district — force them to pick one
                startActivity(new Intent(this, LoginScreen.class));
                finish();
                return;
            }
        }

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
            loadStep1_TBATeams();
        });

        boolean hasInternet       = InternetUtils.isInternetConnected(this);
        boolean hasLaunchedBefore = prefs.hasLaunchedBefore();

        if (!hasInternet && !hasLaunchedBefore) {
            showNoInternetState();
            return;
        }
        if (!hasInternet) {
            navigateNext();
            return;
        }

        setProgress(0, "מאתחל...");
        loadStep1_TBATeams();
    }

    private void showNoInternetState() {
        progressBar.setVisibility(View.GONE);
        tvPercent.setVisibility(View.GONE);
        tvStatus.setText("אין חיבור לאינטרנט");
        tvNoInternet.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }


    private void loadStep1_TBATeams() {
        setProgress(10, "טוען  מידע...");
        try {
            TBAApiManager.getInstance().getEventTeams(districtToLoad,
                    new TBAApiManager.TeamCallback() {
                        @Override public void onSuccess(ArrayList<Team> teams) {
                            AppCache.getInstance().setTeamsAtEvent(teams.toArray(new Team[0]));
                            loadStep2_TeamStats();
                        }
                        @Override public void onError(Exception e) { loadStep2_TeamStats(); }
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadStep2_TeamStats() {
        setProgressWithoutText(30);
        DataHelper.getInstance().readAllTeamStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override public void onSuccess(ArrayList<TeamStats> data) {
                AppCache.getInstance().setAllTeamStats(data);
                int totalGames = 0;
                for (TeamStats t : data) if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                AppCache.getInstance().setTotalGames(totalGames);
                loadStep3_TeamCount();
            }
            @Override public void onFailure(String error) { loadStep3_TeamCount(); }
        });
    }

    private void loadStep3_TeamCount() {
        setProgressWithoutText(50);
        DataHelper.getInstance().countTeams(count -> {
            AppCache.getInstance().setTeamCount(count);
            loadStep4_Games();
        });
    }

    private void loadStep4_Games() {
        setProgressWithoutText(65);
        TBAApiManager.getInstance().getEventGames(districtToLoad,
                new TBAApiManager.GameCallback() {
                    @Override public void onSuccess(ArrayList<Game> games) {
                        AppCache.getInstance().setGamesList(games);
                        loadStep5_IsraeliTeams();
                    }
                    @Override public void onError(Exception e) { loadStep5_IsraeliTeams(); }
                }
        );
    }

    private void loadStep5_IsraeliTeams() {
        setProgressWithoutText(80);
        TBAApiManager.getInstance().getIsraeliTeams(new TBAApiManager.TeamListCallback() {
            @Override public void onSuccess(ArrayList<Team> teams) {
                AppCache.getInstance().setIsraeliTeams(teams);
                loadStep6_InitTeams(teams);
            }
        });
    }

    /** Moved from old SplashScreen — creates Firebase entries for new teams. */
    private void loadStep6_InitTeams(ArrayList<Team> teams) {
        setProgressWithoutText(90);
        for (Team t : teams) {
            DataHelper.getInstance().isTeamDataExists(t, exists -> {
                if (!exists) DataHelper.getInstance().createTeamStats(new TeamStats(t), null);
            });
        }
        setProgress(100, "מוכן! ✓");
        prefs.markHasLaunched();
        new Handler(Looper.getMainLooper()).postDelayed(() -> navigateNext(), 400);
    }

    private void setProgressWithoutText(int percent){
        setProgress(percent, tvStatus.getText().toString());
    }
    private void setProgress(int percent, String message) {
        runOnUiThread(() -> {
            progressBar.setProgress(percent);
            tvStatus.setText(message);
            tvPercent.setText(percent + "%");
        });
    }

    private void navigateNext() {
        if (!prefs.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }
        Intent intent = prefs.isAdmin()
                ? new Intent(this, AdminMainActivity.class)
                : new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}