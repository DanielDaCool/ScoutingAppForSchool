package com.example.mainapp;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.concurrent.atomic.AtomicInteger;

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

        // ADD THESE LINES - Create adapter before loading data
        adapter = new TeamStatsAdapter(allTeamsStats);
        recyclerView.setAdapter(adapter);

        addFilterSearchTeam();

        // Now load data (remove try-catch, method doesn't throw exceptions)
        try {
            uploadDataFromDBToAdapter();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


    private void uploadDataFromDBToAdapter() throws JSONException, IOException {
        allTeamsStats.clear();

        TBAApiManager.getInstance().getEventTeams(Constants.CURRENT_EVENT_ON_APP,
                new TBAApiManager.TeamCallback() {
                    @Override
                    public void onSuccess(ArrayList<Team> teams) {
                        if (teams.isEmpty()) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "No teams found", LENGTH_SHORT).show();
                            });
                            return;
                        }

                        final int totalTeams = teams.size();
                        final AtomicInteger loadedCount = new AtomicInteger(0);

                        for (Team t : teams) {
                            DataHelper.getInstance().getUpdatedTeamStats(t,
                                    new DataHelper.DataCallback<TeamStats>() {
                                        @Override
                                        public void onSuccess(TeamStats data) {
                                            synchronized (allTeamsStats) {
                                                allTeamsStats.add(data);
                                            }

                                            // Check if all teams are loaded
                                            if (loadedCount.incrementAndGet() == totalTeams) {
                                                runOnUiThread(() -> {
                                                    adapter.updateData(allTeamsStats);
                                                    adapter.notifyDataSetChanged();
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Log.e("TeamStatsActivity", "Failed to load team: " + error);

                                            // Still increment counter even on failure
                                            if (loadedCount.incrementAndGet() == totalTeams) {
                                                runOnUiThread(() -> {
                                                    adapter.updateData(allTeamsStats);
                                                    adapter.notifyDataSetChanged();
                                                });
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "Failed to load teams: " + e.getMessage(),
                                    LENGTH_SHORT).show();
                        });
                    }
                });
    }


    private void init() {
        // Get all team games (the flat list of every game played by every team)
        context = TeamStatsActivity.this;
        allTeamsStats = new ArrayList<>();
        teamsAtComp = new ArrayList<>();
        this.editText = findViewById(R.id.editTextTeamFilter);
    }

}