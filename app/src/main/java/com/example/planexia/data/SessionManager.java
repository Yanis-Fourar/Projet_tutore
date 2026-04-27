package com.example.planexia.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "planexia_session";
    private static final String KEY_USER_ID = "user_id";
    private static final long NO_USER = -1;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, NO_USER);
    }

    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    public void clear() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
