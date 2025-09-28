package com.example.mainapp;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mainapp.Adapters.TeamStatsAdapter;
import com.example.mainapp.Utils.TeamAtGame;
import com.example.mainapp.Utils.Tests;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TeamStatsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TeamStatsAdapter adapter;

    // CHANGE 1: The activity now holds the final, aggregated list
    private ArrayList<ArrayList<TeamAtGame>> allTeamsGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        init();

        // CHANGE 2: Pass the aggregated list to the adapter
        adapter = new TeamStatsAdapter(allTeamsGames);
        recyclerView.setAdapter(adapter);
    }

    private void init() {
        // Get all team games (the flat list of every game played by every team)
        allTeamsGames = Tests.generateAllGamesOfTeams();
    }

}