package com.example.mainapp.Utils.DatabaseUtils;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.example.mainapp.Utils.Constants;
import com.example.mainapp.Utils.TeamUtils.Team;
import com.example.mainapp.Utils.TeamUtils.TeamAtGame;
import com.example.mainapp.Utils.TeamUtils.TeamStats;
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

    private final FirebaseDatabase database;
    private final DatabaseReference rootRef;
    private final FirebaseAuth auth;

    private static DataHelper instance;

    private DataHelper() {
        database = FirebaseDatabase.getInstance("https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app");
        rootRef = database.getReference();
        auth = FirebaseAuth.getInstance();
    }

    // ==================== SINGLETON ====================

    public static synchronized DataHelper getInstance() {
        if (instance == null) instance = new DataHelper();
        return instance;
    }

    // ==================== GENERIC HELPERS ====================

    private <T> void fetchNode(String path, Class<T> type, DataCallback<T> callback) {
        Thread t = new Thread(() ->
                rootRef.child(path).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null)
                            callback.onFailure(task.getException() != null
                                    ? task.getException().getMessage() : "Unknown error");
                        return;
                    }
                    DataSnapshot snapshot = task.getResult();
                    if (!snapshot.exists()) {
                        if (callback != null) callback.onFailure("Not found");
                        return;
                    }
                    T value = snapshot.getValue(type);
                    if (callback != null) callback.onSuccess(value);
                })
        );
        t.setName("firebase-fetch-" + path);
        t.start();
    }

    private void writeNode(String tableName, String id, Object data, DatabaseCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(tableName).child(id).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(id);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e.getMessage());
                        })
        );
        t.setName("firebase-write-" + tableName + "-" + id);
        t.start();
    }

    // ==================== AUTH METHODS ====================

    /**
     * Register a new user.
     * Saves role: "SCOUTER" to users/userId/role in Realtime DB.
     * Admin can manually change this to "ADMIN" in Firebase Console.
     */
    public void registerUser(String fullName, String email, String password, DataCallback<User> callback) {
        Thread t = new Thread(() ->
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                // Save display name to Auth profile
                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();
                                firebaseUser.updateProfile(profileUpdate)
                                        .addOnCompleteListener(profileTask -> {
                                            // Save only the role to DB — everything else lives in Auth
                                            rootRef.child(Constants.USERS_TABLE_NAME)
                                                    .child(firebaseUser.getUid())
                                                    .child("role")
                                                    .setValue(UserRole.SCOUTER.name())
                                                    .addOnCompleteListener(roleTask -> {
                                                        if (callback != null)
                                                            callback.onSuccess(new User(
                                                                    fullName,
                                                                    email,
                                                                    UserRole.SCOUTER,
                                                                    firebaseUser.getUid()));
                                                    });
                                        });
                            } else {
                                if (callback != null)
                                    callback.onFailure(task.getException() != null
                                            ? task.getException().getMessage() : "Registration failed");
                            }
                        })
        );
        t.setName("firebase-register-" + email);
        t.start();
    }

    /**
     * Login user.
     * Reads role from users/userId/role — everything else comes from Firebase Auth.
     */
    public void loginUser(String email, String password, DataCallback<User> callback) {
        Thread t = new Thread(() ->
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String fullName = firebaseUser.getDisplayName() != null
                                        ? firebaseUser.getDisplayName() : "";
                                // Read role from DB
                                getUserRole(firebaseUser.getUid(), new DataCallback<UserRole>() {
                                    @Override
                                    public void onSuccess(UserRole role) {
                                        if (callback != null)
                                            callback.onSuccess(new User(
                                                    fullName,
                                                    firebaseUser.getEmail(),
                                                    role,
                                                    firebaseUser.getUid()));
                                    }
                                    @Override
                                    public void onFailure(String error) {
                                        // Default to SCOUTER if role not found
                                        if (callback != null)
                                            callback.onSuccess(new User(
                                                    fullName,
                                                    firebaseUser.getEmail(),
                                                    UserRole.SCOUTER,
                                                    firebaseUser.getUid()));
                                    }
                                });
                            } else {
                                if (callback != null) {
                                    String msg = task.getException() != null
                                            ? task.getException().getMessage() : "Login failed";
                                    if (msg.contains("no user record") || msg.contains("INVALID_LOGIN_CREDENTIALS")) {
                                        callback.onFailure("User not found");
                                    } else if (msg.contains("password is invalid") || msg.contains("WRONG_PASSWORD")) {
                                        callback.onFailure("Wrong password");
                                    } else {
                                        callback.onFailure(msg);
                                    }
                                }
                            }
                        })
        );
        t.setName("firebase-login-" + email);
        t.start();
    }

    /**
     * Reads role from users/userId/role.
     * Returns SCOUTER by default if not found.
     */
    public void getUserRole(String userId, DataCallback<UserRole> callback) {
        fetchNode(Constants.USERS_TABLE_NAME + "/" + userId + "/role",
                String.class,
                new DataCallback<String>() {
                    @Override
                    public void onSuccess(String role) {
                        try {
                            callback.onSuccess(UserRole.valueOf(role));
                        } catch (IllegalArgumentException e) {
                            callback.onSuccess(UserRole.SCOUTER); // fallback
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                        callback.onSuccess(UserRole.SCOUTER); // fallback
                    }
                });
    }

    public void logoutUser() {
        auth.signOut();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ==================== ASSIGNMENT METHODS ====================

    /**
     * Admin saves a new pending assignment for a scouter.
     * Path: assignments/userId/pending/gameNumber-teamNumber
     */
    public void saveAssignment(String userId, Assignment assignment, DatabaseCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.ASSIGNMENTS_TABLE_NAME)
                        .child(userId)
                        .child("pending")
                        .child(assignment.getKey())
                        .setValue(assignment)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(assignment.getKey());
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e.getMessage());
                        })
        );
        t.setName("firebase-save-assignment-" + userId);
        t.start();
    }

    /**
     * Moves assignment from pending to completed.
     * Called automatically when scouter submits a form.
     */
    public void completeAssignment(String userId, Assignment assignment, DatabaseCallback callback) {
        Thread t = new Thread(() -> {
            String key = assignment.getKey();
            rootRef.child(Constants.ASSIGNMENTS_TABLE_NAME)
                    .child(userId)
                    .child("completed")
                    .child(key)
                    .setValue(assignment)
                    .addOnSuccessListener(aVoid ->
                            rootRef.child(Constants.ASSIGNMENTS_TABLE_NAME)
                                    .child(userId)
                                    .child("pending")
                                    .child(key)
                                    .removeValue()
                                    .addOnSuccessListener(aVoid2 -> {
                                        if (callback != null) callback.onSuccess(key);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (callback != null) callback.onFailure(e.getMessage());
                                    })
                    )
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
        });
        t.setName("firebase-complete-assignment-" + userId + "-" + assignment.getKey());
        t.start();
    }

    /**
     * Deletes a pending assignment — used by admin to remove an assignment.
     */
    public void deleteAssignment(String userId, String key, DatabaseCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.ASSIGNMENTS_TABLE_NAME)
                        .child(userId)
                        .child("pending")
                        .child(key)
                        .removeValue()
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(key);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e.getMessage());
                        })
        );
        t.setName("firebase-delete-assignment-" + userId + "-" + key);
        t.start();
    }

    /**
     * Live listener for pending assignments.
     * Auto-updates the profile panel when admin adds/removes assignments.
     */
    public void listenToPendingAssignments(String userId, DataCallback<ArrayList<Assignment>> callback) {
        rootRef.child(Constants.ASSIGNMENTS_TABLE_NAME)
                .child(userId)
                .child("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Assignment> assignments = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Assignment a = child.getValue(Assignment.class);
                                if (a != null) assignments.add(a);
                            }
                        }
                        assignments.sort((a1, a2) ->
                                Integer.compare(a1.getGameNumber(), a2.getGameNumber()));
                        if (callback != null) callback.onSuccess(assignments);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Fetches all users from users/ — used in AdminPanelActivity.
     * Returns userId + role pairs.
     */
    public void getAllUserIds(DataCallback<ArrayList<String>> callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.USERS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) callback.onFailure("Failed to fetch users");
                        return;
                    }
                    ArrayList<String> userIds = new ArrayList<>();
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            userIds.add(child.getKey());
                        }
                    }
                    if (callback != null) callback.onSuccess(userIds);
                })
        );
        t.setName("firebase-get-all-userids");
        t.start();
    }

    // ==================== TEAM METHODS ====================

    public void createTeamStats(TeamStats data, DatabaseCallback callback) {
        writeNode(Constants.TEAMS_TABLE_NAME, Integer.toString(data.getTeam().getTeamNumber()), data, callback);
    }

    public void createWithId(String tableName, String id, Object data, DatabaseCallback callback) {
        writeNode(tableName, id, data, callback);
    }

    public void getAvgOfTeam(int teamNumber, int amount, DataCallback<Double> callback) {
        getAvgOfTeam(Integer.toString(teamNumber), amount, callback);
    }

    public void getAvgOfTeam(String teamID, int amount, DataCallback<Double> callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                if (callback != null)
                                    callback.onFailure(task.getException() != null
                                            ? task.getException().getMessage() : "Unknown error");
                                return;
                            }
                            DataSnapshot snapshot = task.getResult();
                            if (!snapshot.exists()) {
                                if (callback != null) callback.onSuccess(0.0);
                                return;
                            }
                            TeamStats teamStats = snapshot.getValue(TeamStats.class);
                            if (teamStats == null || teamStats.getAllGames() == null || teamStats.getAllGames().isEmpty()) {
                                if (callback != null) callback.onSuccess(0.0);
                                return;
                            }
                            List<TeamAtGame> games = teamStats.getAllGames();
                            double avgPoints = 0.0;
                            int amt = MathUtils.clamp(amount, 1, 3);
                            if (amt > games.size()) amt = games.size();
                            for (int i = 0; i < amt; i++) {
                                avgPoints += games.get(games.size() - 1 - i).calculatePoints() * (1.0 / amount);
                            }
                            if (callback != null) callback.onSuccess(avgPoints);
                        })
        );
        t.setName("firebase-avg-" + teamID);
        t.start();
    }

    public void readTeamStats(String teamID, DataCallback<TeamStats> callback) {
        fetchNode(Constants.TEAMS_TABLE_NAME + "/" + teamID, TeamStats.class, new DataCallback<TeamStats>() {
            @Override
            public void onSuccess(TeamStats data) {
                if (callback != null) callback.onSuccess(data);
            }
            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure("קבוצה לא קיימת, איתחול מידע");
            }
        });
    }

    public void isTeamDataExists(Team t, ExistsCallback callback) {
        readTeamStats(Integer.toString(t.getTeamNumber()), new DataCallback<TeamStats>() {
            @Override
            public void onSuccess(TeamStats data) {
                callback.onResult(data.getGamesPlayed() != 0);
            }
            @Override
            public void onFailure(String error) {
                callback.onResult(false);
            }
        });
    }

    public void readAllTeamStats(DataCallback<ArrayList<TeamStats>> callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null)
                            callback.onFailure(task.getException() != null
                                    ? task.getException().getMessage() : "Unknown error");
                        return;
                    }
                    DataSnapshot snapshot = task.getResult();
                    ArrayList<TeamStats> teamStatsList = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            TeamStats teamStats = child.getValue(TeamStats.class);
                            if (teamStats != null) teamStatsList.add(teamStats);
                        }
                    }
                    if (callback != null) callback.onSuccess(teamStatsList);
                })
        );
        t.setName("firebase-read-all-teams");
        t.start();
    }

    public void countTeams(CountCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        long count = task.getResult().getChildrenCount();
                        if (callback != null) callback.onResult(count);
                    } else {
                        if (callback != null) callback.onResult(0);
                    }
                })
        );
        t.setName("firebase-count-teams");
        t.start();
    }

    public void update(String tableName, String id, Map<String, Object> updates, DatabaseCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(tableName).child(id).updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess(id);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onFailure(e.getMessage());
                        })
        );
        t.setName("firebase-update-" + tableName + "-" + id);
        t.start();
    }

    public void replace(String tableName, String id, Object data, DatabaseCallback callback) {
        writeNode(tableName, id, data, callback);
    }

    public void getCurrentTeamSnapshot(TeamSnapshotCallback callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onSuccess(task.getResult());
                    } else {
                        if (callback != null) callback.onFailure(new Exception("Task not successful"));
                    }
                })
        );
        t.setName("firebase-snapshot-teams");
        t.start();
    }

    public void getUpdatedTeamStats(Team team, DataCallback<TeamStats> callback) {
        Thread t = new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME)
                        .child(Integer.toString(team.getTeamNumber()))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    TeamStats teamStats = snapshot.getValue(TeamStats.class);
                                    if (teamStats != null) {
                                        if (callback != null) callback.onSuccess(teamStats);
                                    } else {
                                        if (callback != null) callback.onFailure("Failed to parse team stats");
                                    }
                                } else {
                                    if (callback != null) callback.onFailure("Team not found");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (callback != null) callback.onFailure(error.getMessage());
                            }
                        })
        );
        t.setName("firebase-live-team-" + team.getTeamNumber());
        t.start();
    }

    // Persistent listener — no thread wrap intentionally
    public void getUpdatedTeamsStats(DataCallback<ArrayList<TeamStats>> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<TeamStats> teamStatsList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        TeamStats teamStats = child.getValue(TeamStats.class);
                        if (teamStats != null) teamStatsList.add(teamStats);
                    }
                }
                if (callback != null) callback.onSuccess(teamStatsList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
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
}