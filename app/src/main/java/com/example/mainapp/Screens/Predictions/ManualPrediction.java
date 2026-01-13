package com.example.mainapp.Screens.Predictions;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ManualPrediction extends AppCompatActivity {

    // Red Alliance inputs
    private EditText edtRedTeam1, edtRedTeam2, edtRedTeam3;

    // Blue Alliance inputs
    private EditText edtBlueTeam1, edtBlueTeam2, edtBlueTeam3;

    private Button btnCalculate;
    private TextView predictionTxt;

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
        predictionTxt = findViewById(R.id.txtResult);

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

    private void getAllTeamAverages(String[] redTeams, String[] blueTeams, GamePrediction.AllianceAvgCallback callback) {

        CountDownLatch latch = new CountDownLatch(6);
        final double[] teamAverages = new double[6];

        // Get red alliance averages (indices 0, 1, 2)
        for (int i = 0; i < 3; i++) {
            final int indexRed = i;
            final int indexBlue = i + 3;

             DataHelper.getInstance().getAvgOfTeam(redTeams[i], 1, new DataHelper.DataCallback<Double>() {
                @Override
                public void onSuccess(Double data) {
                    teamAverages[indexRed] = data;
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ManualPrediction", "Red team " + indexRed + " failed: " + error);
                    teamAverages[indexRed] = 0;
                    latch.countDown();
                }
            });

           DataHelper.getInstance().getAvgOfTeam(blueTeams[i], 1, new DataHelper.DataCallback<Double>() {
                @Override
                public void onSuccess(Double data) {
                    teamAverages[indexBlue] = data;
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ManualPrediction", "Blue team " + indexBlue + " failed: " + error);
                    teamAverages[indexBlue] = 0;
                    latch.countDown();
                }
            });
        }

        // Wait in background thread
        new Thread(() -> {
            try {
                latch.await();

                // Calculate sums
                double redSum = (teamAverages[0] + teamAverages[1] + teamAverages[2]) / 3.0;
                double blueSum = (teamAverages[3] + teamAverages[4] + teamAverages[5]) / 3.0;


                // Return results via callback
                callback.onSuccess(redSum, blueSum);

            } catch (InterruptedException e) {
               callback.onError(e);
            }
        }).start();
    }

    private void loadTeamStatsAndCalculate() {

        // Show loading
        predictionTxt.setText("מחשב חיזוי...");
        predictionTxt.setTextColor(Color.BLACK);
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

        getAllTeamAverages(redTeamNumbers, blueTeamNumbers, new GamePrediction.AllianceAvgCallback() {
            @Override
            public void onSuccess(double redAvg, double blueAvg) {
                runOnUiThread(() -> {

                    String prediction;

                    if (redAvg > blueAvg) {
                        prediction = "חיזוי: הברית האדומה תנצח!\n";
                        predictionTxt.setTextColor(Color.rgb(255, 0, 0));
                    } else if (blueAvg > redAvg) {
                        prediction = "חיזוי: הברית הכחולה תנצח!\n";
                        predictionTxt.setTextColor(Color.rgb(0, 0, 255));
                    } else {
                        prediction = "חיזוי: תיקו!\n";
                        predictionTxt.setTextColor(Color.BLACK);
                    }
                    prediction += String.format("אדום: %.2f | כחול: %.2f", redAvg, blueAvg);

                    predictionTxt.setText(prediction);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.d("ManualPrediction", "error: " + e.toString());
            }
        });
    }


}