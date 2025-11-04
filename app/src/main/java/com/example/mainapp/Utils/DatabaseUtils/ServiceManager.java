package com.example.mainapp.Utils.DatabaseUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

/**
 * Manager class to control FirebaseMonitoringService and handle broadcasts
 */
public class ServiceManager {

    private static final String TAG = "ServiceManager";
    private Context context;
    private FirebaseDataReceiver dataReceiver;
    private OnDataChangeListener listener;
    private boolean isReceiverRegistered = false;

    /**
     * Listener interface for data changes
     */
    public interface OnDataChangeListener {
        void onTeamStatsChanged(String changeType, int teamCount);
        void onError(String errorMessage);
    }

    public ServiceManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Start the Firebase Monitoring Service
     */
    public void startMonitoringService() {
        Intent serviceIntent = new Intent(context, FirebaseService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        Log.d(TAG, "Firebase Monitoring Service started");
    }

    /**
     * Stop the Firebase Monitoring Service
     */
    public void stopMonitoringService() {
        Intent serviceIntent = new Intent(context, FirebaseService.class);
        context.stopService(serviceIntent);
        Log.d(TAG, "Firebase Monitoring Service stopped");
    }

    /**
     * Register a listener to receive data change notifications
     */
    public void registerDataChangeListener(OnDataChangeListener listener) {
        this.listener = listener;

        // Create and register broadcast receiver
        dataReceiver = new FirebaseDataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FirebaseService.ACTION_TEAMSTATS_CHANGED);
        filter.addAction(FirebaseService.ACTION_SERVICE_ERROR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(dataReceiver, filter);
        }

        isReceiverRegistered = true;
        Log.d(TAG, "Data change listener registered");
    }

    /**
     * Unregister the data change listener
     */
    public void unregisterDataChangeListener() {
        if (dataReceiver != null && isReceiverRegistered) {
            try {
                context.unregisterReceiver(dataReceiver);
                dataReceiver = null;
                listener = null;
                isReceiverRegistered = false;
                Log.d(TAG, "Data change listener unregistered");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Receiver not registered", e);
            }
        }
    }

    /**
     * Check if the service is running
     */
    public boolean isServiceRunning() {
        return isReceiverRegistered;
    }

    /**
     * BroadcastReceiver to receive updates from FirebaseMonitoringService
     */
    private class FirebaseDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            String action = intent.getAction();
            Log.d(TAG, "Received broadcast: " + action);

            if (action.equals(FirebaseService.ACTION_TEAMSTATS_CHANGED)) {
                String changeType = intent.getStringExtra(FirebaseService.EXTRA_CHANGE_TYPE);
                int teamCount = intent.getIntExtra("team_count", 0);

                Log.d(TAG, "TeamStats changed: " + changeType + ", Count: " + teamCount);

                if (listener != null) {
                    listener.onTeamStatsChanged(changeType, teamCount);
                }
            } else if (action.equals(FirebaseService.ACTION_SERVICE_ERROR)) {
                String errorMessage = intent.getStringExtra(FirebaseService.EXTRA_ERROR_MESSAGE);

                Log.e(TAG, "Service error: " + errorMessage);

                if (listener != null) {
                    listener.onError(errorMessage);
                }
            }
        }
    }
}