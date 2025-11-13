package com.example.mainapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

public class FormsActivity extends AppCompatActivity {
    private static final String TAG = "FormsActivity";

    private EditText autoL1, autoL2, autoL3, autoL4, teleL1, teleL2, teleL3, teleL4, teleNet, teleProc, teamNumber, gameNumber;
    private RadioGroup group;
    private Button sendBtn;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);
        init();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "=== Send button clicked ===");

                // Validate inputs
                if (teamNumber.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, "ENTER TEAM NUMBER", Toast.LENGTH_LONG).show();
                    return;
                }

                if (gameNumber.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, "ENTER GAME NUMBER", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    int teamNum = getInputFromEditText(teamNumber);
                    int gameNum = getInputFromEditText(gameNumber);

                    Log.d(TAG, "Team Number: " + teamNum + ", Game Number: " + gameNum);

                    TBAApiManager.getInstance().getTeam(teamNum, new TBAApiManager.SingleTeamCallback() {
                        @Override
                        public void onSuccess(Team t) {
                            Log.d(TAG, "TBA API Success: Got team " + t.getTeamNumber());

                            runOnUiThread(() -> {
                                DataHelper.getInstance().readTeamStats(
                                        Integer.toString(t.getTeamNumber()),
                                        new DataHelper.DataCallback<TeamStats>() {
                                            @Override
                                            public void onSuccess(TeamStats data) {
                                                Log.d(TAG, "Read existing TeamStats for team " + t.getTeamNumber());

                                                try {
                                                    // Create new team at game
                                                    TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
                                                    updateGamePieceScored(teamAtGame);

                                                    // Add game to stats
                                                    if (data == null) {
                                                        Log.d(TAG, "Creating new TeamStats");
                                                        data = new TeamStats(t);
                                                    }
                                                    data.addGame(teamAtGame);

                                                    Log.d(TAG, "TeamStats now has " + data.getGamesPlayed() + " games");
                                                    Log.d(TAG, "About to save to Firebase table: " + Constants.TEAMS_TABLE_NAME);
                                                    Log.d(TAG, "Team ID: " + t.getTeamNumber());

                                                    // Save to database
                                                    DataHelper.getInstance().replace(
                                                            Constants.TEAMS_TABLE_NAME,
                                                            Integer.toString(t.getTeamNumber()),
                                                            data,
                                                            new DataHelper.DatabaseCallback() {
                                                                @Override
                                                                public void onSuccess(String id) {
                                                                    Log.d(TAG, "!!! FIREBASE SAVE SUCCESS !!! Team ID: " + id);

                                                                    runOnUiThread(() -> {
                                                                        Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                                                        clearForm();
                                                                        Intent intent = new Intent(FormsActivity.this, TeamStatsActivity.class);

                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {
                                                                    Log.e(TAG, "!!! FIREBASE SAVE FAILED !!! Error: " + error);

                                                                    runOnUiThread(() -> {
                                                                        Toast.makeText(context, "Failed to save: " + error, Toast.LENGTH_LONG).show();
                                                                    });
                                                                }
                                                            }
                                                    );
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error processing data", e);
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(context, "Error processing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.d(TAG, "No existing TeamStats found (this is OK for first game): " + error);
                                                Toast.makeText(context, "Wrong team number", Toast.LENGTH_SHORT);
                                            }
                                        }
                                );
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "TBA API Error", e);
                            runOnUiThread(() -> {
                                Toast.makeText(context, "Failed to fetch team: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Number format error", e);
                    Toast.makeText(context, "Invalid number format", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "General error", e);
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateGamePieceScored(TeamAtGame teamAtGame) {
        Log.d(TAG, "Updating game pieces scored");

        // Auto period
        for (int i = 0; i < getInputFromEditText(autoL1); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L1, true);
        }
        for (int i = 0; i < getInputFromEditText(autoL2); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L2, true);
        }
        for (int i = 0; i < getInputFromEditText(autoL3); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L3, true);
        }
        for (int i = 0; i < getInputFromEditText(autoL4); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L4, true);
        }

        // Teleop period
        for (int i = 0; i < getInputFromEditText(teleL1); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L1, false);
        }
        for (int i = 0; i < getInputFromEditText(teleL2); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L2, false);
        }
        for (int i = 0; i < getInputFromEditText(teleL3); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L3, false);
        }
        for (int i = 0; i < getInputFromEditText(teleL4); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L4, false);
        }
        for (int i = 0; i < getInputFromEditText(teleNet); i++) {
            teamAtGame.addGamePieceScored(GamePiece.NET, false);
        }
        for (int i = 0; i < getInputFromEditText(teleProc); i++) {
            teamAtGame.addGamePieceScored(GamePiece.PROCESSOR, false);
        }
        Log.d(TAG, "ADDED CLIMB: " + checkClimb());
        teamAtGame.addClimb(checkClimb());
    }

    private CLIMB checkClimb(){


        int climb = group.getCheckedRadioButtonId();
        if(climb == -1) return CLIMB.DIDNT_TRY;


        if(climb == R.id.ClimbHigh) return CLIMB.HIGH;
        if(climb == R.id.ClimbLow) return  CLIMB.LOW;
        if(climb == R.id.ClimbFailed) return CLIMB.FAILED;
        return CLIMB.DIDNT_TRY;
    }

    private int getInputFromEditText(EditText et) {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void clearForm() {
        autoL1.setText("");
        autoL2.setText("");
        autoL3.setText("");
        autoL4.setText("");
        teleL1.setText("");
        teleL2.setText("");
        teleL3.setText("");
        teleL4.setText("");
        teleNet.setText("");
        teleProc.setText("");
        teamNumber.setText("");
        gameNumber.setText("");
    }

    private void init() {
        this.autoL1 = findViewById(R.id.AutoL1Count);
        this.autoL2 = findViewById(R.id.AutoL2Count);
        this.autoL3 = findViewById(R.id.AutoL3Count);
        this.autoL4 = findViewById(R.id.AutoL4Count);
        this.teleL1 = findViewById(R.id.TeleopL1Count);
        this.teleL2 = findViewById(R.id.TeleopL2Count);
        this.teleL3 = findViewById(R.id.TeleopL3Count);
        this.teleL4 = findViewById(R.id.TeleopL4Count);
        this.teleProc = findViewById(R.id.TeleopProCount);
        this.teleNet = findViewById(R.id.TeleopNetCount);
        this.teamNumber = findViewById(R.id.TeamNumberEditText);
        this.gameNumber = findViewById(R.id.GameNumberEditText);
        this.sendBtn = findViewById(R.id.buttonSave);
        this.context = FormsActivity.this;

        this.group = findViewById(R.id.ClimbGroup);

        Log.d(TAG, "Forms activity initialized");
    }
}