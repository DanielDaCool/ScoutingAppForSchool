package com.example.mainapp.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mainapp.Utils.DatabaseUtils.CLIMB;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.example.mainapp.Utils.DatabaseUtils.AppCache;
import com.example.mainapp.Utils.TeamUtils.TeamUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class InternetReciver extends BroadcastReceiver {

    private static final String TAG = "InternetReciver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            if (InternetUtils.isInternetConnected(context)) {
                Log.d("SYNC", "Internet restored — starting sync");
                Toast.makeText(context, "מחובר לאינטרנט ✓", Toast.LENGTH_SHORT).show();
                syncPendingForms(context);
            }
        }
    }

    // ==================== SYNC ====================

    /**
     * Called when internet is restored.
     * Reads all unsynced forms from SQLite and pushes them to Firebase silently.
     */
    private void syncPendingForms(Context context) {
        LocalDatabase db = LocalDatabase.getInstance(context);
        List<LocalDatabase.PendingForm> pending = db.getUnsyncedForms();
        Log.d("SYNC", "Found " + pending.size() + " unsynced forms");
        if (pending.isEmpty()) return;

        Log.d(TAG, "Syncing " + pending.size() + " pending forms...");

        for (LocalDatabase.PendingForm pendingForm : pending) {
            syncSingleForm(context, db, pendingForm);
        }
    }
    private void syncSingleForm(Context context,
                                LocalDatabase db,
                                LocalDatabase.PendingForm pendingForm) {
        OfflineFormData formData = pendingForm.data();

        // Don't rely on AppCache — it may be empty if app was killed
        // Just create a basic Team object from the stored team number
        Team team = new Team(formData.teamNumber(), "Team " + formData.teamNumber());

        DataHelper.getInstance().readTeamStats(
                String.valueOf(formData.teamNumber()),
                new DataHelper.DataCallback<TeamStats>() {
                    @Override
                    public void onSuccess(TeamStats stats) {
                        if (stats == null) stats = new TeamStats(team);
                        mergeAndSave(context, db, pendingForm, team, stats);
                    }
                    @Override
                    public void onFailure(String error) {
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

            DataHelper.getInstance().replace(
                    com.example.mainapp.Utils.Constants.TEAMS_TABLE_NAME,
                    String.valueOf(formData.teamNumber()),
                    stats,
                    new DataHelper.DatabaseCallback() {
                        @Override
                        public void onSuccess(String id) {
                            db.markAsSynced(pendingForm.id());
                            Log.d(TAG, "Synced form — team " + formData.teamNumber()
                                    + " game " + formData.gameNumber());
                        }
                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Failed to sync: " + error);
                            // Leave unsynced — will retry next time internet returns
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing form: " + e.getMessage());
        }
    }

    /**
     * Rebuilds a TeamAtGame from the JSON stored in SQLite.
     */
    private TeamAtGame deserializeTeamAtGame(Team team, OfflineFormData formData) throws Exception {
        TeamAtGame teamAtGame = new TeamAtGame(team, formData.gameNumber());

        JSONArray piecesArray = new JSONArray(formData.gamePiecesJson());
        for (int i = 0; i < piecesArray.length(); i++) {
            JSONObject pieceObj  = piecesArray.getJSONObject(i);
            String     pieceName = pieceObj.getString("piece");
            boolean    inAuto    = pieceObj.getBoolean("inAuto");
            GamePiece  gamePiece = GamePiece.valueOf(pieceName);
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