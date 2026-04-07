package com.example.mainapp.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mainapp.Utils.DatabaseUtils.Assignment;
import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class InternetReciver extends BroadcastReceiver {

    private static final String TAG = "InternetReciver";
    private boolean hasDisconnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (hasDisconnected && InternetUtils.isInternetConnected(context)) {
                hasDisconnected = false;
                Toast.makeText(context, "מחובר לאינטרנט ✓", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Internet restored — starting sync");
                // Run sync on background thread so main thread is never blocked
                new Thread(() -> syncPendingForms(context)).start();
            }
            else if (!hasDisconnected && !InternetUtils.isInternetConnected(context)) {
                hasDisconnected = true;
                Toast.makeText(context, "מנותק מהאינטרנט ✗", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Internet lost");
            }
        }
    }

    // ==================== SYNC ====================

    private void syncPendingForms(Context context) {
        LocalDatabase db = LocalDatabase.getInstance(context);
        List<LocalDatabase.PendingForm> pending = db.getUnsyncedForms();

        Log.d(TAG, "Found " + pending.size() + " unsynced forms");

        if (pending.isEmpty()) return;

        for (LocalDatabase.PendingForm form : pending) {
            syncSingleForm(context, db, form);
        }
    }

    private void syncSingleForm(Context context,
                                LocalDatabase db,
                                LocalDatabase.PendingForm pendingForm) {
        OfflineFormData formData = pendingForm.data();

        Log.d(TAG, "Syncing form — team: " + formData.teamNumber()
                + " game: " + formData.gameNumber()
                + " assignmentKey: " + formData.assignmentKey());

        Team team = new Team(formData.teamNumber(), "Team " + formData.teamNumber());

        DataHelper.getInstance().readTeamStats(
                String.valueOf(formData.teamNumber()),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override
                    public void onSuccess(TeamStats stats) {
                        Log.d(TAG, "Read team stats success — team: " + formData.teamNumber());
                        if (stats == null) stats = new TeamStats(team);
                        mergeAndSave(context, db, pendingForm, team, stats);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Read team stats failed: " + error + " — creating fresh stats");
                        mergeAndSave(context, db, pendingForm, team, new TeamStats(team));
                    }
                }
        );
    }

    private void mergeAndSave(Context context,
                              LocalDatabase db,
                              LocalDatabase.PendingForm pendingForm,
                              Team team,
                              TeamStats stats) {
        OfflineFormData formData = pendingForm.data();
        try {
            TeamAtGame teamAtGame = deserializeTeamAtGame(team, formData);
            stats.addGame(teamAtGame);

            Log.d(TAG, "Writing to Firebase — team: " + formData.teamNumber());

            DataHelper.getInstance().replace(
                    com.example.mainapp.Utils.Constants.TEAMS_TABLE_NAME,
                    String.valueOf(formData.teamNumber()),
                    stats,
                    new DataHelper.DatabaseCallback() {
                        @Override
                        public void onSuccess(String id) {
                            db.markAsSynced(pendingForm.id());
                            Log.d(TAG, "✅ Firebase write success — team: "
                                    + formData.teamNumber()
                                    + " game: " + formData.gameNumber());

                            if (formData.assignmentKey() != null && formData.userId() != null) {
                                Log.d(TAG, "Completing assignment: " + formData.assignmentKey());
                                completeAssignment(formData);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "❌ Firebase write failed: " + error);
                            // Leave unsynced — will retry next time internet returns
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during merge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void completeAssignment(OfflineFormData formData) {
        Log.d(TAG, "completeAssignment called — userId: " + formData.userId()
                + " key: " + formData.assignmentKey()
                + " game: " + formData.gameNumber()
                + " team: " + formData.teamNumber());

        Assignment assignment = new Assignment(
                formData.gameNumber(),
                formData.teamNumber());
        DataHelper.getInstance().completeAssignment(
                formData.userId(),
                assignment,
                new DataHelper.DatabaseCallback() {
                    @Override
                    public void onSuccess(String id) {
                        Log.d(TAG, "✅ Assignment completed: " + formData.assignmentKey());
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "❌ Assignment complete failed: " + error);
                    }
                }
        );
    }

    private TeamAtGame deserializeTeamAtGame(Team team, OfflineFormData formData) throws Exception {
        TeamAtGame teamAtGame = new TeamAtGame(team, formData.gameNumber());

        JSONArray piecesArray = new JSONArray(formData.gamePiecesJson());
        for (int i = 0; i < piecesArray.length(); i++) {
            JSONObject pieceObj = piecesArray.getJSONObject(i);
            String pieceName = pieceObj.getString("piece");
            boolean inAuto = pieceObj.getBoolean("inAuto");
            GamePiece gamePiece = GamePiece.valueOf(pieceName);
            teamAtGame.addGamePieceScored(gamePiece, inAuto);
        }

        try {
            teamAtGame.setClimb(CLIMB.valueOf(formData.climb()));
        } catch (IllegalArgumentException e) {
            teamAtGame.setClimb(CLIMB.DIDNT_TRY);
        }

        return teamAtGame;
    }
}