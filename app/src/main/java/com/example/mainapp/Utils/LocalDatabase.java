package com.example.mainapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME    = "scouting_offline.db";
    private static final int    DB_VERSION = 1;

    private static final String TABLE      = "pending_forms";
    private static final String COL_ID     = "id";
    private static final String COL_TEAM   = "teamNumber";
    private static final String COL_GAME   = "gameNumber";
    private static final String COL_PIECES = "gamePiecesJson";
    private static final String COL_CLIMB  = "climb";
    private static final String COL_SYNCED = "synced";
    private static final String COL_TIME   = "timestamp";

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
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TEAM   + " INTEGER, " +
                COL_GAME   + " INTEGER, " +
                COL_PIECES + " TEXT, " +
                COL_CLIMB  + " TEXT, " +
                COL_SYNCED + " INTEGER DEFAULT 0, " +
                COL_TIME   + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // ==================== WRITE ====================

    /**
     * Saves a form locally when offline.
     * Returns the row id, or -1 on failure.
     */
    public long saveForm(OfflineFormData form) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEAM,   form.teamNumber());
        values.put(COL_GAME,   form.gameNumber());
        values.put(COL_PIECES, form.gamePiecesJson());
        values.put(COL_CLIMB,  form.climb());
        values.put(COL_SYNCED, 0);
        values.put(COL_TIME,   form.timestamp());
        return db.insert(TABLE, null, values);
    }

    /**
     * Marks a row as synced after Firebase confirms the save.
     */
    public void markAsSynced(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNCED, 1);
        db.update(TABLE, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ==================== READ ====================

    /**
     * Returns all unsynced forms — called when internet returns.
     */
    public List<PendingForm> getUnsyncedForms() {
        SQLiteDatabase db = getReadableDatabase();
        List<PendingForm> results = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE,
                null,
                COL_SYNCED + " = 0",
                null, null, null,
                COL_TIME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                long   id        = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                int    team      = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TEAM));
                int    game      = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GAME));
                String pieces    = cursor.getString(cursor.getColumnIndexOrThrow(COL_PIECES));
                String climb     = cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIMB));
                long   timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIME));
                results.add(new PendingForm(id,
                        new OfflineFormData(team, game, pieces, climb, timestamp)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

    /**
     * Returns count of unsynced forms.
     */
    public int getUnsyncedCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE " + COL_SYNCED + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ==================== MODEL ====================

    /**
     * Wraps OfflineFormData with its SQLite row id so we can mark it synced later.
     */
    public record PendingForm(long id, OfflineFormData data) {}
}