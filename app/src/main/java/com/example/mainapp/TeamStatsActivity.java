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
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class TeamStatsActivity extends AppCompatActivity {
    private Context context;
    private RecyclerView recyclerView;
    private TeamStatsAdapter adapter;

    // CHANGE 1: The activity now holds the final, aggregated list
    private static ArrayList<TeamStats> allTeamsStats;
    private ArrayList<Team> teamsAtComp;
    private ValueEventListener teamStatsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        init();

        // CHANGE 2: Pass the aggregated list to the adapter
        adapter = new TeamStatsAdapter(allTeamsStats);
        recyclerView.setAdapter(adapter);

        loadTeamsFromAPI();

    }


    public static void setAllTeamsStats(ArrayList<TeamStats> arrList){
        allTeamsStats = arrList;
    }

    private void loadTeamsFromAPI() {
        try {
            TBAApiManager.getInstance().getEventTeams(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.TeamCallback() {
                @Override
                public void onSuccess(ArrayList<Team> teams) {
                    runOnUiThread(() -> {
                        System.out.println("Successfully loaded " + teams.size() + " games");

                        teamsAtComp.clear();
                        teamsAtComp.addAll(teams);

                        allTeamsStats.clear();
                        for (Team team : teams) {
                            System.out.println("NUM: " + team.getTeamNumber() + " NAME: " + team.getTeamName());
                            allTeamsStats.add(new TeamStats(team));
                           // allTeamsStats.add(new TeamStats(team));
                        }
                        adapter.notifyDataSetChanged();

                        uploadDataFromAPIToDB();

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

    private void uploadDataFromAPIToDB() {

        for (TeamStats teamStats : allTeamsStats){
            DataHelper.getInstance().createTeamStats(teamStats, new DataHelper.DatabaseCallback() {
                @Override
                public void onSuccess(String id) {

                }

                @Override
                public void onFailure(String error) {

                }
            });
        }
    }



    private void init() {
        // Get all team games (the flat list of every game played by every team)
        context = TeamStatsActivity.this;
        allTeamsStats = new ArrayList<>();
        teamsAtComp = new ArrayList<>();
    }

}