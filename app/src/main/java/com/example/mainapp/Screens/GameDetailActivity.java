package com.example.mainapp.Screens;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.concurrent.CountDownLatch;

public class GameDetailActivity extends AppCompatActivity {

    private TextView tvGameTitle, tvBackBtn;

    // Red team views
    private TextView tvRedTeam1, tvRedTeam2, tvRedTeam3;
    private TextView tvRedPoints1, tvRedPoints2, tvRedPoints3;
    private TextView tvRedHeight1, tvRedHeight2, tvRedHeight3;
    private TextView tvRedClimb1, tvRedClimb2, tvRedClimb3;
    private TextView tvRedGames1, tvRedGames2, tvRedGames3;

    // Blue team views
    private TextView tvBlueTeam1, tvBlueTeam2, tvBlueTeam3;
    private TextView tvBluePoints1, tvBluePoints2, tvBluePoints3;
    private TextView tvBlueHeight1, tvBlueHeight2, tvBlueHeight3;
    private TextView tvBlueClimb1, tvBlueClimb2, tvBlueClimb3;
    private TextView tvBlueGames1, tvBlueGames2, tvBlueGames3;

    private ProgressBar progressBar;

    private int gameNumber;
    private int[] redTeams  = new int[3];
    private int[] blueTeams = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        gameNumber   = getIntent().getIntExtra("gameNumber", 0);
        redTeams[0]  = getIntent().getIntExtra("redTeam1", 0);
        redTeams[1]  = getIntent().getIntExtra("redTeam2", 0);
        redTeams[2]  = getIntent().getIntExtra("redTeam3", 0);
        blueTeams[0] = getIntent().getIntExtra("blueTeam1", 0);
        blueTeams[1] = getIntent().getIntExtra("blueTeam2", 0);
        blueTeams[2] = getIntent().getIntExtra("blueTeam3", 0);

        init();

        tvGameTitle.setText("משחק " + gameNumber);
        tvBackBtn.setOnClickListener(v -> finish());

        tvRedTeam1.setText("#" + redTeams[0]);
        tvRedTeam2.setText("#" + redTeams[1]);
        tvRedTeam3.setText("#" + redTeams[2]);
        tvBlueTeam1.setText("#" + blueTeams[0]);
        tvBlueTeam2.setText("#" + blueTeams[1]);
        tvBlueTeam3.setText("#" + blueTeams[2]);

        loadAllTeamData();
    }

    private void loadAllTeamData() {
        progressBar.setVisibility(View.VISIBLE);
        CountDownLatch latch = new CountDownLatch(6);
        TeamStats[] stats = new TeamStats[6]; // 0-2 red, 3-5 blue

        for (int i = 0; i < 3; i++) {
            final int ri = i;
            final int bi = i + 3;

            DataHelper.getInstance().readTeamStats(
                    String.valueOf(redTeams[i]),
                    new DataHelper.DataCallback<TeamStats>() {
                        @Override public void onSuccess(TeamStats data) { stats[ri] = data; latch.countDown(); }
                        @Override public void onFailure(String e)       { stats[ri] = null; latch.countDown(); }
                    }
            );

            DataHelper.getInstance().readTeamStats(
                    String.valueOf(blueTeams[i]),
                    new DataHelper.DataCallback<TeamStats>() {
                        @Override public void onSuccess(TeamStats data) { stats[bi] = data; latch.countDown(); }
                        @Override public void onFailure(String e)       { stats[bi] = null; latch.countDown(); }
                    }
            );
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    bindTeamData(stats[0], tvRedPoints1, tvRedHeight1, tvRedClimb1, tvRedGames1);
                    bindTeamData(stats[1], tvRedPoints2, tvRedHeight2, tvRedClimb2, tvRedGames2);
                    bindTeamData(stats[2], tvRedPoints3, tvRedHeight3, tvRedClimb3, tvRedGames3);
                    bindTeamData(stats[3], tvBluePoints1, tvBlueHeight1, tvBlueClimb1, tvBlueGames1);
                    bindTeamData(stats[4], tvBluePoints2, tvBlueHeight2, tvBlueClimb2, tvBlueGames2);
                    bindTeamData(stats[5], tvBluePoints3, tvBlueHeight3, tvBlueClimb3, tvBlueGames3);
                    progressBar.setVisibility(View.GONE);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void bindTeamData(TeamStats ts,
                              TextView tvPoints,
                              TextView tvHeight,
                              TextView tvClimb,
                              TextView tvGames) {
        if (ts == null || ts.getGamesPlayed() == 0) {
            tvPoints.setText("נק׳: אין מידע");
            tvHeight.setText("גובה: —");
            tvClimb.setText("טיפוס: —");
            tvGames.setText("משחקים: 0");
            return;
        }

        tvPoints.setText(String.format("נק׳: %.1f", ts.calculateAvgPoints()));

        String height = ts.getMostScoredGamePiece() != null
                ? "גובה: " + ts.getMostScoredGamePiece().name()
                : "גובה: —";
        tvHeight.setText(height);

        tvClimb.setText(String.format("טיפוס: %.0f%%",
                ts.calculateAvgClimbPerGame() * 100));

        tvGames.setText("משחקים: " + ts.getGamesPlayed());
    }

    private void init() {
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvBackBtn   = findViewById(R.id.tvBackBtn);
        progressBar = findViewById(R.id.progressBar);

        tvRedTeam1   = findViewById(R.id.tvRedTeam1);
        tvRedTeam2   = findViewById(R.id.tvRedTeam2);
        tvRedTeam3   = findViewById(R.id.tvRedTeam3);
        tvRedPoints1 = findViewById(R.id.tvRedPoints1);
        tvRedPoints2 = findViewById(R.id.tvRedPoints2);
        tvRedPoints3 = findViewById(R.id.tvRedPoints3);
        tvRedHeight1 = findViewById(R.id.tvRedHeight1);
        tvRedHeight2 = findViewById(R.id.tvRedHeight2);
        tvRedHeight3 = findViewById(R.id.tvRedHeight3);
        tvRedClimb1  = findViewById(R.id.tvRedClimb1);
        tvRedClimb2  = findViewById(R.id.tvRedClimb2);
        tvRedClimb3  = findViewById(R.id.tvRedClimb3);
        tvRedGames1  = findViewById(R.id.tvRedGames1);
        tvRedGames2  = findViewById(R.id.tvRedGames2);
        tvRedGames3  = findViewById(R.id.tvRedGames3);

        tvBlueTeam1   = findViewById(R.id.tvBlueTeam1);
        tvBlueTeam2   = findViewById(R.id.tvBlueTeam2);
        tvBlueTeam3   = findViewById(R.id.tvBlueTeam3);
        tvBluePoints1 = findViewById(R.id.tvBluePoints1);
        tvBluePoints2 = findViewById(R.id.tvBluePoints2);
        tvBluePoints3 = findViewById(R.id.tvBluePoints3);
        tvBlueHeight1 = findViewById(R.id.tvBlueHeight1);
        tvBlueHeight2 = findViewById(R.id.tvBlueHeight2);
        tvBlueHeight3 = findViewById(R.id.tvBlueHeight3);
        tvBlueClimb1  = findViewById(R.id.tvBlueClimb1);
        tvBlueClimb2  = findViewById(R.id.tvBlueClimb2);
        tvBlueClimb3  = findViewById(R.id.tvBlueClimb3);
        tvBlueGames1  = findViewById(R.id.tvBlueGames1);
        tvBlueGames2  = findViewById(R.id.tvBlueGames2);
        tvBlueGames3  = findViewById(R.id.tvBlueGames3);
    }
}