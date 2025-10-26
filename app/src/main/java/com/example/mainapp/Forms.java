package com.example.mainapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.TBAHelpers.TBAApiManager;
import com.example.mainapp.Utils.CLIMB;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DataHelper;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.Team;
import com.example.mainapp.Utils.TeamAtGame;
import com.example.mainapp.Utils.TeamStats;

public class Forms extends AppCompatActivity {
    private EditText autoL1, autoL2, autoL3, autoL4, teleL1, teleL2, teleL3, teleL4, teleNet, teleProc, teamNumber, gameNumber;
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

                    TBAApiManager.getInstance().getTeam(teamNum, new TBAApiManager.SingleTeamCallback() {
                        @Override
                        public void onSuccess(Team t) {
                            runOnUiThread(() -> {
                                DataHelper.getInstance().readTeamStats(
                                        Integer.toString(t.getTeamNumber()),
                                        new DataHelper.DataCallback<TeamStats>() {
                                            @Override
                                            public void onSuccess(TeamStats data) {
                                                try {
                                                    // Create new team at game
                                                    TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
                                                    updateGamePieceScored(teamAtGame);

                                                    // Add game to stats
                                                    if (data == null) {
                                                        data = new TeamStats(t);
                                                    }
                                                    data.addGame(teamAtGame);

                                                    // Save to database
                                                    DataHelper.getInstance().replace(
                                                            Constants.GAMES_TABLE_NAME,
                                                            Integer.toString(t.getTeamNumber()),
                                                            data,
                                                            new DataHelper.DatabaseCallback() {
                                                                @Override
                                                                public void onSuccess(String id) {
                                                                    runOnUiThread(() -> {
                                                                        Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                                                                        clearForm();
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {
                                                                    runOnUiThread(() -> {
                                                                        Toast.makeText(context, "Failed to save: " + error, Toast.LENGTH_LONG).show();
                                                                    });
                                                                }
                                                            }
                                                    );
                                                } catch (Exception e) {
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(context, "Error processing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                runOnUiThread(() -> {
                                                    Toast.makeText(context, "Failed to read team stats: " + error, Toast.LENGTH_LONG).show();
                                                });
                                            }
                                        }
                                );
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(context, "Failed to fetch team: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid number format", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateGamePieceScored(TeamAtGame teamAtGame) {
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

        // Teleop period - FIXED: Now uses correct GamePiece types
        for (int i = 0; i < getInputFromEditText(teleL1); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L1, false);
        }
        for (int i = 0; i < getInputFromEditText(teleL2); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L2, false); // FIXED: was L1
        }
        for (int i = 0; i < getInputFromEditText(teleL3); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L3, false); // FIXED: was L1
        }
        for (int i = 0; i < getInputFromEditText(teleL4); i++) {
            teamAtGame.addGamePieceScored(GamePiece.L4, false); // FIXED: was L1
        }
        for (int i = 0; i < getInputFromEditText(teleNet); i++) {
            teamAtGame.addGamePieceScored(GamePiece.NET, false);
        }
        for (int i = 0; i < getInputFromEditText(teleProc); i++) {
            teamAtGame.addGamePieceScored(GamePiece.PROCESSOR, false);
        }
        teamAtGame.addClimb(checkClimb());
    }
    private CLIMB checkClimb(){

        RadioGroup group = findViewById(R.id.ClimbGroup);
        if(!group.isSelected()) return CLIMB.DIDNT_TRY;
        int climb = group.getCheckedRadioButtonId();

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
        this.context = Forms.this;
    }
}