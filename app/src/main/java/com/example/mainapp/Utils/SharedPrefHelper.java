package com.example.mainapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

    private static final String PREF_NAME = "MainAppPreferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static SharedPrefHelper instance;

    private SharedPrefHelper(Context context){
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public static SharedPrefHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(int userId, String userName, String fullName) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isUserLoggedIn(){
        return  sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public String getUserName(){
        return isUserLoggedIn() ? sharedPreferences.getString(KEY_USER_NAME, "") : "שם";
    }

}
