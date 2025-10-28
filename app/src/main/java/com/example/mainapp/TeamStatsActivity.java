package com.example.mainapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.TeamStatsAdapter;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.FirebaseListenerService;
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

    private BroadcastReceiver teamStatsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (FirebaseListenerService.ACTION_TEAM_STATS_UPDATED.equals(intent.getAction())) {
                String updateType = intent.getStringExtra(FirebaseListenerService.EXTRA_UPDATE_TYPE);
                int count = intent.getIntExtra(FirebaseListenerService.EXTRA_TEAM_STATS_COUNT, 0);

                if (FirebaseListenerService.UPDATE_TYPE_SUCCESS.equals(updateType)) {
                    System.out.println("✅ Firebase updated: " + count + " teams");
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(context, "נטענו " + count + " קבוצות", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    System.err.println("❌ Firebase update error");
                    Toast.makeText(context, "שגיאה בעדכון נתונים", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

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

        startFirebaseService();
        DataHelper.getInstance().isTableDataEmpty(new DataHelper.ExistsCallback() {
            @Override
            public void onResult(boolean isEmpty) {
                if(isEmpty) uploadDataFromAPIToDB();
            }
        });

    }

    private void startFirebaseService(){
        Intent listenerService = new Intent(this, FirebaseListenerService.class);
        startService(listenerService);

        System.out.println("🚀 Firebase Service started");
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
        loadTeamsFromAPI();
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