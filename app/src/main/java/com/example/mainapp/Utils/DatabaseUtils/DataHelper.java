package com.example.mainapp.Utils.DatabaseUtils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.Game;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Refactored DataHelper - Removed listener methods
 * Use FirebaseMonitoringService for real-time updates instead
 */
public class DataHelper {

    private FirebaseDatabase database;
    private DatabaseReference rootRef;

    // Singleton pattern
    private static DataHelper instance;

    private DataHelper() {
        database = FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app");
        rootRef = database.getReference();
    }

    public DatabaseReference getRootRef() {
        return this.rootRef;
    }

    public static DataHelper getInstance() {
        if (instance == null) {
            instance = new DataHelper();
        }
        return instance;
    }


    public void createUser(User user, DatabaseCallback callback) {
        isTableEmpty(Constants.USERS_TABLE_NAME, new ExistsCallback() {
            @Override
            public void onResult(boolean empty) {
                if (!empty) {
                    getLatestUserId(new DatabaseCallback() {
                        @Override
                        public void onSuccess(String id) {
                            int userID = Integer.parseInt(id) + 1;
                            createWithId(Constants.USERS_TABLE_NAME, Integer.toString(userID), new User(user.getFullName(), userID, user.getPassword(), user.getUserName()), callback);
                        }

                        @Override
                        public void onFailure(String error) {
                            callback.onFailure(error);
                        }
                    });
                } else {
                    createWithId(Constants.USERS_TABLE_NAME, "1", new User(user.getFullName(), 1, user.getPassword(), user.getUserName()), callback);
                }
            }
        });
    }

    public void createTeamStats(TeamStats data, DatabaseCallback callback) {
        createWithId(Constants.TEAMS_TABLE_NAME, Integer.toString(data.getTeam().getTeamNumber()), data, callback);
    }

    public void createWithId(String tableName, String id, Object data, DatabaseCallback callback) {
        new Thread(() -> {

            rootRef.child(tableName).child(id).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) {
                            callback.onSuccess(id);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                    });
        }).start();
    }


    public void isTableEmpty(String tableName, ExistsCallback callback) {
        rootRef.child(tableName).limitToFirst(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        boolean empty = !snapshot.exists() || !snapshot.hasChildren();
                        if (callback != null) {
                            callback.onResult(empty);
                        }
                    } else {
                        if (callback != null) {
                            callback.onResult(true); // Assume empty on error
                        }
                    }
                });
    }

    public void readUser(String userId, DataCallback<User> callback) {
        rootRef.child(Constants.USERS_TABLE_NAME).child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (callback != null) {
                                callback.onSuccess(user);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("User not found");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void readUserByUsername(String userName, DataCallback<User> callback) {
        rootRef.child(Constants.USERS_TABLE_NAME)
                .orderByChild("userName")
                .equalTo(userName)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();


                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            User foundUser = null;

                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {


                                User user = childSnapshot.getValue(User.class);

                                if (user != null) {

                                    if (user.getUserName() != null && user.getUserName().equals(userName)) {
                                        foundUser = user;
                                        break;
                                    }
                                }
                            }

                            if (foundUser != null) {
                                if (callback != null) {
                                    callback.onSuccess(foundUser);
                                }
                            } else {
                                if (callback != null) {
                                    callback.onFailure("User not found");
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("User not found");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error");
                        }
                    }
                });
    }

    public void getAvgOfTeam(int teamNumber, int amount, DataCallback<Double> callback) {
        getAvgOfTeam(Integer.toString(teamNumber), amount, callback);
    }

    public void getAvgOfTeam(String teamID, int amount, DataCallback<Double> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.exists()) {
                        TeamStats t = snapshot.getValue(TeamStats.class);


                        if (t == null || t.getAllGames() == null || t.getAllGames().isEmpty()) {

                            callback.onSuccess(0.0);
                            return;
                        }

                        List<TeamAtGame> games = t.getAllGames();
                        double avgPoints = 0.0;
                        int amountInFunc = MathUtils.clamp(amount, 1, 3);
                        if (amountInFunc > games.size()) amountInFunc = games.size();

                        for (int i = 0; i < amountInFunc; i++) {
                            avgPoints += (games.get(games.size() - 1 - i).calculatePoints()) * (1.0 / amount);
                        }

                        callback.onSuccess(avgPoints);
                    } else {

                        callback.onSuccess(0.0);
                    }
                } else {
                    callback.onFailure(task.getException() != null ?
                            task.getException().getMessage() : "שגיאה לא ידועה");
                }
            }
        });
    }

    public void readTeamStats(String teamID, DataCallback<TeamStats> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            TeamStats t = snapshot.getValue(TeamStats.class);
                            if (callback != null) {
                                callback.onSuccess(t);
                            }
                        } else {
                            if (callback != null) {

                                callback.onFailure("קבוצה לא קיימת, איתחול מידע");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void isTeamDataExists(Team t, ExistsCallback callback) {
        readTeamStats(Integer.toString(t.getTeamNumber()), new DataCallback<TeamStats>() {
            @Override
            public void onSuccess(TeamStats data) {
                if (data.getGamesPlayed() == 0) callback.onResult(false);
                callback.onResult(true);
            }

            @Override
            public void onFailure(String error) {
                callback.onResult(false);
            }
        });
    }

    public void isTableDataEmpty(ExistsCallback callback) {
        readAllTeamStats(new DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> data) {
                for (TeamStats t : data) {
                    if (t.getAllGames().size() != 0) callback.onResult(false);
                }
                callback.onResult(true);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    public void readAllTeamStats(DataCallback<ArrayList<TeamStats>> callback) {
        new Thread(() -> {
            rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    ArrayList<TeamStats> teamStatsList = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            TeamStats teamStats = child.getValue(TeamStats.class);
                            if (teamStats != null) {
                                teamStatsList.add(teamStats);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onSuccess(teamStatsList);
                    }
                }
            });
        }).start();
    }


    public void getLatestUserId(DatabaseCallback callback) {
        rootRef.child(Constants.USERS_TABLE_NAME)
                .orderByKey()
                .limitToLast(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String latestUserId = childSnapshot.getKey();
                                if (callback != null) {
                                    callback.onSuccess(latestUserId);
                                }

                                return;
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("No users found");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void countUsers(CountCallback callback) {
        rootRef.child(Constants.USERS_TABLE_NAME).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        long count = snapshot.getChildrenCount();
                        if (callback != null) {
                            callback.onResult(count);
                        }
                    } else {
                        if (callback != null) {
                            callback.onResult(0);
                        }
                    }
                });
    }

    public void countTeams(CountCallback callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                long count = snapshot.getChildrenCount();
                if (callback != null) callback.onResult(count);
            } else {
                if (callback != null) callback.onResult(0);
            }
        });
    }

    public void update(String tableName, String id, Map<String, Object> updates, DatabaseCallback callback) {
        rootRef.child(tableName).child(id).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(id);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void replace(String tableName, String id, Object data, DatabaseCallback callback) {
        rootRef.child(tableName).child(id).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(id);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    public void getCurrentTeamSnapshot(TeamSnapshotCallback callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(

                task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult());
                    } else {
                        callback.onFailure(new Exception("Task not successful"));
                    }
                }
        );
    }


    public void getUpdatedTeamStats(Team t, DataCallback<TeamStats> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME)
                .child(Integer.toString(t.getTeamNumber()))  // ← Specific team
                .addListenerForSingleValueEvent(new ValueEventListener() {  // ← Single read, not continuous
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            TeamStats teamStats = snapshot.getValue(TeamStats.class);
                            if (teamStats != null) {
                                callback.onSuccess(teamStats);
                            } else {
                                callback.onFailure("Failed to parse team stats");
                            }
                        } else {
                            callback.onFailure("Team not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public void getUpdatedTeamsStats(DataCallback<ArrayList<TeamStats>> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TeamStats> teamStatsList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        TeamStats teamStats = child.getValue(TeamStats.class);
                        if (teamStats != null) {
                            teamStatsList.add(teamStats);
                        }
                    }
                }
                callback.onSuccess(teamStatsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // ==================== CALLBACK INTERFACES ====================
    public interface DatabaseCallback {
        void onSuccess(String id);

        void onFailure(String error);
    }

    public interface DataCallback<T> {
        void onSuccess(T data);

        void onFailure(String error);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }

    public interface CountCallback {
        void onResult(long count);
    }

    public interface TeamSnapshotCallback {
        void onSuccess(DataSnapshot snapshot);

        void onFailure(Exception e);
    }

    // Removed: TeamStatsUpdateCallback interface
    // Removed: TeamStatsListCallback interface
}