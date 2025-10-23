package com.example.mainapp;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mainapp.Adapters.TeamStatsAdapter;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.Team;
import com.example.mainapp.Utils.TeamAtGame;
import com.example.mainapp.Utils.Tests;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TeamStatsActivity extends AppCompatActivity {
    private Context context;
    private RecyclerView recyclerView;
    private TeamStatsAdapter adapter;

    // CHANGE 1: The activity now holds the final, aggregated list
    private ArrayList<ArrayList<TeamAtGame>> allTeamsGames;
    private ArrayList<Team> teamsAtComp;

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
        if(allTeamsGames.size() == 0){
            try {
                TBAApiManager.getInstance().getEventTeams(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.TeamCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        runOnUiThread(() -> {
                            System.out.println("Successfully loaded " + teams.size() + " games");

                            teamsAtComp.clear();
                            teamsAtComp.addAll(teams);
                            allTeamsGames.clear();
                            for (Team team : teams){
                                ArrayList<TeamAtGame> empty = new ArrayList<>();
                                empty.add(new TeamAtGame(team, false, -1));
                                allTeamsGames.add(empty);
                            }
                            adapter.notifyDataSetChanged();

                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            System.err.println("Error loading games: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(context, "Error loading games: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void init() {
        // Get all team games (the flat list of every game played by every team)
        context = TeamStatsActivity.this;
        allTeamsGames = new ArrayList<>();
        teamsAtComp = new ArrayList<>();
    }

}