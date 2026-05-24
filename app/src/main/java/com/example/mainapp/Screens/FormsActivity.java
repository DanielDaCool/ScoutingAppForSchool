package com.example.mainapp.Screens;

import static android.app.ProgressDialog.show;
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
import com.example.mainapp.Utils.DatabaseUtils.Climb;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.GamePiece;
import com.example.mainapp.Utils.InternetUtils;
import com.example.mainapp.Utils.SharedPrefHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

/**
 * FormsActivity is the scouting data entry screen.
 * Scouters use this form to record a team's performance during a match, including
 * autonomous and teleop scoring across different levels, and end-game climb status.
 */
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
    private EVENTS assignmentDistrict = null;

    @Override
/**
 * Initializes the activity and prepares the screen components and data.
 * @param savedInstanceState parameter required for this method.
 */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);


        init();

        if (getIntent().getExtras() != null) {
            teamNumberValue = getIntent().getIntExtra("teamNumber", 0);
            gameNumberValue = getIntent().getIntExtra("gameNumber", 0);
            assignmentKey = getIntent().getStringExtra("assignmentKey");

            // Read district from Intent so we complete the right district's assignment
            String districtKey = getIntent().getStringExtra("districtKey");
            if (districtKey != null) {
                for (EVENTS e : EVENTS.values()) {
                    if (e.getEventKey().equals(districtKey)) {
                        assignmentDistrict = e;
                        break;
                    }
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


    /**
     * Handles the click event for the send button.
     * Validates the input fields (team number, game number, scoring counts)
     * before initiating the save process.
     */
    private void handleSendBtnClick() {
        if (teamNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר קבוצה", Toast.LENGTH_LONG).show();
            return;
        }
        int teamNum = getInput(teamNumber);
        if (teamNum < 0 || teamNum > 12000) {
            Toast.makeText(context, "הכנס מספר קבוצה תקין", LENGTH_SHORT).show();
            return;
        }

        // Null-safe team validation — skip if cache empty
        Team[] teamsAtEvent = AppCache.getInstance().getTeamsAtEvent();
        if (teamsAtEvent != null && !TeamUtils.containsTeam(teamsAtEvent, teamNum)) {
            Toast.makeText(context, "הכנס קבוצה שמתחרה בתחרות שבחרת", LENGTH_SHORT).show();
            return;
        }

        if (gameNumber.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "הכנס מספר משחק", Toast.LENGTH_LONG).show();
            return;
        }
        int gameNum = getInput(gameNumber);

        Team t = null;
        if (teamsAtEvent != null) t = TeamUtils.getTeamFromArray(teamsAtEvent, teamNum);
        if (t == null) t = new Team(teamNum, "Team " + teamNum);



        TeamAtGame teamAtGame = new TeamAtGame(t, gameNum);
        if (updateGamePieces(teamAtGame)) {
            if (!InternetUtils.isInternetConnected(context)) {
                Toast.makeText(context, "אין חיבור לאינטרנט. המידע לא יישמר.", Toast.LENGTH_LONG).show();
                return;
            }
            progressBar.setVisibility(VISIBLE);
            sendBtn.setEnabled(false);
            saveToFirebase(t, gameNum, teamAtGame);
        }
    }


    /**
     * Initiates the process of saving the collected scouting data to Firebase.
     * First, it fetches existing stats for the team to ensure the new data is appended correctly.
     * @param t The team being scouted.
     * @param gameNum The match number.
     * @param teamAtGame The data object containing the team's performance in this specific game.
     */
    private void saveToFirebase(Team t, int gameNum, TeamAtGame teamAtGame) {
        DataHelper.getInstance().readTeamStats(Integer.toString(t.getTeamNumber()),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override
                    public void onSuccess(TeamStats data) {
                        if (data == null) data = new TeamStats(t);
                        persistToFirebase(t, gameNum, teamAtGame, data);
                    }

                    @Override
                    public void onFailure(String error) {
                        persistToFirebase(t, gameNum, teamAtGame, new TeamStats(t));
                    }
                }
        );
    }

    /**
     * Persists the aggregated team statistics back to the Firebase database.
     * If the form was opened from an assignment, it also marks that assignment as completed.
     * @param t The team being scouted.
     * @param gameNum The match number.
     * @param teamAtGame The performance data for this game.
     * @param stats The updated aggregated statistics for the team.
     */
    private void persistToFirebase(Team t, int gameNum, TeamAtGame teamAtGame, TeamStats stats) {
        stats.addGame(teamAtGame);
        DataHelper.getInstance().replace(
                Constants.TEAMS_TABLE_NAME, Integer.toString(t.getTeamNumber()), stats,
                new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        if (assignmentKey != null) completeAssignment(t, gameNum);
                        else runOnUiThread(() -> onSaveSuccess());
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> onSaveSuccess());
                    }
                }
        );
    }

    /**
     * Marks a pending assignment as completed in the database.
     * @param t The team that was scouted.
     * @param gameNum The match number that was scouted.
     */
    private void completeAssignment(Team t, int gameNum) {
        String userId = SharedPrefHelper.getInstance(context).getUserId();
        if (assignmentDistrict == null) {
            runOnUiThread(() -> onSaveSuccess());
            return;
        }
        DataHelper.getInstance().completeAssignment(
                userId, assignmentDistrict, new Assignment(gameNum, t.getTeamNumber()),
                new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        runOnUiThread(() -> onSaveSuccess());
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> onSaveSuccess());
                    }
                }
        );
    }

    /**
     * Called when the scouting data has been successfully saved to the database.
     * Resets the form and provides feedback to the user.
     */
    private void onSaveSuccess() {
        progressBar.setVisibility(GONE);
        sendBtn.setEnabled(true);
        clearForm();
        Toast.makeText(this, "המידע נשמר בהצלחה", LENGTH_SHORT).show();
        AppCache.getInstance().setTotalGames(AppCache.getInstance().getTotalGames() + 1);
        if (assignmentKey != null) finish();
    }

    /**
     * Collects and validates all game piece scoring data from the form's EditText fields.
     * @param tg The TeamAtGame object to populate with scoring data.
     * @return true if all data is valid, false otherwise.
     */
    private boolean updateGamePieces(TeamAtGame tg) {

        String wrongDataStart = "you have entered wrong data in: ";
        if (!isGamePieceAmountCorrect(getInput(autoL1))) {
            Toast.makeText(this, wrongDataStart + " Auto L1", LENGTH_SHORT).show();
            return false;
        }
        if (!isGamePieceAmountCorrect(getInput(autoL2))) {
            Toast.makeText(this, wrongDataStart + " Auto L2", LENGTH_SHORT).show();
            return false;
        }
        if (!isGamePieceAmountCorrect(getInput(autoL3))) {
            Toast.makeText(this, wrongDataStart + " Auto L3", LENGTH_SHORT).show();
            return false;
        }
        if (!isGamePieceAmountCorrect(getInput(autoL4))) {
            Toast.makeText(this, wrongDataStart + " Auto L4", LENGTH_SHORT).show();
            return false;
        }

        if (!isGamePieceAmountCorrect(getInput(teleL1))) {
            Toast.makeText(this, wrongDataStart + " Teleop L1", LENGTH_SHORT).show();
            return false;
        }

        if (!isGamePieceAmountCorrect(getInput(teleL2))) {
            Toast.makeText(this, wrongDataStart + " Teleop L2", LENGTH_SHORT).show();
            return false;
        }


        if (!isGamePieceAmountCorrect(getInput(teleL3))) {
            Toast.makeText(this, wrongDataStart + " Teleop L3", LENGTH_SHORT).show();
            return false;
        }

        if (!isGamePieceAmountCorrect(getInput(teleL4))) {
            Toast.makeText(this, wrongDataStart + " Teleop L4", LENGTH_SHORT).show();
            return false;
        }

        if (!isGamePieceAmountCorrect(getInput(teleNet))) {
            Toast.makeText(this, wrongDataStart + " Teleop Net", LENGTH_SHORT).show();
            return false;
        }

        if (!isGamePieceAmountCorrect(getInput(teleProc))) {
            Toast.makeText(this, wrongDataStart + " Teleop Processor", LENGTH_SHORT).show();
            return false;
        }

        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L1, true), getInput(autoL1));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L2, true), getInput(autoL2));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L3, true), getInput(autoL3));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L4, true), getInput(autoL4));

        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L1, false), getInput(teleL1));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L2, false), getInput(teleL2));

        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L3, false), getInput(teleL3));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.L4, false), getInput(teleL4));

        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.NET, false), getInput(teleNet));
        tg.addGamePieceScored(new TeamAtGame.GamePieceScore(GamePiece.PROCESSOR, false), getInput(teleProc));

        tg.setClimb(checkClimb());
        return true;
    }


    private boolean isGamePieceAmountCorrect(int value) {
        return value >= 0 && value < 20;

    }


    /**
     * Executes the logic associated with the checkClimb operation.
     *
     * @return the value produced by this method.
     */
    private Climb checkClimb() {
        int id = group.getCheckedRadioButtonId();
        if (id == R.id.ClimbHigh) return Climb.HIGH;
        if (id == R.id.ClimbLow) return Climb.LOW;
        if (id == R.id.ClimbFailed) return Climb.FAILED;
        return Climb.DIDNT_TRY;
    }

    /**
     * Executes the logic associated with the getInput operation.
     *
     * @param et parameter required for this method.
     * @return the value produced by this method.
     */
    private int getInput(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Executes the logic associated with the clearForm operation.
     */
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
        if (assignmentKey == null) {
            teamNumber.setText("");
            gameNumber.setText("");
        }
        group.clearCheck();
    }

    /**
     * Executes the logic associated with the init operation.
     */
    private void init() {
        autoL1 = findViewById(R.id.AutoL1Count);
        autoL2 = findViewById(R.id.AutoL2Count);
        autoL3 = findViewById(R.id.AutoL3Count);
        autoL4 = findViewById(R.id.AutoL4Count);
        teleL1 = findViewById(R.id.TeleopL1Count);
        teleL2 = findViewById(R.id.TeleopL2Count);
        teleL3 = findViewById(R.id.TeleopL3Count);
        teleL4 = findViewById(R.id.TeleopL4Count);
        teleProc = findViewById(R.id.TeleopProCount);
        teleNet = findViewById(R.id.TeleopNetCount);
        teamNumber = findViewById(R.id.TeamNumberEditText);
        gameNumber = findViewById(R.id.GameNumberEditText);
        sendBtn = findViewById(R.id.buttonSave);
        context = FormsActivity.this;
        group = findViewById(R.id.ClimbGroup);
        progressBar = findViewById(R.id.progressBar);
    }

}