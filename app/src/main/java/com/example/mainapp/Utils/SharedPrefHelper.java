package com.example.mainapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mainapp.Utils.DatabaseUtils.UserRole;

public class SharedPrefHelper {

    private static final String PREF_NAME       = "MainAppPreferences";
    private static final String KEY_FULL_NAME   = "full_name";
    private static final String KEY_EMAIL       = "email";
    private static final String KEY_USER_ID     = "user_id";
    private static final String KEY_ROLE        = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static SharedPrefHelper instance;

    private SharedPrefHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public static SharedPrefHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefHelper(context.getApplicationContext());
        }
        return instance;
    }

    // ==================== SAVE / LOGOUT ====================

    public void saveUser(String fullName, String email, String userId, UserRole role) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role.name());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void logout() {
        editor.putString(KEY_FULL_NAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.putString(KEY_USER_ID, "");
        editor.putString(KEY_ROLE, UserRole.SCOUTER.name());
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    // ==================== GETTERS ====================

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getFullName() {
        return isUserLoggedIn() ? sharedPreferences.getString(KEY_FULL_NAME, "") : "משתמש";
    }

    public String getFirstName() {
        return getFullName().split(" ", 2)[0];
    }

    public String getEmail() {
        return isUserLoggedIn() ? sharedPreferences.getString(KEY_EMAIL, "") : "";
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public UserRole getRole() {
        String role = sharedPreferences.getString(KEY_ROLE, UserRole.SCOUTER.name());
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            return UserRole.SCOUTER; // fallback
        }
    }

    public boolean isAdmin() {
        return getRole() == UserRole.ADMIN;
    }
}