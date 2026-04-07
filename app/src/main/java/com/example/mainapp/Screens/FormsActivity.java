package com.example.mainapp.Screens;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

public class FormsActivity extends AppCompatActivity {

    private EditText autoL1, autoL2, autoL3, autoL4;
    private EditText teleL1, teleL2, teleL3, teleL4, teleNet, teleProc;
    private EditText teamNumber, gameNumber;
    private RadioGroup group;
    private Button sendBtn;
    private Context context;
    private ProgressBar progressBar;

    private int    teamNumberValue    = 0;
    private int    gameNumberValue    = 0;
    private String assignmentKey      = null;
    private EVENTS assignmentDistrict = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);
        init();

        if (getIntent().getExtras() != null) {
            teamNumberValue = (Integer) getIntent().getExtras().get("teamNumber");
            gameNumberValue = (Integer) getIntent().getExtras().get("gameNumber");
            assignmentKey   = getIntent().getStringExtra("assignmentKey");

            // Read district from Intent so we complete the right district's assignment
            String districtKey = getIntent().getStringExtra("districtKey");
            if (districtKey != null) {
                for (EVENTS e : EVENTS.values()) {
                    if (e.getEventKey().equals(districtKey)) { assignmentDistrict = e; break; }
                }
            }
        }
        if (assignmentDistrict == null)
            assignmentDistrict = SharedPrefHelper.getInstance(context).getCurrentDistrict();

        if (teamNumberValue != 0) {
            teamNumber.setText(String.valueOf(teamNumberValue));
            teamNumber.setEnabled(false);
            teamNumber.setAlpha(0.6f);
        }
        if (gameNumberValue != 0) {
            gameNumber.setText(String.valueOf(gameNumberValue));
            gameNumber.setEnabled(false);
            gameNumber.setAlpha(0.6f);
        }

        sendBtn.setOnClickListener(v -> handleSendBtnClick());
    }

    private void handleSendBtnClick() {
        if (teamNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר קבוצה", Toast.LENGTH_LONG).show(); return;
        }
        int teamNum = getInput(teamNumber);
        if (teamNum < 0 || teamNum > 12000) {
            Toast.makeText(context, "הכנס מספר קבוצה תקין", LENGTH_SHORT).show(); return;
        }

        // Null-safe team validation — skip if cache empty
        Team[] teamsAtEvent = AppCache.getInstance().getTeamsAtEvent();
        if (teamsAtEvent != null && !TeamUtils.containsTeam(teamsAtEvent, teamNum)) {
            Toast.makeText(context, "הכנס קבוצה שמתחרה בתחרות שבחרת", LENGTH_SHORT).show(); return;
        }

        if (gameNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר משחק", Toast.LENGTH_LONG).show(); return;
        }
        int gameNum = getInput(gameNumber);

        Team t = null;
        if (teamsAtEvent != null) t = TeamUtils.getTeamFromArray(teamsAtEvent, teamNum);
        if (t == null) t = new Team(teamNum, "Team " + teamNum);

        progressBar.setVisibility(VISIBLE);
        sendBtn.setEnabled(false);

        TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
        updateGamePieces(teamAtGame);
        saveToFirebase(t, gameNum, teamAtGame);
    }

    private void saveToFirebase(Team t, int gameNum, TeamAtGame teamAtGame) {
        DataHelper.getInstance().readTeamStats(Integer.toString(t.getTeamNumber()),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override public void onSuccess(TeamStats data) {
                        if (data == null) data = new TeamStats(t);
                        persistToFirebase(t, gameNum, teamAtGame, data);
                    }
                    @Override public void onFailure(String error) {
                        persistToFirebase(t, gameNum, teamAtGame, new TeamStats(t));
                    }
                }
        );
    }

    private void persistToFirebase(Team t, int gameNum, TeamAtGame teamAtGame, TeamStats stats) {
        stats.addGame(teamAtGame);
        DataHelper.getInstance().replace(
                Constants.TEAMS_TABLE_NAME, Integer.toString(t.getTeamNumber()), stats,
                new DataHelper.DatabaseCallback() {
                    @Override public void onSuccess(String id) {
                        if (assignmentKey != null) completeAssignment(t, gameNum);
                        else runOnUiThread(() -> onSaveSuccess());
                    }
                    @Override public void onFailure(String error) {
                        runOnUiThread(() -> onSaveSuccess());
                    }
                }
        );
    }

    private void completeAssignment(Team t, int gameNum) {
        String userId = SharedPrefHelper.getInstance(context).getUserId();
        if (assignmentDistrict == null) { runOnUiThread(() -> onSaveSuccess()); return; }
        DataHelper.getInstance().completeAssignment(
                userId, assignmentDistrict, new Assignment(gameNum, t.getTeamNumber()),
                new DataHelper.DatabaseCallback() {
                    @Override public void onSuccess(String id) { runOnUiThread(() -> onSaveSuccess()); }
                    @Override public void onFailure(String error) { runOnUiThread(() -> onSaveSuccess()); }
                }
        );
    }

    private void onSaveSuccess() {
        progressBar.setVisibility(GONE);
        sendBtn.setEnabled(true);
        clearForm();
        Toast.makeText(this, "המידע נשמר בהצלחה ✓", LENGTH_SHORT).show();
        AppCache.getInstance().setTotalGames(AppCache.getInstance().getTotalGames() + 1);
        if (assignmentKey != null) finish();
    }

    private void updateGamePieces(TeamAtGame tg) {
        for (int i = 0; i < getInput(autoL1); i++) tg.addGamePieceScored(GamePiece.L1, true);
        for (int i = 0; i < getInput(autoL2); i++) tg.addGamePieceScored(GamePiece.L2, true);
        for (int i = 0; i < getInput(autoL3); i++) tg.addGamePieceScored(GamePiece.L3, true);
        for (int i = 0; i < getInput(autoL4); i++) tg.addGamePieceScored(GamePiece.L4, true);
        for (int i = 0; i < getInput(teleL1); i++) tg.addGamePieceScored(GamePiece.L1, false);
        for (int i = 0; i < getInput(teleL2); i++) tg.addGamePieceScored(GamePiece.L2, false);
        for (int i = 0; i < getInput(teleL3); i++) tg.addGamePieceScored(GamePiece.L3, false);
        for (int i = 0; i < getInput(teleL4); i++) tg.addGamePieceScored(GamePiece.L4, false);
        for (int i = 0; i < getInput(teleNet); i++) tg.addGamePieceScored(GamePiece.NET, false);
        for (int i = 0; i < getInput(teleProc); i++) tg.addGamePieceScored(GamePiece.PROCESSOR, false);
        tg.setClimb(checkClimb());
    }

    private CLIMB checkClimb() {
        int id = group.getCheckedRadioButtonId();
        if (id == R.id.ClimbHigh)   return CLIMB.HIGH;
        if (id == R.id.ClimbLow)    return CLIMB.LOW;
        if (id == R.id.ClimbFailed) return CLIMB.FAILED;
        return CLIMB.DIDNT_TRY;
    }

    private int getInput(EditText et) {
        try { return Integer.parseInt(et.getText().toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void clearForm() {
        autoL1.setText(""); autoL2.setText(""); autoL3.setText(""); autoL4.setText("");
        teleL1.setText(""); teleL2.setText(""); teleL3.setText(""); teleL4.setText("");
        teleNet.setText(""); teleProc.setText("");
        if (assignmentKey == null) { teamNumber.setText(""); gameNumber.setText(""); }
        group.clearCheck();
    }

    private void init() {
        autoL1      = findViewById(R.id.AutoL1Count);
        autoL2      = findViewById(R.id.AutoL2Count);
        autoL3      = findViewById(R.id.AutoL3Count);
        autoL4      = findViewById(R.id.AutoL4Count);
        teleL1      = findViewById(R.id.TeleopL1Count);
        teleL2      = findViewById(R.id.TeleopL2Count);
        teleL3      = findViewById(R.id.TeleopL3Count);
        teleL4      = findViewById(R.id.TeleopL4Count);
        teleProc    = findViewById(R.id.TeleopProCount);
        teleNet     = findViewById(R.id.TeleopNetCount);
        teamNumber  = findViewById(R.id.TeamNumberEditText);
        gameNumber  = findViewById(R.id.GameNumberEditText);
        sendBtn     = findViewById(R.id.buttonSave);
        context     = FormsActivity.this;
        group       = findViewById(R.id.ClimbGroup);
        progressBar = findViewById(R.id.progressBar);
    }
}