package com.example.mainapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mainapp.Utils.DatabaseUtils.Assignment;

import java.util.ArrayList;
import java.util.List;

public class LocalDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME    = "scouting_offline.db";
    private static final int    DB_VERSION = 3;

    // Forms table
    private static final String FORMS_TABLE        = "pending_forms";
    private static final String COL_ID             = "id";
    private static final String COL_TEAM           = "teamNumber";
    private static final String COL_GAME           = "gameNumber";
    private static final String COL_PIECES         = "gamePiecesJson";
    private static final String COL_CLIMB          = "climb";
    private static final String COL_SYNCED         = "synced";
    private static final String COL_TIME           = "timestamp";
    private static final String COL_ASSIGNMENT_KEY = "assignmentKey";
    private static final String COL_USER_ID        = "userId";

    // Assignments table
    private static final String ASSIGN_TABLE    = "local_assignments";
    private static final String COL_KEY         = "assignmentKey";
    private static final String COL_ASSIGN_TEAM = "teamNumber";
    private static final String COL_ASSIGN_GAME = "gameNumber";

    private static LocalDatabase instance;

    private LocalDatabase(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized LocalDatabase getInstance(Context context) {
        if (instance == null) instance = new LocalDatabase(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FORMS_TABLE + " (" +
                COL_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TEAM           + " INTEGER, " +
                COL_GAME           + " INTEGER, " +
                COL_PIECES         + " TEXT, " +
                COL_CLIMB          + " TEXT, " +
                COL_SYNCED         + " INTEGER DEFAULT 0, " +
                COL_TIME           + " INTEGER, " +
                COL_ASSIGNMENT_KEY + " TEXT, " +
                COL_USER_ID        + " TEXT)");

        db.execSQL("CREATE TABLE " + ASSIGN_TABLE + " (" +
                COL_KEY         + " TEXT PRIMARY KEY, " +
                COL_ASSIGN_TEAM + " INTEGER, " +
                COL_ASSIGN_GAME + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FORMS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ASSIGN_TABLE);
        onCreate(db);
    }

    // ==================== FORMS ====================

    public long saveForm(OfflineFormData form) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEAM,           form.teamNumber());
        values.put(COL_GAME,           form.gameNumber());
        values.put(COL_PIECES,         form.gamePiecesJson());
        values.put(COL_CLIMB,          form.climb());
        values.put(COL_SYNCED,         0);
        values.put(COL_TIME,           form.timestamp());
        values.put(COL_ASSIGNMENT_KEY, form.assignmentKey());
        values.put(COL_USER_ID,        form.userId());
        return db.insert(FORMS_TABLE, null, values);
    }

    public void markAsSynced(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNCED, 1);
        db.update(FORMS_TABLE, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void markLatestAsSynced(int teamNumber, int gameNumber) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                FORMS_TABLE,
                new String[]{COL_ID},
                COL_TEAM + " = ? AND " + COL_GAME + " = ? AND " + COL_SYNCED + " = 0",
                new String[]{String.valueOf(teamNumber), String.valueOf(gameNumber)},
                null, null,
                COL_TIME + " DESC",
                "1"
        );
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            markAsSynced(id);
        }
        cursor.close();
    }

    public List<PendingForm> getUnsyncedForms() {
        SQLiteDatabase db = getReadableDatabase();
        List<PendingForm> results = new ArrayList<>();
        Cursor cursor = db.query(
                FORMS_TABLE, null,
                COL_SYNCED + " = 0",
                null, null, null,
                COL_TIME + " ASC"
        );
        if (cursor.moveToFirst()) {
            do {
                long   id            = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                int    team          = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM));
                int    game          = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GAME));
                String pieces        = cursor.getString(cursor.getColumnIndexOrThrow(COL_PIECES));
                String climb         = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIMB));
                long   timestamp     = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
                String assignmentKey = cursor.getString(cursor.getColumnIndexOrThrow(COL_ASSIGNMENT_KEY));
                String userId        = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID));
                results.add(new PendingForm(id,
                        new OfflineFormData(team, game, pieces, climb,
                                timestamp, assignmentKey, userId)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    public int getUnsyncedCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + FORMS_TABLE + " WHERE " + COL_SYNCED + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ==================== ASSIGNMENTS ====================

    /**
     * Replaces the entire local assignment list.
     * Called when Firebase listener fires online.
     */
    public void replaceAllAssignments(List<Assignment> assignments) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(ASSIGN_TABLE, null, null);
            for (Assignment a : assignments) {
                ContentValues values = new ContentValues();
                values.put(COL_KEY,         a.getKey());
                values.put(COL_ASSIGN_TEAM, a.getTeamNumber());
                values.put(COL_ASSIGN_GAME, a.getGameNumber());
                db.insert(ASSIGN_TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Deletes a single assignment.
     * Called immediately when scouter submits a form — removes from UI instantly.
     */
    public void deleteAssignment(String key) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ASSIGN_TABLE, COL_KEY + " = ?", new String[]{key});
    }


    public List<String> getLocalAssignmentKeys() {
        SQLiteDatabase db = getReadableDatabase();
        List<String> keys = new ArrayList<>();
        Cursor cursor = db.query(
                ASSIGN_TABLE,
                new String[]{COL_KEY},
                null, null, null, null, null
        );
        if (cursor.moveToFirst()) {
            do {
                keys.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return keys;
    }
    /**
     * Returns all locally stored assignments.
     * Used when offline to show the last known list.
     */
    public List<Assignment> getLocalAssignments() {
        SQLiteDatabase db = getReadableDatabase();
        List<Assignment> results = new ArrayList<>();
        Cursor cursor = db.query(
                ASSIGN_TABLE, null,
                null, null, null, null,
                COL_ASSIGN_GAME + " ASC"
        );
        if (cursor.moveToFirst()) {
            do {
                int    team = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ASSIGN_TEAM));
                int    game = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ASSIGN_GAME));
                results.add(new Assignment(game, team));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    // ==================== MODEL ====================

    public record PendingForm(long id, OfflineFormData data) {}
}