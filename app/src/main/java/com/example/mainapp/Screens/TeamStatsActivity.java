package com.example.mainapp.Screens;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
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
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
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
    ArrayList<TeamStats> cachedStats;

    private ValueEventListener teamStatsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        init();

        cachedStats = AppCache.getInstance().getAllTeamStats();
        if(cachedStats != null){

            adapter = new TeamStatsAdapter(cachedStats);

        }
        recyclerView.setAdapter(adapter);

        addFilterSearchTeam();
        DataHelper.getInstance().getUpdatedTeamsStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> fresh) {
                AppCache.getInstance().setAllTeamStats(fresh); // keep cache in sync
                runOnUiThread(() -> updateUI(fresh));
            }
            @Override
            public void onFailure(String error) {}
        });


    }

    private void addFilterSearchTeam() {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String input = editText.getText().toString().trim();

                    if (input.isEmpty()) {
                        updateUI(cachedStats);
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
                updateUI(t);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "no team number", LENGTH_SHORT);
                updateUI(cachedStats);
            }
        });

    }

    private void updateUI(ArrayList<TeamStats> newStats){
        adapter.updateData(newStats);
        adapter.notifyDataSetChanged();


    }

    private void init() {
        // Get all team games (the flat list of every game played by every team)
        context = TeamStatsActivity.this;
        this.editText = findViewById(R.id.editTextTeamFilter);
    }

}