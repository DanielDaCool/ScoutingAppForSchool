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
import com.example.mainapp.Screens.AdminScreens.AdminMainActivity;
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

/**
 * LoadingScreen is the initial synchronization screen of the application.
 * It performs several critical steps:
 * 1. Verifies internet connectivity.
 * 2. Fetches team and match data from The Blue Alliance API.
 * 3. Retrieves existing team statistics from Firebase.
 * 4. Initializes local caches to ensure a smooth user experience in other activities.
 * 5. Navigates the user to either the Admin or Scouter dashboard based on their role.
 */
public class LoadingScreen extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus, tvPercent, tvNoInternet;
    private Button btnRetry;
    private SharedPrefHelper prefs;
    private EVENTS districtToLoad;

    @Override
/**
 * Initializes the activity and prepares the screen components and data.
 * @param savedInstanceState parameter required for this method.
 */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvPercent = findViewById(R.id.tvPercent);
        tvNoInternet = findViewById(R.id.tvNoInternet);
        btnRetry = findViewById(R.id.btnRetry);
        prefs = SharedPrefHelper.getInstance(this);
        progressBar.setMax(100);


        districtToLoad = prefs.getCurrentDistrict();
        if (districtToLoad == null) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
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

        boolean hasInternet = InternetUtils.isInternetConnected(this);
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

/**
 * Executes the logic associated with the showNoInternetState operation.
 */
    private void showNoInternetState() {
        progressBar.setVisibility(View.GONE);
        tvPercent.setVisibility(View.GONE);
        tvStatus.setText("אין חיבור לאינטרנט");
        tvNoInternet.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }



/**
 * Executes the logic associated with the loadStep1_TBATeams operation.
 */
    private void loadStep1_TBATeams() {
        setProgress(10, "טוען  מידע...");
        try {
            TBAApiManager.getInstance().getEventTeams(districtToLoad,
                    new TBAApiManager.TeamCallback() {
                        @Override
/**
 * Executes the logic associated with the onSuccess operation.
 * @param teams parameter required for this method.
 */
                        public void onSuccess(ArrayList<Team> teams) {
                            AppCache.getInstance().setTeamsAtEvent(teams.toArray(new Team[0]));
                            loadStep2_TeamStats();
                        }

                        @Override
/**
 * Executes the logic associated with the onError operation.
 * @param e parameter required for this method.
 */
                        public void onError(Exception e) {
                            loadStep2_TeamStats();
                        }
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

/**
 * Executes the logic associated with the loadStep2_TeamStats operation.
 */
    private void loadStep2_TeamStats() {
        setProgressWithoutText(30);
        DataHelper.getInstance().readAllTeamStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
/**
 * Executes the logic associated with the onSuccess operation.
 * @param data parameter required for this method.
 */
            public void onSuccess(ArrayList<TeamStats> data) {
                AppCache.getInstance().setAllTeamStats(data);
                int totalGames = 0;
                for (TeamStats t : data)
                    if (t.getAllGames() != null) totalGames += t.getAllGames().size();
                AppCache.getInstance().setTotalGames(totalGames);
                loadStep3_TeamCount();
            }

            @Override
/**
 * Executes the logic associated with the onFailure operation.
 * @param error parameter required for this method.
 */
            public void onFailure(String error) {
                loadStep3_TeamCount();
            }
        });
    }

/**
 * Executes the logic associated with the loadStep3_TeamCount operation.
 */
    private void loadStep3_TeamCount() {
        setProgressWithoutText(50);
        DataHelper.getInstance().countTeams(count -> {
            AppCache.getInstance().setTeamCount(count);
            loadStep4_Games();
        });
    }

/**
 * Executes the logic associated with the loadStep4_Games operation.
 */
    private void loadStep4_Games() {
        setProgressWithoutText(65);
        TBAApiManager.getInstance().getEventGames(districtToLoad,
                new TBAApiManager.GameCallback() {
                    @Override
/**
 * Executes the logic associated with the onSuccess operation.
 * @param games parameter required for this method.
 */
                    public void onSuccess(ArrayList<Game> games) {
                        AppCache.getInstance().setGamesList(games);
                        loadStep5_IsraeliTeams();
                    }

                    @Override
/**
 * Executes the logic associated with the onError operation.
 * @param e parameter required for this method.
 */
                    public void onError(Exception e) {
                        loadStep5_IsraeliTeams();
                    }
                }
        );
    }

/**
 * Executes the logic associated with the loadStep5_IsraeliTeams operation.
 */
    private void loadStep5_IsraeliTeams() {
        setProgressWithoutText(80);
        TBAApiManager.getInstance().getIsraeliTeams(new TBAApiManager.TeamCallback() {
            @Override
/**
 * Executes the logic associated with the onSuccess operation.
 * @param teams parameter required for this method.
 */
            public void onSuccess(ArrayList<Team> teams) {
                AppCache.getInstance().setIsraeliTeams(teams);
                loadStep6_InitTeams(teams);
            }

            @Override
/**
 * Executes the logic associated with the onError operation.
 * @param e parameter required for this method.
 */
            public void onError(Exception e) {

            }
        });
    }

/**
 * Executes the logic associated with the loadStep6_InitTeams operation.
 * @param teams parameter required for this method.
 */
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

/**
 * Executes the logic associated with the setProgressWithoutText operation.
 * @param percent parameter required for this method.
 */
    private void setProgressWithoutText(int percent) {
        setProgress(percent, tvStatus.getText().toString());
    }

/**
 * Executes the logic associated with the setProgress operation.
 * @param percent parameter required for this method.
 * @param message parameter required for this method.
 */
    private void setProgress(int percent, String message) {
        runOnUiThread(() -> {
            progressBar.setProgress(percent);
            tvStatus.setText(message);
            tvPercent.setText(percent + "%");
        });
    }

/**
 * Executes the logic associated with the navigateNext operation.
 */
    private void navigateNext() {
        if (!prefs.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }
        Intent intent = prefs.isAdmin()
                ? new Intent(this, AdminMainActivity.class)
                : new Intent(this, ScouterMainActivity.class);
        startActivity(intent);
        finish();
    }
}