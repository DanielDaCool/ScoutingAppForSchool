package com.example.mainapp.Utils.DatabaseUtils;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataHelper {

    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private FirebaseAuth auth;

    private static DataHelper instance;

    private DataHelper() {
        database = FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app");
        rootRef = database.getReference();
        auth = FirebaseAuth.getInstance();
    }


    public static synchronized DataHelper getInstance() {
        if (instance == null) {
            instance = new DataHelper();
        }
        return instance;
    }


    public void registerUser(String fullName, String email, String password, DataCallback<User> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();
                        firebaseUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(profileTask -> {
                                    if (callback != null) {
                                        callback.onSuccess(new User(fullName, email));
                                    }
                                });
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException() != null
                                    ? task.getException().getMessage() : "Registration failed");
                        }
                    }
                });
    }

    public void loginUser(String email, String password, DataCallback<User> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String fullName = firebaseUser.getDisplayName() != null
                                ? firebaseUser.getDisplayName() : "";
                        if (callback != null) {
                            callback.onSuccess(new User(fullName, firebaseUser.getEmail()));
                        }
                    } else {
                        if (callback != null) {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage() : "Login failed";
                            // Map Firebase error messages to user-friendly Hebrew-compatible keys
                            if (msg.contains("no user record") || msg.contains("INVALID_LOGIN_CREDENTIALS")) {
                                callback.onFailure("User not found");
                            } else if (msg.contains("password is invalid") || msg.contains("WRONG_PASSWORD")) {
                                callback.onFailure("Wrong password");
                            } else {
                                callback.onFailure(msg);
                            }
                        }
                    }
                });
    }

    public void logoutUser() {
        auth.signOut();
    }


    public void createTeamStats(TeamStats data, DatabaseCallback callback) {
        createWithId(Constants.TEAMS_TABLE_NAME, Integer.toString(data.getTeam().getTeamNumber()), data, callback);
    }

    public void createWithId(String tableName, String id, Object data, DatabaseCallback callback) {
        new Thread(() -> {
            rootRef.child(tableName).child(id).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess(id);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
        }).start();
    }

    public void getAvgOfTeam(int teamNumber, int amount, DataCallback<Double> callback) {
        getAvgOfTeam(Integer.toString(teamNumber), amount, callback);
    }

    public void getAvgOfTeam(String teamID, int amount, DataCallback<Double> callback) {
        new Thread(()->rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
                            callback.onFailure(task.getException() != null
                                    ? task.getException().getMessage() : "שגיאה לא ידועה");
                        }
                    }
                })).start();
    }

    public void readTeamStats(String teamID, DataCallback<TeamStats> callback) {
        new Thread(()->rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            TeamStats t = snapshot.getValue(TeamStats.class);
                            if (callback != null) callback.onSuccess(t);
                        } else {
                            if (callback != null) callback.onFailure("קבוצה לא קיימת, איתחול מידע");
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException().getMessage());
                    }
                })).start();
    }

    public void isTeamDataExists(Team t, ExistsCallback callback) {
        readTeamStats(Integer.toString(t.getTeamNumber()), new DataCallback<TeamStats>() {
            @Override
            public void onSuccess(TeamStats data) {
                if (data.getGamesPlayed() == 0) callback.onResult(false);
                else callback.onResult(true);
            }
            @Override
            public void onFailure(String error) {
                callback.onResult(false);
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
                            if (teamStats != null) teamStatsList.add(teamStats);
                        }
                    }
                    if (callback != null) callback.onSuccess(teamStatsList);
                }
            });
        }).start();
    }

    public void countTeams(CountCallback callback) {
        new Thread(()->rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                long count = snapshot.getChildrenCount();
                if (callback != null) callback.onResult(count);
            } else {
                if (callback != null) callback.onResult(0);
            }
        })).start();
    }

    public void update(String tableName, String id, Map<String, Object> updates, DatabaseCallback callback) {
        new Thread(()->rootRef.child(tableName).child(id).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(id);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                })).start();
    }

    public void replace(String tableName, String id, Object data, DatabaseCallback callback) {
        new Thread(()->rootRef.child(tableName).child(id).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(id);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                })).start();
    }

    public void getUpdatedTeamsStats(DataCallback<ArrayList<TeamStats>> callback) {
        new Thread(()->rootRef.child(Constants.TEAMS_TABLE_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TeamStats> teamStatsList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        TeamStats teamStats = child.getValue(TeamStats.class);
                        if (teamStats != null) teamStatsList.add(teamStats);
                    }
                }
                callback.onSuccess(teamStatsList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        })).start();
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
}