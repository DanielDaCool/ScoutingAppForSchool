package com.example.mainapp.Screens.Predictions;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class GamePrediction extends AppCompatActivity {
    private ArrayList<Game> allGames;
    private Spinner spinnerGames;
    private TextView redAlliance;
    private TextView blueAlliance;
    private TextView predictionTxt;
    private Button predictBtn;
    private Team[] redAllianceTeams;
    private Team[] blueAllianceTeams;
    private int currentGameID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_prediction);
        init();

    }


    private void init() {
        this.spinnerGames = findViewById(R.id.spinnerGames);
        this.redAlliance = findViewById(R.id.txtRedAlliance);
        this.blueAlliance = findViewById(R.id.txtBlueAlliance);
        this.predictionTxt = findViewById(R.id.txtPredictionResult);
        this.predictBtn = findViewById(R.id.btnPredict);
        this.redAllianceTeams = new Team[3];
        this.blueAllianceTeams = new Team[3];
        this.currentGameID = 0;
        initGames();

        onClickGame();
        onClickButton();
    }


    private void onClickButton() {
        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentGameID == 0) {
                    predictionTxt.setText("בחר משחק לקבלת חיזוי");
                    return;
                }
                predictionTxt.setText("מחשב חיזוי...");
                Game game = allGames.get(currentGameID);
                getAllTeamAverages(game.getRedAlliance(), game.getBlueAlliance(), new AllianceAvgCallback() {
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

                    }
                });

            }

        });

    }

    private void initGames() {
        TBAApiManager.getInstance().getEventGames(Constants.CURRENT_EVENT_ON_APP, new TBAApiManager.GameCallback() {
            @Override
            public void onSuccess(ArrayList<Game> games) {
                allGames = games;

                runOnUiThread(() -> initSpinner());
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void getAllTeamAverages(Team[] redTeams, Team[] blueTeams, AllianceAvgCallback callback) {
        CountDownLatch latch = new CountDownLatch(6);
        final double[] teamAverages = new double[6];

        // Get red alliance averages (indices 0, 1, 2)
        for (int i = 0; i < 3; i++) {
            final int indexRed = i;
            final int indexBlue = i + 3;
            DataHelper.getInstance().getAvgOfTeam(redTeams[i].getTeamNumber(), 1, new DataHelper.DataCallback<Double>() {
                @Override
                public void onSuccess(Double data) {
                    teamAverages[indexRed] = data;
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
                    teamAverages[indexRed] = 0;
                    latch.countDown();
                }
            });


            DataHelper.getInstance().getAvgOfTeam(blueTeams[i].getTeamNumber(), 1, new DataHelper.DataCallback<Double>() {
                @Override
                public void onSuccess(Double data) {
                    teamAverages[indexBlue] = data;
                    latch.countDown();
                }

                @Override
                public void onFailure(String error) {
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

    // Callback interface
    interface AllianceAvgCallback {
        void onSuccess(double redAvg, double blueAvg);

        void onError(Exception e);
    }

    private void initSpinner() {
        ArrayList<String> gameNames = new ArrayList<>();
        gameNames.add("בחר משחק");

        if (allGames != null) {
            for (Game game : allGames) {
                String gameName = game.getGameTitle();
                gameNames.add(gameName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                GamePrediction.this,
                R.layout.simple_spinner_item,
                gameNames
        );
        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spinnerGames.setAdapter(adapter);
    }

    private void onClickGame() {
        spinnerGames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {


                    runOnUiThread(() -> {
                        currentGameID = position - 1;
                        Log.d("GamePrediction", Integer.toString(position));
                        Game wantedGame = allGames.get(position - 1);
                        blueAllianceTeams = wantedGame.getBlueAlliance();
                        redAllianceTeams = wantedGame.getRedAlliance();
                        String redAllianceString = "";
                        String blueAllianceString = "";
                        for (int i = 0; i < 3; i++) {
                            redAllianceString += wantedGame.getRedAlliance()[i].getTeamNumber() + " ";
                            blueAllianceString += wantedGame.getBlueAlliance()[i].getTeamNumber() + " ";
                        }
                        redAlliance.setText(redAllianceString);
                        redAlliance.setTextColor(Color.rgb(255, 0, 0));
                        blueAlliance.setText(blueAllianceString);

                        blueAlliance.setTextColor(Color.rgb(0, 0, 255));
                        predictionTxt.setText("לחץ על חשב חיזוי על מנת לקבל חיזוי על הברית המנצחת");


                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}