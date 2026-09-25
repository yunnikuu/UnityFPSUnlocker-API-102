package io.github.hexstr.UnityFPSUnlocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Prefs {
    public static String prefs_name_ = "fps_prefs";

    public static SharedPreferences getSharedPrefs(Context context) {
        ServiceState.register();
        SharedPreferences local_preferences;
        try {
            local_preferences = context.getSharedPreferences(prefs_name_, Context.MODE_PRIVATE);
        } catch (Throwable t) {
            Log.e("hexstr", t.toString());
            local_preferences = null;
        }
        return ServiceState.getPreferences(prefs_name_, local_preferences);
    }
}
