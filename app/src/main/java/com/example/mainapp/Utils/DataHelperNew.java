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
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
    }

    public static DataHelperNew getInstance() {
        if (instance == null) {
            instance = new DataHelperNew();
        }
        return instance;
    }


    public void createUser(User user, DatabaseCallback callback) {
        DatabaseReference tableRef = rootRef.child(Constants.USERS_TABLE_NAME);
        String id = tableRef.push().getKey();

        if (id != null) {
            tableRef.child(id).setValue(user)
                    .addOnSuccessListener(Void -> {
                        if (callback != null) callback.onSuccess(id);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                    });
        }
    }

    public void create(Object data, DatabaseCallback callback) {
        DatabaseReference tableRef = rootRef.child(tableName);
        String id = tableRef.push().getKey();

        if (id != null) {
            tableRef.child(id).setValue(data)
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
    }

    /**
     * Create a new record with custom ID
     *
     * @param tableName The name of the table/collection
     * @param id        Custom ID for the record
     * @param data      The object to save
     * @param callback  Callback for success/failure
     */
    public void createWithId(String tableName, String id, Object data, DatabaseCallback callback) {
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

    // ==================== READ ====================

    /**
     * Read a single record by ID (one-time)
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param dataClass The class type to convert to
     * @param callback  Callback with the data
     */
    public <T> void read(String tableName, String id, Class<T> dataClass, DataCallback<T> callback) {
        rootRef.child(tableName).child(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            T data = snapshot.getValue(dataClass);
                            if (callback != null) {
                                callback.onSuccess(data);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure("No data found");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Read all records from a table (one-time)
     *
     * @param tableName The name of the table/collection
     * @param dataClass The class type to convert to
     * @param callback  Callback with list of data
     */
    public <T> void readAll(String tableName, Class<T> dataClass, DataListCallback<T> callback) {
        rootRef.child(tableName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    T data = childSnapshot.getValue(dataClass);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }

    /**
     * Listen to a single record in real-time
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param dataClass The class type to convert to
     * @param callback  Callback with the data (called on each update)
     */
    public <T> void listenToRecord(String tableName, String id, Class<T> dataClass, DataCallback<T> callback) {
        rootRef.child(tableName).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    T data = snapshot.getValue(dataClass);
                    if (callback != null) {
                        callback.onSuccess(data);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }

    /**
     * Listen to all records in a table in real-time
     *
     * @param tableName The name of the table/collection
     * @param dataClass The class type to convert to
     * @param callback  Callback with list of data (called on each update)
     */
    public <T> void listenToTable(String tableName, Class<T> dataClass, DataListCallback<T> callback) {
        rootRef.child(tableName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    T data = childSnapshot.getValue(dataClass);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        });
    }

    // ==================== UPDATE ====================

    /**
     * Update specific fields of a record
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param updates   Map of field names to new values
     * @param callback  Callback for success/failure
     */
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

    /**
     * Replace entire record (overwrites all data)
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param data      The new object to save
     * @param callback  Callback for success/failure
     */
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

    // ==================== DELETE ====================

    /**
     * Delete a single record
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param callback  Callback for success/failure
     */
    public void delete(String tableName, String id, DatabaseCallback callback) {
        rootRef.child(tableName).child(id).removeValue()
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

    /**
     * Delete entire table
     *
     * @param tableName The name of the table/collection
     * @param callback  Callback for success/failure
     */
    public void deleteTable(String tableName, DatabaseCallback callback) {
        rootRef.child(tableName).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(tableName);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get a reference to a specific table
     *
     * @param tableName The name of the table/collection
     * @return DatabaseReference
     */
    public DatabaseReference getTableReference(String tableName) {
        return rootRef.child(tableName);
    }

    /**
     * Check if a record exists
     *
     * @param tableName The name of the table/collection
     * @param id        The ID of the record
     * @param callback  Callback with boolean result
     */
    public void exists(String tableName, String id, ExistsCallback callback) {
        rootRef.child(tableName).child(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = task.getResult().exists();
                        if (callback != null) {
                            callback.onResult(exists);
                        }
                    } else {
                        if (callback != null) {
                            callback.onResult(false);
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

    public interface DataListCallback<T> {
        void onSuccess(List<T> dataList);

        void onFailure(String error);
    }

    public interface ExistsCallback {
        void onResult(boolean exists);
    }
}