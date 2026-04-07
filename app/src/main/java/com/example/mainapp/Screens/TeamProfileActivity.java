package com.example.mainapp.Screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.GameHistoryAdapter;
import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;
import java.util.List;

public class TeamProfileActivity extends AppCompatActivity {

    private TextView    tvTeamName, tvTeamNumber, tvAvgPoints, tvClimbRate, tvGamesPlayed;
    private RecyclerView rvGameHistory;
    private ProgressBar  progressBar;
    private TextView     tvNoGames;
    private TextView     tvBack;
    private Context      context;

    private GameHistoryAdapter gameHistoryAdapter;
    private ArrayList<TeamAtGame> gameList = new ArrayList<>();

    public static final String EXTRA_TEAM_NUMBER = "teamNumber";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_profile);
        context = this;
        init();

        int teamNumber = getIntent().getIntExtra(EXTRA_TEAM_NUMBER, -1);
        if (teamNumber == -1) { finish(); return; }

        tvBack.setOnClickListener(v -> finish());

        loadTeamProfile(teamNumber);
    }

    private void loadTeamProfile(int teamNumber) {
        progressBar.setVisibility(View.VISIBLE);

        DataHelper.getInstance().readTeamStats(String.valueOf(teamNumber),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override public void onSuccess(TeamStats data) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (data == null) { tvNoGames.setVisibility(View.VISIBLE); return; }
                            displayTeamProfile(data);
                        });
                    }
                    @Override public void onFailure(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvNoGames.setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }

    private void displayTeamProfile(TeamStats stats) {
        Team team = stats.getTeam();

        // Header info
        tvTeamNumber.setText("קבוצה " + team.getTeamNumber());
        tvTeamName.setText(team.getTeamName());
        tvGamesPlayed.setText(String.valueOf(stats.getGamesPlayed()));

        // Avg points
        double avg = stats.calculateAvgPoints();
        tvAvgPoints.setText(String.format("%.1f", avg));

        // Climb rate
        int climbRate = calculateClimbRate(stats.getAllGames());
        tvClimbRate.setText(climbRate + "%");

        // Game history list
        List<TeamAtGame> games = stats.getAllGames();
        if (games == null || games.isEmpty()) {
            tvNoGames.setVisibility(View.VISIBLE);
            return;
        }

        // Show most recent games first
        gameList.clear();
        for (int i = games.size() - 1; i >= 0; i--) gameList.add(games.get(i));
        gameHistoryAdapter.notifyDataSetChanged();
        rvGameHistory.setVisibility(View.VISIBLE);
    }

    private int calculateClimbRate(List<TeamAtGame> games) {
        if (games == null || games.isEmpty()) return 0;
        int climbed = 0;
        for (TeamAtGame g : games) {
            if (g.getClimb() != null) {
                switch (g.getClimb()) {
                    case HIGH:
                    case LOW:
                        climbed++;
                        break;
                    default:
                        break;
                }
            }
        }
        return (int) Math.round(climbed * 100.0 / games.size());
    }

    private void init() {
        tvBack        = findViewById(R.id.tvBack);
        tvTeamNumber  = findViewById(R.id.tvTeamNumber);
        tvTeamName    = findViewById(R.id.tvTeamName);
        tvAvgPoints   = findViewById(R.id.tvAvgPoints);
        tvClimbRate   = findViewById(R.id.tvClimbRate);
        tvGamesPlayed = findViewById(R.id.tvGamesPlayed);
        progressBar   = findViewById(R.id.progressBar);
        tvNoGames     = findViewById(R.id.tvNoGames);
        rvGameHistory = findViewById(R.id.rvGameHistory);

        gameHistoryAdapter = new GameHistoryAdapter(gameList);
        rvGameHistory.setLayoutManager(new LinearLayoutManager(context));
        rvGameHistory.setAdapter(gameHistoryAdapter);
        rvGameHistory.setVisibility(View.GONE);
    }
}