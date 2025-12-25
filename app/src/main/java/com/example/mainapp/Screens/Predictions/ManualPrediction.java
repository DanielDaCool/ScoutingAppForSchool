package com.example.mainapp.Screens.Predictions;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ManualPrediction extends AppCompatActivity {

    // Red Alliance inputs
    private EditText edtRedTeam1, edtRedTeam2, edtRedTeam3;

    // Blue Alliance inputs
    private EditText edtBlueTeam1, edtBlueTeam2, edtBlueTeam3;

    private Button btnCalculate;
    private TextView txtResult;

    private ArrayList<TeamStats> redAllianceStats;
    private ArrayList<TeamStats> blueAllianceStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_prediction);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtRedTeam1 = findViewById(R.id.edtRedTeam1);
        edtRedTeam2 = findViewById(R.id.edtRedTeam2);
        edtRedTeam3 = findViewById(R.id.edtRedTeam3);

        edtBlueTeam1 = findViewById(R.id.edtBlueTeam1);
        edtBlueTeam2 = findViewById(R.id.edtBlueTeam2);
        edtBlueTeam3 = findViewById(R.id.edtBlueTeam3);

        btnCalculate = findViewById(R.id.btnCalculate);
        txtResult = findViewById(R.id.txtResult);

        redAllianceStats = new ArrayList<>();
        blueAllianceStats = new ArrayList<>();
    }

    private void setupListeners() {
        btnCalculate.setOnClickListener(v -> {
            if (validateInputs()) {
                loadTeamStatsAndCalculate();
            }
        });
    }

    private boolean validateInputs() {
        String[] redTeams = {
                edtRedTeam1.getText().toString().trim(),
                edtRedTeam2.getText().toString().trim(),
                edtRedTeam3.getText().toString().trim()
        };

        String[] blueTeams = {
                edtBlueTeam1.getText().toString().trim(),
                edtBlueTeam2.getText().toString().trim(),
                edtBlueTeam3.getText().toString().trim()
        };

        for (String team : redTeams) {
            if (team.isEmpty()) {
                Toast.makeText(this, "אנא הזן את כל מספרי הקבוצות האדומות", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        for (String team : blueTeams) {
            if (team.isEmpty()) {
                Toast.makeText(this, "אנא הזן את כל מספרי הקבוצות הכחולות", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void loadTeamStatsAndCalculate() {
        redAllianceStats.clear();
        blueAllianceStats.clear();

        String[] redTeamNumbers = {
                edtRedTeam1.getText().toString().trim(),
                edtRedTeam2.getText().toString().trim(),
                edtRedTeam3.getText().toString().trim()
        };

        String[] blueTeamNumbers = {
                edtBlueTeam1.getText().toString().trim(),
                edtBlueTeam2.getText().toString().trim(),
                edtBlueTeam3.getText().toString().trim()
        };

        final AtomicInteger loadedCount = new AtomicInteger(0);
        final int totalTeams = 6;

        // Load Red Alliance stats
        for (String teamNumber : redTeamNumbers) {
            DataHelper.getInstance().readTeamStats(teamNumber, new DataHelper.DataCallback<TeamStats>() {
                @Override
                public void onSuccess(TeamStats data) {
                    synchronized (redAllianceStats) {
                        redAllianceStats.add(data);
                    }
                    checkIfAllLoaded(loadedCount, totalTeams);
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(ManualPrediction.this,
                                    "לא נמצאה קבוצה אדומה: " + teamNumber,
                                    Toast.LENGTH_SHORT).show()
                    );
                    checkIfAllLoaded(loadedCount, totalTeams);
                }
            });
        }

        // Load Blue Alliance stats
        for (String teamNumber : blueTeamNumbers) {
            DataHelper.getInstance().readTeamStats(teamNumber, new DataHelper.DataCallback<TeamStats>() {
                @Override
                public void onSuccess(TeamStats data) {
                    synchronized (blueAllianceStats) {
                        blueAllianceStats.add(data);
                    }
                    checkIfAllLoaded(loadedCount, totalTeams);
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(ManualPrediction.this,
                                    "לא נמצאה קבוצה כחולה: " + teamNumber,
                                    Toast.LENGTH_SHORT).show()
                    );
                    checkIfAllLoaded(loadedCount, totalTeams);
                }
            });
        }
    }

    private void checkIfAllLoaded(AtomicInteger loadedCount, int totalTeams) {
        if (loadedCount.incrementAndGet() == totalTeams) {
            runOnUiThread(this::calculatePrediction);
        }
    }

    private void calculatePrediction() {
        if (redAllianceStats.size() != 3 || blueAllianceStats.size() != 3) {
            Toast.makeText(this, "לא כל הקבוצות נמצאו במערכת", Toast.LENGTH_LONG).show();
            return;
        }

        // TODO: Implement your prediction algorithm here
        // This is a simple placeholder calculation
        double redScore = calculateAllianceScore(redAllianceStats);
        double blueScore = calculateAllianceScore(blueAllianceStats);

        String winner = redScore > blueScore ? "הברית האדומה" : "הברית הכחולה";
        String result = String.format(
                "חיזוי:\n%s צפויה לנצח!\n\nניקוד צפוי:\nאדום: %.1f\nכחול: %.1f",
                winner, redScore, blueScore
        );

        txtResult.setText(result);
        txtResult.setVisibility(TextView.VISIBLE);
    }

    private double calculateAllianceScore(ArrayList<TeamStats> allianceStats) {
        // TODO: Implement your actual scoring algorithm
        // This is a placeholder that averages some stats
        double totalScore = 0;

        for (TeamStats stats : allianceStats) {
            // Example: average points per game
            if (stats.getGamesPlayed() > 0) {
                // You'll need to implement proper scoring based on your TeamStats structure
                totalScore += 50; // Placeholder value
            }
        }

        return totalScore;
    }
}