package com.example.mainapp.Utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataHelperNew {

    private FirebaseDatabase database;
    private DatabaseReference rootRef;

    // Singleton pattern
    private static DataHelperNew instance;

    private DataHelperNew() {
        database = FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app");
        rootRef = database.getReference();
    }

    public static DataHelperNew getInstance() {
        if (instance == null) {
            instance = new DataHelperNew();
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
                            createWithId(Constants.USERS_TABLE_NAME, Integer.toString(userID), new User(user.getFullName(), userID, user.getPassword()), callback);
                        }

                        @Override
                        public void onFailure(String error) {
                            callback.onFailure(error);
                        }
                    });
                } else {
                    createWithId(Constants.USERS_TABLE_NAME, "1", new User(user.getFullName(), 1, user.getPassword()), callback);
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
}