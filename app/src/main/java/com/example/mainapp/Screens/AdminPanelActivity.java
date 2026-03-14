package com.example.mainapp.Screens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.ScouterAdapter;
import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

import java.util.ArrayList;

public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView rvScouters;
    private ScouterAdapter scouterAdapter;
    private ArrayList<User> scouterList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvBack, tvEmpty;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        context = this;
        init();

        tvBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScouters();
    }

    // ==================== LOAD SCOUTERS ====================

    private void loadScouters() {
        progressBar.setVisibility(View.VISIBLE);

        DataHelper.getInstance().getAllUsers(new DataHelper.DataCallback<ArrayList<User>>() {
            @Override
            public void onSuccess(ArrayList<User> users) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    scouterList.clear();

                    for (User u : users) {
                        if (!u.isAdmin()) scouterList.add(u);
                    }

                    scouterAdapter.updateData(scouterList);
                    tvEmpty.setVisibility(scouterList.isEmpty() ? View.VISIBLE : View.GONE);

                    // Start live listeners for each scouter's pending count
                    loadPendingCounts();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Attaches a live listener for each scouter's pending assignments.
     * Count updates automatically when assignments are added or completed.
     */
    private void loadPendingCounts() {
        for (User scouter : scouterList) {
            DataHelper.getInstance().listenToPendingAssignments(
                    scouter.getUserId(),
                    new DataHelper.DataCallback<ArrayList<Assignment>>() {
                        @Override
                        public void onSuccess(ArrayList<Assignment> assignments) {
                            runOnUiThread(() ->
                                    scouterAdapter.setPendingCount(
                                            scouter.getUserId(),
                                            assignments.size()
                                    )
                            );
                        }
                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() ->
                                    scouterAdapter.setPendingCount(scouter.getUserId(), 0)
                            );
                        }
                    }
            );
        }
    }

    // ==================== ASSIGN DIALOG ====================

    private void showAssignDialog(User scouter) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_assign, null);

        EditText etGame = dialogView.findViewById(R.id.etAssignGame);
        EditText etTeam = dialogView.findViewById(R.id.etAssignTeam);

        new AlertDialog.Builder(context, R.style.DarkAlertDialog)
                .setTitle("הקצה משימה ל-" + scouter.getFullName())
                .setView(dialogView)
                .setPositiveButton("הקצה", (dialog, which) -> {
                    String gameStr = etGame.getText().toString().trim();
                    String teamStr = etTeam.getText().toString().trim();

                    if (gameStr.isEmpty() || teamStr.isEmpty()) {
                        Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int gameNumber = Integer.parseInt(gameStr);
                        int teamNumber = Integer.parseInt(teamStr);

                        if (!TeamUtils.containsTeam(AppCache.getInstance().getTeamsAtEvent(), teamNumber)) {
                            Toast.makeText(context, "הכנס קבוצה שמתחרה בתחרות", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!TeamUtils.containsTeam(
                                AppCache.getInstance().getGamesList().get(gameNumber - 1).getPlayingTeamsNumbers(),
                                teamNumber)) {
                            Toast.makeText(context, "הכנס משחק שהקבוצה משחקת בו", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveAssignment(scouter, new Assignment(gameNumber, teamNumber));
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "הכנס מספרים בלבד", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void saveAssignment(User scouter, Assignment assignment) {
        DataHelper.getInstance().saveAssignment(scouter.getUserId(), assignment,
                new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        runOnUiThread(() ->
                                        Toast.makeText(context,
                                                "משימה הוקצתה ל-" + scouter.getFullName(),
                                                Toast.LENGTH_SHORT).show()
                                // No need to call loadPendingCounts() —
                                // the live listener fires automatically
                        );
                    }
                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }

    // ==================== INIT ====================

    private void init() {
        tvBack      = findViewById(R.id.tvBackBtn);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty     = findViewById(R.id.tvEmpty);
        rvScouters  = findViewById(R.id.rvScouters);

        rvScouters.setLayoutManager(new LinearLayoutManager(context));
        scouterAdapter = new ScouterAdapter(scouterList);
        rvScouters.setAdapter(scouterAdapter);

        scouterAdapter.setOnScouterClickListener(scouter ->
                showAssignDialog(scouter)
        );
    }
}