package com.example.mainapp.Screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;

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

                    // Filter out admins — only show scouters
                    for (User u : users) {
                        if (!u.isAdmin()) scouterList.add(u);
                    }

                    scouterAdapter.updateData(scouterList);
                    tvEmpty.setVisibility(scouterList.isEmpty() ? View.VISIBLE : View.GONE);

                    // Load pending count for each scouter
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

    private void loadPendingCounts() {
        for (User scouter : scouterList) {
            DataHelper.getInstance().getPendingAssignments(scouter.getUserId(),
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
                        Assignment assignment = new Assignment(gameNumber, teamNumber);
                        saveAssignment(scouter, assignment);
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
                        runOnUiThread(() -> {
                            Toast.makeText(context,
                                    "משימה הוקצתה ל-" + scouter.getFullName(),
                                    Toast.LENGTH_SHORT).show();
                            // Reload counts to reflect the new assignment
                            loadPendingCounts();
                        });
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