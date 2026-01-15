package com.example.mainapp.Screens;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
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
    private ProgressBar progressBar;

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
                    Toast.makeText(context, "הכנס מספר קבוצה", Toast.LENGTH_LONG).show();
                    return;
                }
                if (getInputFromEditText(teamNumber) < 0 || getInputFromEditText(teamNumber) > 12000) {
                    Toast.makeText(context, "הכנס מספר קבוצה תקין", LENGTH_SHORT).show();
                }

                if (gameNumber.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, "הכנס מספר משחק", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    int teamNum = getInputFromEditText(teamNumber);
                    int gameNum = getInputFromEditText(gameNumber);

                    progressBar.setVisibility(VISIBLE);
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
                                                            Constants.TEAMS_TABLE_NAME,
                                                            Integer.toString(t.getTeamNumber()),
                                                            data,
                                                            new DataHelper.DatabaseCallback() {
                                                                @Override
                                                                public void onSuccess(String id) {

                                                                    runOnUiThread(() -> {
                                                                        progressBar.setVisibility(GONE);
                                                                        clearForm();
                                                                        Toast.makeText(FormsActivity.this, "המידע נשמר בהצלחה", LENGTH_SHORT);

                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {

                                                                    runOnUiThread(() -> {
                                                                        Toast.makeText(context, "שגיאה בשמירת הנתונים", Toast.LENGTH_LONG).show();
                                                                    });
                                                                }
                                                            }
                                                    );
                                                } catch (Exception e) {}
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Toast.makeText(context, "מספר הקבוצה לא תקין", LENGTH_SHORT);
                                            }
                                        }
                                );
                            });
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                } catch (Exception e) {

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
        teamAtGame.setClimb(checkClimb());
    }

    private CLIMB checkClimb() {


        int climb = group.getCheckedRadioButtonId();
        if (climb == -1) return CLIMB.DIDNT_TRY;


        if (climb == R.id.ClimbHigh) return CLIMB.HIGH;
        if (climb == R.id.ClimbLow) return CLIMB.LOW;
        if (climb == R.id.ClimbFailed) return CLIMB.FAILED;
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
        this.progressBar = findViewById(R.id.progressBar);


    }
}