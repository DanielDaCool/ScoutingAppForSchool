package com.example.mainapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

    private static final String PREF_NAME = "MainAppPreferences";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static SharedPrefHelper instance;

    private SharedPrefHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public static SharedPrefHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void logout() {
        editor.putString(KEY_FULL_NAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public void saveUser(String fullName, String email) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getFullName() {
        return isUserLoggedIn() ? sharedPreferences.getString(KEY_FULL_NAME, "") : "משתמש";
    }
    public String getEmail(){
        return isUserLoggedIn() ? sharedPreferences.getString(KEY_EMAIL, "") : "";
    }
    public String getFirstName(){
        return getFullName().split(" ", 2)[0];
    }
}
