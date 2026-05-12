package com.example.autocallresponder;

import android.content.Context;
import android.content.SharedPreferences;

public class AutoReplyConsent {

    private static final String PREF = "auto_reply_consent";
    private static final String KEY_ENABLED = "enabled";

    public static void enable(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, true)
                .apply();
    }

    public static boolean isEnabled(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false);
    }

    public static void disable(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, false)
                .apply();
    }
}
