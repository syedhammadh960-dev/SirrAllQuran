package com.example.sirralquran.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.sirralquran.models.User;

public class AuthController {
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SirrUlQuranPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";

    public AuthController(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean login(String email, String password) {
        // In production, validate with backend/Firebase
        // For now, simple validation
        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Save login state
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, "user_" + System.currentTimeMillis());
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();

        return true;
    }

    public boolean signUp(String fullName, String email, String password) {
        // In production, create account with backend/Firebase
        // For now, simple validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return false;
        }

        // Save user data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, "user_" + System.currentTimeMillis());
        editor.putString(KEY_USER_NAME, fullName);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();

        return true;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getCurrentUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getCurrentUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "User");
    }

    public String getCurrentUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
}
