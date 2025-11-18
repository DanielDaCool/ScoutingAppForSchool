package com.example.mainapp;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.TeamStatsAdapter;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class TeamStatsActivity extends AppCompatActivity {
    private Context context;
    private static RecyclerView recyclerView;
    private static TeamStatsAdapter adapter;
    private EditText editText;

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

        uploadDataFromAPIToDB();
        uploadDataFromDBToAdapter();

        addFilterSearchTeam();

    }

    private void addFilterSearchTeam() {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String input = editText.getText().toString().trim();

                    if (input.isEmpty()) {
                        adapter.updateData(allTeamsStats);
                        adapter.notifyDataSetChanged();
                        return true;
                    }

                    try {
                        int teamNumber = Integer.parseInt(input);
                        if (teamNumber < 0 || teamNumber > 10000) {
                            Toast.makeText(context, "מספר קבוצה לא חוקי", LENGTH_SHORT).show();
                            editText.setText("");
                        } else {
                            showFilteredTeam(teamNumber);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "תכניס מספר אמיתי", LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void showFilteredTeam(int teamNumber) {
            DataHelper.getInstance().readTeamStats(Integer.toString(teamNumber), new DataHelper.DataCallback<TeamStats>() {
            @Override
            public void onSuccess(TeamStats data) {
                ArrayList<TeamStats> t = new ArrayList<>();
                t.add(data);
                adapter.updateData(t);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "no team number", LENGTH_SHORT);
                adapter.updateData(allTeamsStats);
                adapter.notifyDataSetChanged();
            }
        });

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

    private void uploadDataFromDBToAdapter() {
        DataHelper.getInstance().getUpdatedTeamsStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> data) {
                adapter.updateData(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    private void uploadDataFromAPIToDB() {
        loadTeamsFromAPI();
        for (TeamStats teamStats : allTeamsStats) {
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
        this.editText = findViewById(R.id.editTextTeamFilter);
    }

}