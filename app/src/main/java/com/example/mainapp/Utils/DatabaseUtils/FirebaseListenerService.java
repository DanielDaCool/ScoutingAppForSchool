package com.example.mainapp.Utils.DatabaseUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseListenerService extends Service {

    private static final String TAG = "FirebaseListenerService";

    // Constants for Broadcast
    public static final String ACTION_TEAM_STATS_UPDATED = "com.example.mainapp.TEAM_STATS_UPDATED";
    public static final String EXTRA_TEAM_STATS_COUNT = "team_stats_count";
    public static final String EXTRA_UPDATE_TYPE = "update_type";

    // Update types
    public static final String UPDATE_TYPE_SUCCESS = "success";
    public static final String UPDATE_TYPE_ERROR = "error";

    private ValueEventListener teamStatsListener;
    private boolean isListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (!isListening) {
            startListeningToFirebase();
            isListening = true;
        }

        // Service יחזור אוטומטית אם המערכת הרגה אותו
        return START_STICKY;
    }

    private void startListeningToFirebase() {
        Log.d(TAG, "Starting Firebase listener");

        teamStatsListener = DataHelper.getInstance().listenToAllTeamStats(
                new DataHelper.TeamStatsUpdateCallback() {
                    @Override
                    public void onSuccess(ArrayList<TeamStats> teamStatsList) {
                        Log.d(TAG, "Firebase data received: " + teamStatsList.size() + " teams");

                        // שליחת Broadcast לכל ה-Activities המאזינים
                        sendUpdateBroadcast(UPDATE_TYPE_SUCCESS, teamStatsList.size());
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Firebase listener error: " + error);
                        sendUpdateBroadcast(UPDATE_TYPE_ERROR, 0);
                    }
                }
        );
    }



    private void sendUpdateBroadcast(String updateType, int count) {
        Intent intent = new Intent(ACTION_TEAM_STATS_UPDATED);
        intent.putExtra(EXTRA_UPDATE_TYPE, updateType);
        intent.putExtra(EXTRA_TEAM_STATS_COUNT, count);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + updateType + ", count: " + count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        // הסרת המאזין
        if (teamStatsListener != null) {
            DataHelper.getInstance().removeTeamStatsListener(teamStatsListener);
            isListening = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Service לא קשור (Unbound Service)
        return null;
    }
}