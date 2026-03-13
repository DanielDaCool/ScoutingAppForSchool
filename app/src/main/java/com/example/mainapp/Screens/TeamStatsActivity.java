package com.example.mainapp.Screens;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.TeamStatsAdapter;
import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;

public class TeamStatsActivity extends AppCompatActivity {

    private enum SortType {
        TEAM_NUMBER,
        AVG_POINTS,
        AVG_GAME_PIECES
    }

    private Context context;
    private RecyclerView recyclerView;
    private TeamStatsAdapter adapter;
    private EditText editText;
    private Button btnSort;
    private TextView tvSortIndicator;
    private ArrayList<TeamStats> cachedStats;
    private SortType currentSort = SortType.AVG_POINTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);

        init();

        cachedStats = AppCache.getInstance().getAllTeamStats();
        if (cachedStats != null) {
            adapter = new TeamStatsAdapter(cachedStats);
        }
        recyclerView.setAdapter(adapter);

        setupSortButton();
        sortAndRefresh();
        addFilterSearchTeam();

        // Live updates from Firebase
        DataHelper.getInstance().getUpdatedTeamsStats(new DataHelper.DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> fresh) {
                AppCache.getInstance().setAllTeamStats(fresh);
                cachedStats = fresh;
                runOnUiThread(() -> {
                    sortAndRefresh();
                });
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void setupSortButton() {
        btnSort.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("מיין לפי")
                    .setItems(new String[]{
                            "מספר קבוצה",
                            "ממוצע נקודות",
                            "ממוצע חלקי משחק"
                    }, (dialog, which) -> {
                        switch (which) {
                            case 0: currentSort = SortType.TEAM_NUMBER; break;
                            case 1: currentSort = SortType.AVG_POINTS; break;
                            case 2: currentSort = SortType.AVG_GAME_PIECES; break;
                        }
                        sortAndRefresh();
                    })
                    .show();
        });
    }

    private void sortAndRefresh() {
        ArrayList<TeamStats> stats = AppCache.getInstance().getAllTeamStats();
        if (stats == null) return;

        switch (currentSort) {
            case TEAM_NUMBER:
                stats.sort((a, b) ->
                        Integer.compare(a.getTeam().getTeamNumber(), b.getTeam().getTeamNumber()));
                tvSortIndicator.setText("ממוין לפי: מספר קבוצה");
                break;
            case AVG_POINTS:
                stats.sort((a, b) ->
                        Double.compare(b.calculateAvgPoints(), a.calculateAvgPoints()));
                tvSortIndicator.setText("ממוין לפי: ממוצע נקודות");
                break;
            case AVG_GAME_PIECES:
                stats.sort((a, b) ->
                        Double.compare(b.getAvgGamePieceCount(), a.getAvgGamePieceCount()));
                tvSortIndicator.setText("ממוין לפי: ממוצע חלקי משחק");
                break;
        }

        updateUI(stats);
    }

    private void addFilterSearchTeam() {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String input = editText.getText().toString().trim();

                if (input.isEmpty()) {
                    sortAndRefresh();
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
        });
    }

    private void showFilteredTeam(int teamNumber) {
        DataHelper.getInstance().readTeamStats(Integer.toString(teamNumber),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override
                    public void onSuccess(TeamStats data) {
                        ArrayList<TeamStats> filtered = new ArrayList<>();
                        filtered.add(data);
                        runOnUiThread(() -> updateUI(filtered));
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "קבוצה לא נמצאה", LENGTH_SHORT).show();
                            sortAndRefresh();
                        });
                    }
                }
        );
    }

    private void updateUI(ArrayList<TeamStats> newStats) {
        adapter.updateData(newStats);
        adapter.notifyDataSetChanged();
    }

    private void init() {
        context = TeamStatsActivity.this;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        editText = findViewById(R.id.editTextTeamFilter);
        btnSort = findViewById(R.id.btnSort);
        tvSortIndicator = findViewById(R.id.tvSortIndicator);
    }
}