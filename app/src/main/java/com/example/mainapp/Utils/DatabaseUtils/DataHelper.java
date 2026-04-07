package com.example.mainapp.Utils.DatabaseUtils;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.example.mainapp.TBAHelpers.EVENTS;
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
        database = FirebaseDatabase.getInstance(
                "https://scoutingapp-7bb4e-default-rtdb.europe-west1.firebasedatabase.app");
        rootRef = database.getReference();
        auth    = FirebaseAuth.getInstance();
    }

    public static synchronized DataHelper getInstance() {
        if (instance == null) instance = new DataHelper();
        return instance;
    }

    // ==================== GENERIC HELPERS ====================

    private <T> void fetchNode(String path, Class<T> type, DataCallback<T> callback) {
        new Thread(() ->
                rootRef.child(path).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) callback.onFailure(task.getException() != null
                                ? task.getException().getMessage() : "Unknown error");
                        return;
                    }
                    DataSnapshot snap = task.getResult();
                    if (!snap.exists()) { if (callback != null) callback.onFailure("Not found"); return; }
                    if (callback != null) callback.onSuccess(snap.getValue(type));
                })
        ).start();
    }

    private void writeNode(String table, String id, Object data, DatabaseCallback callback) {
        new Thread(() ->
                rootRef.child(table).child(id).setValue(data)
                        .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(id); })
                        .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); })
        ).start();
    }

    // ==================== AUTH ====================

    public void registerUser(String fullName, String email, String password, DataCallback<User> callback) {
        new Thread(() ->
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser fu = auth.getCurrentUser();
                                fu.updateProfile(new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName).build())
                                        .addOnCompleteListener(pt -> {
                                            User user = new User(fullName, email, UserRole.SCOUTER, fu.getUid());
                                            rootRef.child(Constants.USERS_TABLE_NAME).child(fu.getUid())
                                                    .setValue(user)
                                                    .addOnCompleteListener(dt -> {
                                                        if (callback != null) callback.onSuccess(user);
                                                    });
                                        });
                            } else {
                                if (callback != null) callback.onFailure(task.getException() != null
                                        ? task.getException().getMessage() : "Registration failed");
                            }
                        })
        ).start();
    }

    public void loginUser(String email, String password, DataCallback<User> callback) {
        new Thread(() ->
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser fu       = auth.getCurrentUser();
                                String       fullName = fu.getDisplayName() != null ? fu.getDisplayName() : "";
                                fetchNode(Constants.USERS_TABLE_NAME + "/" + fu.getUid(), User.class,
                                        new DataCallback<User>() {
                                            @Override public void onSuccess(User user) {
                                                if (user.getUserId() == null) user.setUserId(fu.getUid());
                                                if (callback != null) callback.onSuccess(user);
                                            }
                                            @Override public void onFailure(String error) {
                                                if (callback != null) callback.onSuccess(
                                                        new User(fullName, fu.getEmail(), UserRole.SCOUTER, fu.getUid()));
                                            }
                                        }
                                );
                            } else {
                                if (callback != null) {
                                    String msg = task.getException() != null
                                            ? task.getException().getMessage() : "Login failed";
                                    if (msg.contains("no user record") || msg.contains("INVALID_LOGIN_CREDENTIALS"))
                                        callback.onFailure("User not found");
                                    else if (msg.contains("password is invalid") || msg.contains("WRONG_PASSWORD"))
                                        callback.onFailure("Wrong password");
                                    else callback.onFailure(msg);
                                }
                            }
                        })
        ).start();
    }

    public void logoutUser()                   { auth.signOut(); }
    public FirebaseUser getCurrentFirebaseUser() { return auth.getCurrentUser(); }
    public String getCurrentUserId() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    // ==================== USERS ====================

    public void getAllUsers(DataCallback<ArrayList<User>> callback) {
        new Thread(() ->
                rootRef.child(Constants.USERS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) callback.onFailure(task.getException() != null
                                ? task.getException().getMessage() : "Unknown error");
                        return;
                    }
                    ArrayList<User> users = new ArrayList<>();
                    if (task.getResult().exists()) {
                        for (DataSnapshot child : task.getResult().getChildren()) {
                            User user = child.getValue(User.class);
                            if (user != null) {
                                if (user.getUserId() == null) user.setUserId(child.getKey());
                                users.add(user);
                            }
                        }
                    }
                    if (callback != null) callback.onSuccess(users);
                })
        ).start();
    }

    // ==================== ASSIGNMENTS ====================

    /**
     * Firebase path: assignments / userId / districtEventKey / pending|completed / key
     */
    private String assignmentPath(String userId, EVENTS district, String sub) {
        return Constants.ASSIGNMENTS_TABLE_NAME + "/" + userId
                + "/" + district.getEventKey() + "/" + sub;
    }

    public void saveAssignment(String userId, EVENTS district,
                               Assignment assignment, DatabaseCallback callback) {
        new Thread(() ->
                rootRef.child(assignmentPath(userId, district, "pending"))
                        .child(assignment.getKey())
                        .setValue(assignment)
                        .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(assignment.getKey()); })
                        .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); })
        ).start();
    }

    public void completeAssignment(String userId, EVENTS district,
                                   Assignment assignment, DatabaseCallback callback) {
        String key           = assignment.getKey();
        String completedPath = assignmentPath(userId, district, "completed");
        String pendingPath   = assignmentPath(userId, district, "pending");
        new Thread(() ->
                rootRef.child(completedPath).child(key).setValue(assignment)
                        .addOnSuccessListener(v ->
                                rootRef.child(pendingPath).child(key).removeValue()
                                        .addOnSuccessListener(v2 -> { if (callback != null) callback.onSuccess(key); })
                                        .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); })
                        )
                        .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); })
        ).start();
    }

    /** One-time read — used by AdminPanelActivity for counts. */
    public void getPendingAssignments(String userId, EVENTS district,
                                      DataCallback<ArrayList<Assignment>> callback) {
        new Thread(() ->
                rootRef.child(assignmentPath(userId, district, "pending"))
                        .get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                if (callback != null) callback.onFailure(task.getException() != null
                                        ? task.getException().getMessage() : "Unknown error");
                                return;
                            }
                            ArrayList<Assignment> list = new ArrayList<>();
                            if (task.getResult().exists()) {
                                for (DataSnapshot child : task.getResult().getChildren()) {
                                    Assignment a = child.getValue(Assignment.class);
                                    if (a != null) list.add(a);
                                }
                            }
                            list.sort((a, b) -> Integer.compare(a.getGameNumber(), b.getGameNumber()));
                            if (callback != null) callback.onSuccess(list);
                        })
        ).start();
    }

    /** Live listener — used by MainActivity to show scouter's assignments in real time. */
    public void listenToPendingAssignments(String userId, EVENTS district,
                                           DataCallback<ArrayList<Assignment>> callback) {
        rootRef.child(assignmentPath(userId, district, "pending"))
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Assignment> list = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Assignment a = child.getValue(Assignment.class);
                                if (a != null) list.add(a);
                            }
                        }
                        list.sort((a, b) -> Integer.compare(a.getGameNumber(), b.getGameNumber()));
                        if (callback != null) callback.onSuccess(list);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    // ==================== TEAMS ====================

    public void createTeamStats(TeamStats data, DatabaseCallback callback) {
        writeNode(Constants.TEAMS_TABLE_NAME,
                Integer.toString(data.getTeam().getTeamNumber()), data, callback);
    }

    public void isTeamDataExists(Team t, ExistsCallback callback) {
        readTeamStats(Integer.toString(t.getTeamNumber()), new DataCallback<TeamStats>() {
            @Override public void onSuccess(TeamStats data) { callback.onResult(data.getGamesPlayed() != 0); }
            @Override public void onFailure(String error)   { callback.onResult(false); }
        });
    }

    public void readTeamStats(String teamID, DataCallback<TeamStats> callback) {
        fetchNode(Constants.TEAMS_TABLE_NAME + "/" + teamID, TeamStats.class,
                new DataCallback<TeamStats>() {
                    @Override public void onSuccess(TeamStats data) { if (callback != null) callback.onSuccess(data); }
                    @Override public void onFailure(String error)   { if (callback != null) callback.onFailure(error); }
                }
        );
    }

    public void readAllTeamStats(DataCallback<ArrayList<TeamStats>> callback) {
        new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) callback.onFailure(task.getException() != null
                                ? task.getException().getMessage() : "Unknown error");
                        return;
                    }
                    ArrayList<TeamStats> list = new ArrayList<>();
                    if (task.getResult().exists()) {
                        for (DataSnapshot child : task.getResult().getChildren()) {
                            TeamStats ts = child.getValue(TeamStats.class);
                            if (ts != null) list.add(ts);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
        ).start();
    }

    public void countTeams(CountCallback callback) {
        new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onResult(task.getResult().getChildrenCount());
                    } else {
                        if (callback != null) callback.onResult(0);
                    }
                })
        ).start();
    }

    public void replace(String table, String id, Object data, DatabaseCallback callback) {
        writeNode(table, id, data, callback);
    }

    public void getAvgOfTeam(int teamID, int amount,  DataCallback<Double> callback){
        getAvgOfTeam(Integer.toString(teamID), amount, callback);
    }
    public void getAvgOfTeam(String teamID, int amount, DataCallback<Double> callback) {
        new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).child(teamID).get()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) { if (callback != null) callback.onSuccess(0.0); return; }
                            TeamStats ts = task.getResult().getValue(TeamStats.class);
                            if (ts == null || ts.getAllGames() == null || ts.getAllGames().isEmpty()) {
                                if (callback != null) callback.onSuccess(0.0); return;
                            }
                            List<TeamAtGame> games = ts.getAllGames();
                            int    amt       = MathUtils.clamp(amount, 1, games.size());
                            double avg       = 0.0;
                            for (int i = 0; i < amt; i++)
                                avg += games.get(games.size() - 1 - i).calculatePoints() * (1.0 / amount);
                            if (callback != null) callback.onSuccess(avg);
                        })
        ).start();
    }

    public void getUpdatedTeamStats(Team team, DataCallback<TeamStats> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME)
                .child(Integer.toString(team.getTeamNumber()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        TeamStats ts = snap.getValue(TeamStats.class);
                        if (ts != null) { if (callback != null) callback.onSuccess(ts); }
                        else            { if (callback != null) callback.onFailure("Not found"); }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onFailure(error.getMessage());
                    }
                });
    }

    public void getUpdatedTeamsStats(DataCallback<ArrayList<TeamStats>> callback) {
        rootRef.child(Constants.TEAMS_TABLE_NAME).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                ArrayList<TeamStats> list = new ArrayList<>();
                for (DataSnapshot child : snap.getChildren()) {
                    TeamStats ts = child.getValue(TeamStats.class);
                    if (ts != null) list.add(ts);
                }
                if (callback != null) callback.onSuccess(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onFailure(error.getMessage());
            }
        });
    }

    public void getCurrentTeamSnapshot(TeamSnapshotCallback callback) {
        new Thread(() ->
                rootRef.child(Constants.TEAMS_TABLE_NAME).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { if (callback != null) callback.onSuccess(task.getResult()); }
                    else { if (callback != null) callback.onFailure(new Exception("Failed")); }
                })
        ).start();
    }

    public void update(String table, String id, Map<String, Object> updates, DatabaseCallback callback) {
        new Thread(() ->
                rootRef.child(table).child(id).updateChildren(updates)
                        .addOnSuccessListener(v -> { if (callback != null) callback.onSuccess(id); })
                        .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); })
        ).start();
    }

    // ==================== CALLBACKS ====================

    public interface DatabaseCallback {
        void onSuccess(String id);
        void onFailure(String error);
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    public interface ExistsCallback  { void onResult(boolean exists); }
    public interface CountCallback   { void onResult(long count); }

    public interface TeamSnapshotCallback {
        void onSuccess(DataSnapshot snapshot);
        void onFailure(Exception e);
    }
}