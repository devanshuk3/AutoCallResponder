package com.example.autocallresponder;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CustomModeRepository {

    private static final String PREF_NAME = "auto_responder_prefs";
    private static final String KEY_CUSTOM_MODES = "custom_modes_json";

    private final SharedPreferences prefs;

    public CustomModeRepository(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<CustomMode> loadCustomModes() {
        String json = prefs.getString(KEY_CUSTOM_MODES, "[]");
        List<CustomMode> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                CustomMode mode = fromJson(obj);
                if (mode != null) {
                    list.add(mode);
                }
            }
        } catch (JSONException e) {
            // ignore malformed data
        }
        return list;
    }

    public void saveCustomModes(List<CustomMode> modes) {
        JSONArray arr = new JSONArray();
        for (CustomMode m : modes) {
            JSONObject obj = toJson(m);
            if (obj != null) {
                arr.put(obj);
            }
        }
        prefs.edit().putString(KEY_CUSTOM_MODES, arr.toString()).apply();
    }

    public void addCustomMode(CustomMode mode) {
        List<CustomMode> list = loadCustomModes();
        list.add(mode);
        saveCustomModes(list);
    }

    public void removeCustomMode(String id) {
        List<CustomMode> list = loadCustomModes();
        list.removeIf(m -> m.id != null && m.id.equals(id));
        saveCustomModes(list);
    }

    public CustomMode findById(String id) {
        for (CustomMode m : loadCustomModes()) {
            if (m.id != null && m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    private static JSONObject toJson(CustomMode m) {
        try {
            JSONObject o = new JSONObject();
            o.put("id", m.id);
            o.put("name", m.name);
            o.put("message", m.message);
            return o;
        } catch (JSONException e) {
            return null;
        }
    }

    private static CustomMode fromJson(JSONObject o) {
        try {
            String id = o.optString("id", "");
            String name = o.optString("name", "").trim();
            String message = o.optString("message", "").trim();
            if (name.isEmpty()) {
                return null;
            }
            CustomMode mode = new CustomMode(name, message);
            if (!id.isEmpty()) {
                mode.id = id;
            }
            return mode;
        } catch (Exception e) {
            return null;
        }
    }
}
