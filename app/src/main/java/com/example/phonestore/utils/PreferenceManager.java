package com.example.phonestore.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "phonestore_prefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_ACCOUNT = "saved_account";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveRememberMe(boolean remember, String account, String password) {
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) {
            editor.putString(KEY_SAVED_ACCOUNT, account);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(KEY_SAVED_ACCOUNT);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        editor.apply();
    }

    public boolean isRememberMeEnabled() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedAccount() {
        return pref.getString(KEY_SAVED_ACCOUNT, "");
    }

    public String getSavedPassword() {
        return pref.getString(KEY_SAVED_PASSWORD, "");
    }

    public void saveUserSession(boolean isLoggedIn, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public void clearSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.apply();
    }
}
