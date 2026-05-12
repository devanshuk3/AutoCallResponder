package com.example.autocallresponder;

import android.content.Context;
import android.content.SharedPreferences;

public class ModeManager {

    public enum Mode {
        NONE,
        BUSY,
        DRIVING,
        SLEEPING,
        IN_CLASS,
        CUSTOM
    }

    private static final String PREF = "auto_responder_prefs";
    private static final String KEY_MODE = "current_mode";
    private static final String PREFIX_CUSTOM = "custom:";

    public static void setMode(Context context, Mode mode) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, mode.name()).apply();
    }

    public static void setCustomMode(Context context, String customModeId) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, PREFIX_CUSTOM + customModeId).apply();
    }

    public static Mode getMode(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String value = prefs.getString(KEY_MODE, Mode.NONE.name());
        if (value != null && value.startsWith(PREFIX_CUSTOM)) {
            return Mode.CUSTOM;
        }
        try {
            return Mode.valueOf(value);
        } catch (Exception e) {
            return Mode.NONE;
        }
    }

    public static String getCurrentCustomModeId(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String value = prefs.getString(KEY_MODE, Mode.NONE.name());
        if (value != null && value.startsWith(PREFIX_CUSTOM)) {
            return value.substring(PREFIX_CUSTOM.length());
        }
        return null;
    }

    public static String statusText(Mode mode) {
        switch (mode) {
            case BUSY:
                return "currently busy";
            case DRIVING:
                return "currently driving";
            case SLEEPING:
                return "currently sleeping";
            case IN_CLASS:
                return "currently in class";
            default:
                return "";
        }
    }

    /** Returns the status/message text for the current mode (built-in or custom). */
    public static String getStatusMessage(Context context) {
        Mode mode = getMode(context);
        if (mode == Mode.CUSTOM) {
            String id = getCurrentCustomModeId(context);
            if (id != null) {
                CustomMode custom = new CustomModeRepository(context).findById(id);
                if (custom != null && custom.message != null && !custom.message.isEmpty()) {
                    return custom.message.trim();
                }
            }
            return "";
        }
        return statusText(mode);
    }
}
