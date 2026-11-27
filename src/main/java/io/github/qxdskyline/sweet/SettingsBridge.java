package io.github.qxdskyline.sweet;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

public final class SettingsBridge {
    static final String JS_NAME = "SweetSettings";
    private static final String CONFIG_KEY = "config";

    private final SharedPreferences preferences;

    SettingsBridge(Context context) {
        preferences = context.getSharedPreferences("sweet_settings", Context.MODE_PRIVATE);
    }

    @JavascriptInterface
    public String read() {
        return preferences.getString(CONFIG_KEY, "");
    }

    @JavascriptInterface
    public boolean save(String json) {
        try {
            new JSONObject(json);
            return preferences.edit().putString(CONFIG_KEY, json).commit();
        } catch (JSONException invalid) {
            return false;
        }
    }
}
