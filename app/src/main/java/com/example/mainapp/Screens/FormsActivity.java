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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mainapp.R;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.LocalDatabase;
import com.example.mainapp.Utils.OfflineFormData;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class FormsActivity extends AppCompatActivity {

    private EditText autoL1, autoL2, autoL3, autoL4;
    private EditText teleL1, teleL2, teleL3, teleL4, teleNet, teleProc;
    private EditText teamNumber, gameNumber;
    private RadioGroup group;
    private Button sendBtn;
    private Context context;
    private ProgressBar progressBar;

    private int teamNumberValue = 0;
    private int gameNumberValue = 0;
    private String assignmentKey = null;

    // Show offline warning only once per session
    private static boolean offlineWarningShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);
        init();

        if (getIntent().getExtras() != null) {
            teamNumberValue = (Integer) getIntent().getExtras().get("teamNumber");
            gameNumberValue = (Integer) getIntent().getExtras().get("gameNumber");
            assignmentKey   = getIntent().getStringExtra("assignmentKey");
        }

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

        // Show offline warning once per session
        if (!InternetUtils.isInternetConnected(context) && !offlineWarningShown) {
            offlineWarningShown = true;
            new AlertDialog.Builder(context)
                    .setTitle("אין חיבור לאינטרנט")
                    .setMessage("הנתונים יישמרו מקומית ויסונכרנו אוטומטית כשתחזור לאינטרנט")
                    .setPositiveButton("הבנתי", null)
                    .show();
        }

        sendBtn.setOnClickListener(view -> handleSendBtnClick());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        offlineWarningShown = false;
    }

    // ==================== BUTTON CLICK ====================

    private void handleSendBtnClick() {
        if (teamNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר קבוצה", Toast.LENGTH_LONG).show();
            return;
        }
        if (getInputFromEditText(teamNumber) < 0 || getInputFromEditText(teamNumber) > 12000) {
            Toast.makeText(context, "הכנס מספר קבוצה תקין", LENGTH_SHORT).show();
            return;
        }
        if (!TeamUtils.containsTeam(AppCache.getInstance().getTeamAtEvent(),
                getInputFromEditText(teamNumber))) {
            Toast.makeText(context, "הכנס קבוצה שמתחרה בתחרות שבחרת", LENGTH_SHORT).show();
            return;
        }
        if (gameNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר משחק", Toast.LENGTH_LONG).show();
            return;
        }

        int teamNum = getInputFromEditText(teamNumber);
        int gameNum = getInputFromEditText(gameNumber);

        Team t = TeamUtils.getTeamFromArray(AppCache.getInstance().getTeamAtEvent(), teamNum);
        if (t == null) {
            showError("קבוצה לא נמצאה");
            return;
        }

        progressBar.setVisibility(VISIBLE);
        sendBtn.setEnabled(false);

        if (InternetUtils.isInternetConnected(context)) {
            fetchStatsAndSave(t, gameNum);
        } else {
            saveLocally(t, gameNum);
        }
    }

    // ==================== ONLINE FLOW ====================

    private void fetchStatsAndSave(Team t, int gameNum) {
        DataHelper.getInstance().readTeamStats(
                Integer.toString(t.getTeamNumber()),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override
                    public void onSuccess(TeamStats data) {
                        if (data == null) data = new TeamStats(t);
                        saveGameData(t, gameNum, data);
                    }
                    @Override
                    public void onFailure(String error) {
                        saveGameData(t, gameNum, new TeamStats(t));
                    }
                }
        );
    }

    private void saveGameData(Team t, int gameNum, TeamStats stats) {
        TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
        updateGamePieceScored(teamAtGame);
        stats.addGame(teamAtGame);

        DataHelper.getInstance().replace(
                Constants.TEAMS_TABLE_NAME,
                Integer.toString(t.getTeamNumber()),
                stats,
                new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        if (assignmentKey != null) {
                            String userId = SharedPrefHelper.getInstance(context).getUserId();
                            Assignment assignment = new Assignment(gameNum, t.getTeamNumber());
                            DataHelper.getInstance().completeAssignment(userId, assignment,
                                    new DataHelper.DatabaseCallback() {
                                        @Override
                                        public void onSuccess(String id) {
                                            runOnUiThread(() -> { onSaveSuccess(); finish(); });
                                        }
                                        @Override
                                        public void onFailure(String error) {
                                            runOnUiThread(() -> { onSaveSuccess(); finish(); });
                                        }
                                    }
                            );
                        } else {
                            runOnUiThread(() -> onSaveSuccess());
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                        showError("שגיאה בשמירת הנתונים: " + error);
                    }
                }
        );
    }

    // ==================== OFFLINE FLOW ====================

    private void saveLocally(Team t, int gameNum) {
        new Thread(() -> {
            try {
                TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
                updateGamePieceScored(teamAtGame);

                String piecesJson = serializeGamePieces(teamAtGame);
                String climbName  = teamAtGame.getClimb().name();

                OfflineFormData formData = new OfflineFormData(
                        t.getTeamNumber(),
                        gameNum,
                        piecesJson,
                        climbName,
                        System.currentTimeMillis()
                );

                long rowId = LocalDatabase.getInstance(context).saveForm(formData);
                Log.d("OFFLINE", "Saved locally — rowId: " + rowId
                        + " team: " + t.getTeamNumber()
                        + " game: " + gameNum);
                runOnUiThread(() -> {
                    if (rowId != -1) {
                        onOfflineSaveSuccess();
                    } else {
                        showError("שגיאה בשמירה מקומית");
                    }
                });
            } catch (Exception e) {
                showError("שגיאה בשמירה מקומית: " + e.getMessage());
            }
        }).start();
    }

    private String serializeGamePieces(TeamAtGame teamAtGame) throws Exception {
        JSONArray array = new JSONArray();
        for (TeamAtGame.GamePieceScore score : teamAtGame.getGamePiecesScored()) {
            JSONObject obj = new JSONObject();
            obj.put("piece", score.getPiece());
            obj.put("inAuto", score.isInAuto());
            array.put(obj);
        }
        return array.toString();
    }

    // ==================== SUCCESS HANDLERS ====================

    private void onSaveSuccess() {
        progressBar.setVisibility(GONE);
        sendBtn.setEnabled(true);
        clearForm();
        Toast.makeText(this, "המידע נשמר בהצלחה ✓", LENGTH_SHORT).show();
        AppCache.getInstance().setTotalGames(AppCache.getInstance().getTotalGames() + 1);
    }

    private void onOfflineSaveSuccess() {
        progressBar.setVisibility(GONE);
        sendBtn.setEnabled(true);
        clearForm();
        Toast.makeText(this,
                "נשמר מקומית ✓ — יסונכרן כשתחזור לאינטרנט",
                Toast.LENGTH_LONG).show();
        AppCache.getInstance().setTotalGames(AppCache.getInstance().getTotalGames() + 1);
        if (assignmentKey != null) finish();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(GONE);
            sendBtn.setEnabled(true);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        });
    }

    // ==================== HELPERS ====================

    private void updateGamePieceScored(TeamAtGame teamAtGame) {
        for (int i = 0; i < getInputFromEditText(autoL1); i++)
            teamAtGame.addGamePieceScored(GamePiece.L1, true);
        for (int i = 0; i < getInputFromEditText(autoL2); i++)
            teamAtGame.addGamePieceScored(GamePiece.L2, true);
        for (int i = 0; i < getInputFromEditText(autoL3); i++)
            teamAtGame.addGamePieceScored(GamePiece.L3, true);
        for (int i = 0; i < getInputFromEditText(autoL4); i++)
            teamAtGame.addGamePieceScored(GamePiece.L4, true);
        for (int i = 0; i < getInputFromEditText(teleL1); i++)
            teamAtGame.addGamePieceScored(GamePiece.L1, false);
        for (int i = 0; i < getInputFromEditText(teleL2); i++)
            teamAtGame.addGamePieceScored(GamePiece.L2, false);
        for (int i = 0; i < getInputFromEditText(teleL3); i++)
            teamAtGame.addGamePieceScored(GamePiece.L3, false);
        for (int i = 0; i < getInputFromEditText(teleL4); i++)
            teamAtGame.addGamePieceScored(GamePiece.L4, false);
        for (int i = 0; i < getInputFromEditText(teleNet); i++)
            teamAtGame.addGamePieceScored(GamePiece.NET, false);
        for (int i = 0; i < getInputFromEditText(teleProc); i++)
            teamAtGame.addGamePieceScored(GamePiece.PROCESSOR, false);
        teamAtGame.setClimb(checkClimb());
    }

    private CLIMB checkClimb() {
        int climb = group.getCheckedRadioButtonId();
        if (climb == -1)               return CLIMB.DIDNT_TRY;
        if (climb == R.id.ClimbHigh)   return CLIMB.HIGH;
        if (climb == R.id.ClimbLow)    return CLIMB.LOW;
        if (climb == R.id.ClimbFailed) return CLIMB.FAILED;
        return CLIMB.DIDNT_TRY;
    }

    private int getInputFromEditText(EditText et) {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void clearForm() {
        autoL1.setText(""); autoL2.setText("");
        autoL3.setText(""); autoL4.setText("");
        teleL1.setText(""); teleL2.setText("");
        teleL3.setText(""); teleL4.setText("");
        teleNet.setText(""); teleProc.setText("");
        if (assignmentKey == null) {
            teamNumber.setText("");
            gameNumber.setText("");
        }
        group.clearCheck();
    }

    // ==================== INIT ====================

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