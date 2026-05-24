package com.example.mainapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mainapp.TBAHelpers.EVENTS;
import com.example.mainapp.Utils.DatabaseUtils.UserRole;

/**
 * SharedPrefHelper is a utility class for managing Android SharedPreferences.
 * It provides a clean API for persisting and retrieving user session data,
 * application settings, and the currently selected competition district.
 */
public class SharedPrefHelper {

    private static final String PREF_NAME        = "MainAppPreferences";
    private static final String KEY_FULL_NAME    = "full_name";
    private static final String KEY_EMAIL        = "email";
    private static final String KEY_USER_ID      = "user_id";
    private static final String KEY_ROLE         = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_HAS_LAUNCHED = "has_launched";
    private static final String KEY_DISTRICT     = "district";

    private final SharedPreferences prefs;
        private final SharedPreferences.Editor editor;
    private static SharedPrefHelper instance;

    private SharedPrefHelper(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static SharedPrefHelper getInstance(Context context) {
        if (instance == null) instance = new SharedPrefHelper(context.getApplicationContext());
        return instance;
    }


/**
 * Executes the logic associated with the saveUser operation.
 * @param fullName parameter required for this method.
 * @param email parameter required for this method.
 * @param userId parameter required for this method.
 * @param role parameter required for this method.
 */
    public void saveUser(String fullName, String email, String userId, UserRole role) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role.name());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

/**
 * Executes the logic associated with the logout operation.
 */
    public void logout() {
        editor.putString(KEY_FULL_NAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.putString(KEY_USER_ID, "");
        editor.putString(KEY_ROLE, UserRole.SCOUTER.name());
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putString(KEY_DISTRICT, "");
        editor.apply();
    }


/**
 * Executes the logic associated with the saveDistrict operation.
 * @param district parameter required for this method.
 */
    public void saveDistrict(EVENTS district) {
        editor.putString(KEY_DISTRICT, district.name());
        editor.apply();
    }

/**
 * Executes the logic associated with the getCurrentDistrict operation.
 * @return the value produced by this method.
 */
    public EVENTS getCurrentDistrict() {
        String d = prefs.getString(KEY_DISTRICT, null);
        if (d == null || d.isEmpty()) return null;
        try { return EVENTS.valueOf(d); }
        catch (IllegalArgumentException e) { return null; }
    }


/**
 * Executes the logic associated with the hasLaunchedBefore operation.
 * @return the value produced by this method.
 */
    public boolean hasLaunchedBefore() {
        return prefs.getBoolean(KEY_HAS_LAUNCHED, false);
    }

/**
 * Executes the logic associated with the markHasLaunched operation.
 */
    public void markHasLaunched() {
        editor.putBoolean(KEY_HAS_LAUNCHED, true);
        editor.apply();
    }


/**
 * Executes the logic associated with the isUserLoggedIn operation.
 * @return the value produced by this method.
 */
    public boolean isUserLoggedIn() { return prefs.getBoolean(KEY_IS_LOGGED_IN, false); }
/**
 * Executes the logic associated with the getFullName operation.
 * @return the value produced by this method.
 */
    public String getFullName()     { return isUserLoggedIn() ? prefs.getString(KEY_FULL_NAME, "") : "משתמש"; }
/**
 * Executes the logic associated with the getFirstName operation.
 * @return the value produced by this method.
 */
    public String getFirstName()    { return getFullName().split(" ", 2)[0]; }
/**
 * Executes the logic associated with the getEmail operation.
 * @return the value produced by this method.
 */
    public String getEmail()        { return prefs.getString(KEY_EMAIL, ""); }
/**
 * Executes the logic associated with the getUserId operation.
 * @return the value produced by this method.
 */
    public String getUserId()       { return prefs.getString(KEY_USER_ID, ""); }

/**
 * Executes the logic associated with the getRole operation.
 * @return the value produced by this method.
 */
    public UserRole getRole() {
        try { return UserRole.valueOf(prefs.getString(KEY_ROLE, UserRole.SCOUTER.name())); }
        catch (IllegalArgumentException e) { return UserRole.SCOUTER; }
    }

/**
 * Executes the logic associated with the isAdmin operation.
 * @return the value produced by this method.
 */
    public boolean isAdmin() { return getRole() == UserRole.ADMIN; }
}