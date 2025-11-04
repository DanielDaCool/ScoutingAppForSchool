package com.example.mainapp.Utils.DatabaseUtils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.DatabaseUtils.DataHelper;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Service to monitor Firebase Realtime Database changes
 * Polls the database at regular intervals and broadcasts changes
 */
public class FirebaseService extends Service {

    private static final String TAG = "FirebaseMonitorService";
    private static final String CHANNEL_ID = "FirebaseMonitorChannel";
    private static final int NOTIFICATION_ID = 2001;

    // Polling interval in milliseconds (default: 3 seconds)
    private static final long POLL_INTERVAL = 3000;

    // Actions for broadcasts
    public static final String ACTION_TEAMSTATS_CHANGED = "com.example.mainapp.TEAMSTATS_CHANGED";
    public static final String ACTION_USER_CHANGED = "com.example.mainapp.USER_CHANGED";
    public static final String ACTION_SERVICE_ERROR = "com.example.mainapp.SERVICE_ERROR";

    // Extra keys
    public static final String EXTRA_TEAMSTATS_DATA = "teamstats_data";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";
    public static final String EXTRA_CHANGE_TYPE = "change_type";

    private DataHelper dataHelper;
    private Handler pollHandler;
    private Runnable pollRunnable;
    private DatabaseReference rootRef;

    // Cache to store last known state
    private Map<String, Object> lastTeamStatsCache;
    private long lastTeamStatsCount = 0;

    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        // Initialize DataHelper
        dataHelper = DataHelper.getInstance();
        rootRef = dataHelper.getRootRef();

        // Initialize cache
        lastTeamStatsCache = new HashMap<>();

        // Create notification channel
        createNotificationChannel();

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification("Monitoring Firebase Database"));

        // Initialize polling
        pollHandler = new Handler(Looper.getMainLooper());
        setupPolling();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (!isMonitoring) {
            isMonitoring = true;
            // Start polling
            pollHandler.post(pollRunnable);
        }

        // Restart service if killed by system
        return START_STICKY;
    }

    /**
     * Setup custom polling mechanism
     */
    private void setupPolling() {
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                // Monitor TeamStats changes
                monitorTeamStatsChanges();

                // Schedule next poll
                if (isMonitoring) {
                    pollHandler.postDelayed(this, POLL_INTERVAL);
                }
            }
        };
    }

    /**
     * Monitor TeamStats table for changes
     */
    private void monitorTeamStatsChanges() {
        rootRef.child(Constants.GAMES_TABLE_NAME).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();

                if (snapshot.exists()) {
                    // Parse current data
                    ArrayList<TeamStats> currentTeamStats = new ArrayList<>();
                    Map<String, Object> currentCache = new HashMap<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        TeamStats teamStats = child.getValue(TeamStats.class);
                        if (teamStats != null) {
                            currentTeamStats.add(teamStats);
                            currentCache.put(child.getKey(), teamStats);
                        }
                    }

                    // Detect changes
                    String changeType = detectChanges(currentCache, currentTeamStats.size());

                    if (changeType != null) {
                        Log.d(TAG, "TeamStats change detected: " + changeType);
                        broadcastTeamStatsChange(currentTeamStats, changeType);
                        updateNotification("TeamStats Updated (" + changeType + ")");
                    }

                    // Update cache
                    lastTeamStatsCache = currentCache;
                    lastTeamStatsCount = currentTeamStats.size();
                }
            } else {
                Log.e(TAG, "Error fetching TeamStats", task.getException());
                broadcastError("Failed to fetch data: " + task.getException().getMessage());
            }
        });
    }

    /**
     * Detect what type of change occurred
     */
    private String detectChanges(Map<String, Object> currentCache, int currentCount) {
        // First time initialization
        if (lastTeamStatsCache.isEmpty() && !currentCache.isEmpty()) {
            return "INITIAL_LOAD";
        }

        // Count changed (addition or deletion)
        if (currentCount != lastTeamStatsCount) {
            if (currentCount > lastTeamStatsCount) {
                return "TEAM_ADDED";
            } else {
                return "TEAM_REMOVED";
            }
        }

        // Check for modifications
        for (String key : currentCache.keySet()) {
            Object currentValue = currentCache.get(key);
            Object lastValue = lastTeamStatsCache.get(key);

            if (lastValue == null) {
                return "TEAM_ADDED";
            }

            // Compare objects (you might want to implement a more sophisticated comparison)
            if (!areTeamStatsEqual(currentValue, lastValue)) {
                return "TEAM_MODIFIED";
            }
        }

        // Check for removed teams
        for (String key : lastTeamStatsCache.keySet()) {
            if (!currentCache.containsKey(key)) {
                return "TEAM_REMOVED";
            }
        }

        return null; // No changes
    }

    /**
     * Compare two TeamStats objects
     */
    private boolean areTeamStatsEqual(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }

        // Simple comparison using toString
        // You might want to implement a more sophisticated comparison
        // based on specific fields in TeamStats
        return obj1.toString().equals(obj2.toString());
    }

    /**
     * Broadcast TeamStats changes
     */
    private void broadcastTeamStatsChange(ArrayList<TeamStats> teamStatsList, String changeType) {
        Intent intent = new Intent(ACTION_TEAMSTATS_CHANGED);
        intent.putExtra(EXTRA_CHANGE_TYPE, changeType);

        // Note: ArrayList<TeamStats> needs to be Serializable or Parcelable
        // For now, we'll send the count and let receivers fetch the data
        intent.putExtra("team_count", teamStatsList.size());

        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + changeType);
    }

    /**
     * Broadcast error
     */
    private void broadcastError(String errorMessage) {
        Intent intent = new Intent(ACTION_SERVICE_ERROR);
        intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(intent);
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Firebase Monitoring Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors Firebase Realtime Database for changes");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification(String content) {
        Intent notificationIntent = new Intent(this, getMainActivityClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Scouting App - Firebase Monitor")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    /**
     * Update notification content
     */
    private void updateNotification(String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(content));

            // Reset to default message after 2 seconds
            pollHandler.postDelayed(() -> {
                manager.notify(NOTIFICATION_ID, createNotification("Monitoring Firebase Database"));
            }, 2000);
        }
    }

    /**
     * Get the main activity class
     */
    private Class<?> getMainActivityClass() {
        try {
            return Class.forName(getPackageName() + ".MainActivity");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MainActivity not found", e);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        isMonitoring = false;

        // Stop polling
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }

        // Clean up
        lastTeamStatsCache.clear();
        dataHelper = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}