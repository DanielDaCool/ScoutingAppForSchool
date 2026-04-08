package com.example.mainapp.Screens.AdminScreens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainapp.Adapters.ScouterAdapter;
import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.DatabaseUtils.User;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminPanelActivity extends AppCompatActivity {

    private RecyclerView   rvScouters;
    private ScouterAdapter scouterAdapter;
    private ArrayList<User> scouterList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView    tvBack, tvEmpty;
    private Context     context;

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

    private void loadScouters() {
        progressBar.setVisibility(View.VISIBLE);
        DataHelper.getInstance().getAllUsers(new DataHelper.DataCallback<ArrayList<User>>() {
            @Override public void onSuccess(ArrayList<User> users) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    scouterList.clear();
                    for (User u : users) if (!u.isAdmin()) scouterList.add(u);
                    scouterAdapter.updateData(scouterList);
                    tvEmpty.setVisibility(scouterList.isEmpty() ? View.VISIBLE : View.GONE);
                    loadPendingCounts();
                });
            }
            @Override public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * One-time read across all districts per scouter.
     * Using getPendingAssignments (not a live listener) to avoid
     * listener accumulation on repeated onResume calls.
     */
    private void loadPendingCounts() {
        EVENTS[] districts = EVENTS.values();
        for (User scouter : scouterList) {
            AtomicInteger total   = new AtomicInteger(0);
            AtomicInteger checked = new AtomicInteger(0);
            for (EVENTS district : districts) {
                DataHelper.getInstance().getPendingAssignments(
                        scouter.getUserId(), district,
                        new DataHelper.DataCallback<ArrayList<Assignment>>() {
                            @Override public void onSuccess(ArrayList<Assignment> list) {
                                total.addAndGet(list.size());
                                if (checked.incrementAndGet() == districts.length)
                                    runOnUiThread(() -> scouterAdapter.setPendingCount(
                                            scouter.getUserId(), total.get()));
                            }
                            @Override public void onFailure(String error) {
                                if (checked.incrementAndGet() == districts.length)
                                    runOnUiThread(() -> scouterAdapter.setPendingCount(
                                            scouter.getUserId(), total.get()));
                            }
                        }
                );
            }
        }
    }

    private void showAssignDialog(User scouter) {
        View     dialogView = getLayoutInflater().inflate(R.layout.dialog_assign, null);
        EditText etGame     = dialogView.findViewById(R.id.etAssignGame);
        EditText etTeam     = dialogView.findViewById(R.id.etAssignTeam);
        Spinner  spDistrict = dialogView.findViewById(R.id.spDistrict);

        EVENTS[] events     = EVENTS.values();
        String[] eventNames = new String[events.length];
        for (int i = 0; i < events.length; i++) eventNames[i] = events[i].toString();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, eventNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(adapter);

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
                        int    gameNumber     = Integer.parseInt(gameStr);
                        int    teamNumber     = Integer.parseInt(teamStr);
                        EVENTS chosenDistrict = events[spDistrict.getSelectedItemPosition()];

                        if (!TeamUtils.containsTeam(AppCache.getInstance().getTeamsAtEvent(), teamNumber)) {
                            Toast.makeText(context, "הכנס קבוצה שמתחרה בתחרות", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        saveAssignment(scouter, new Assignment(gameNumber, teamNumber), chosenDistrict);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "הכנס מספרים בלבד", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void saveAssignment(User scouter, Assignment assignment, EVENTS district) {
        DataHelper.getInstance().saveAssignment(scouter.getUserId(), district, assignment,
                new DataHelper.DatabaseCallback() {
                    @Override public void onSuccess(String id) {
                        runOnUiThread(() -> {
                            Toast.makeText(context,
                                    "משימה הוקצתה ל-" + scouter.getFullName() + " | " + district,
                                    Toast.LENGTH_SHORT).show();
                            loadPendingCounts(); // refresh counts
                        });
                    }
                    @Override public void onFailure(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(context, "שגיאה: " + error, Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private void init() {
        tvBack      = findViewById(R.id.tvBackBtn);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty     = findViewById(R.id.tvEmpty);
        rvScouters  = findViewById(R.id.rvScouters);
        rvScouters.setLayoutManager(new LinearLayoutManager(context));
        scouterAdapter = new ScouterAdapter(scouterList);
        rvScouters.setAdapter(scouterAdapter);
        scouterAdapter.setOnScouterClickListener(scouter -> showAssignDialog(scouter));
    }
}