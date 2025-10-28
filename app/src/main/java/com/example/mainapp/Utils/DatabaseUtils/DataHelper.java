package com.example.mainapp.Utils.DatabaseUtils;

import androidx.annotation.NonNull;

import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

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
        createWithId(Constants.GAMES_TABLE_NAME, Integer.toString(data.getTeam().getTeamNumber()), data, callback);
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
        android.util.Log.d("DataHelper", "Starting query for userName: " + userName);

        rootRef.child(Constants.USERS_TABLE_NAME)
                .orderByChild("userName")
                .equalTo(userName)
                .get()
                .addOnCompleteListener(task -> {
                    android.util.Log.d("DataHelper", "Query completed");

                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();

                        android.util.Log.d("DataHelper", "Snapshot exists: " + snapshot.exists());
                        android.util.Log.d("DataHelper", "Children count: " + snapshot.getChildrenCount());

                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            User foundUser = null;
                            int childCount = 0;

                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                childCount++;
                                android.util.Log.d("DataHelper", "=== Child #" + childCount + " ===");
                                android.util.Log.d("DataHelper", "Child key: " + childSnapshot.getKey());
                                android.util.Log.d("DataHelper", "Child raw data: " + childSnapshot.getValue());

                                User user = childSnapshot.getValue(User.class);

                                if (user != null) {
                                    android.util.Log.d("DataHelper", "User.userName: " + user.getUserName());
                                    android.util.Log.d("DataHelper", "User.password: " + user.getPassword());
                                    android.util.Log.d("DataHelper", "User.userID: " + user.getUserID());

                                    if (user.getUserName() != null && user.getUserName().equals(userName)) {
                                        foundUser = user;
                                        android.util.Log.d("DataHelper", "Found matching user with ID: " + user.getUserID());
                                        break;
                                    }
                                }
                            }

                            if (foundUser != null) {
                                if (callback != null) {
                                    callback.onSuccess(foundUser);
                                }
                            } else {
                                android.util.Log.d("DataHelper", "No valid user found with userName field");
                                if (callback != null) {
                                    callback.onFailure("User not found");
                                }
                            }
                        } else {
                            android.util.Log.d("DataHelper", "No matching user found");
                            if (callback != null) {
                                callback.onFailure("User not found");
                            }
                        }
                    } else {
                        android.util.Log.e("DataHelper", "Query failed: " + task.getException());
                        if (callback != null) {
                            callback.onFailure(task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error");
                        }
                    }
                });
    }

    public void readTeamStats(String teamID, DataCallback<TeamStats> callback) {
        rootRef.child(Constants.GAMES_TABLE_NAME).child(teamID).get()
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
    public void isTableDataEmpty(ExistsCallback callback){
        readAllTeamStats(new DataCallback<ArrayList<TeamStats>>() {
            @Override
            public void onSuccess(ArrayList<TeamStats> data) {
                for(TeamStats t : data){
                    if(t.getAllGames().size() != 0) callback.onResult(false);
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
            rootRef.child(Constants.GAMES_TABLE_NAME).get().addOnCompleteListener(task -> {
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
        rootRef.child(Constants.GAMES_TABLE_NAME).get().addOnCompleteListener(task -> {
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


    public ValueEventListener listenToAllTeamStats(TeamStatsUpdateCallback callback) {
        DatabaseReference teamStatsRef = database.getReference("teamStats"); // התאם את הנתיב לפי המבנה שלך

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TeamStats> teamStatsList = new ArrayList<>();

                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    TeamStats teamStats = teamSnapshot.getValue(TeamStats.class);
                    if (teamStats != null) {
                        teamStatsList.add(teamStats);
                    }
                }

                callback.onSuccess(teamStatsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        teamStatsRef.addValueEventListener(listener);
        return listener;
    }

    // מתוד להסרת המאזין
    public void removeTeamStatsListener(ValueEventListener listener) {
        if (listener != null) {
            DatabaseReference teamStatsRef = database.getReference("teamStats");
            teamStatsRef.removeEventListener(listener);
        }
    }


    // ==================== CALLBACK INTERFACES ====================
    public interface TeamStatsUpdateCallback {
        void onSuccess(ArrayList<TeamStats> teamStatsList);

        void onFailure(String error);
    }

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

    public interface TeamStatsListCallback {
        void onDataChanged(ArrayList<TeamStats> teamStatsList);

        void onError(String error);
    }
}