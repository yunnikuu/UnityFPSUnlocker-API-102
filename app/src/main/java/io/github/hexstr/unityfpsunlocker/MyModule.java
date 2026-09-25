package io.github.hexstr.UnityFPSUnlocker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Constructor;

import io.github.libxposed.api.XposedInterface.Chain;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

/** Unity FPS Unlocker entry point for libxposed API 102. */
public class MyModule extends XposedModule {
    private static final String TAG = "UnityFPSUnlocker";
    private static final String PREF_GROUP = "fps_prefs";

    private int display_mode_id = -1;
    private int delay = 5;
    private int fps = 90;
    private boolean mod_opcode = true;
    private float scale = -1;

    public static native void HelloWorld(int delay, int fps, boolean mod_opcode, float scale);

    @Override
    public void onPackageReady(PackageReadyParam param) {
        final String package_name = param.getPackageName();
        final ClassLoader class_loader = param.getClassLoader();
        loadPreferences(package_name);

        try {
            Class<?> unity_player_class = Class.forName(
                    "com.unity3d.player.UnityPlayer", false, class_loader);
            Class<?> lifecycle_events_class = Class.forName(
                    "com.unity3d.player.IUnityPlayerLifecycleEvents", false, class_loader);

            boolean is_hooked = false;
            for (Constructor<?> constructor : unity_player_class.getDeclaredConstructors()) {
                if (!containsType(constructor.getParameterTypes(), lifecycle_events_class)) {
                    continue;
                }

                hook(constructor).intercept((Chain chain) -> {
                    Object result = chain.proceed();
                    Activity activity = findActivity(chain.getArgs());
                    if (activity != null && display_mode_id != -1) {
                        Window window = activity.getWindow();
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.preferredDisplayModeId = display_mode_id;
                        window.setAttributes(params);
                        log(Log.INFO, TAG, "Set display mode to " + display_mode_id);
                    }
                    return result;
                });
                is_hooked = true;
            }

            if (!is_hooked) {
                log(Log.WARN, TAG, "No target constructors found.");
            }
        } catch (Throwable throwable) {
            log(Log.ERROR, TAG, "Failed to hook UnityPlayer", throwable);
        }

        log(Log.INFO, TAG, "display_mode_id: " + display_mode_id
                + " | delay: " + delay
                + " | fps: " + fps
                + " | mod_opcode: " + mod_opcode
                + " | scale: " + scale);
        try {
            System.loadLibrary("UnityFPSUnlocker");
            HelloWorld(delay, fps, mod_opcode, scale);
        } catch (Throwable throwable) {
            log(Log.ERROR, TAG, "Failed to load native UnityFPSUnlocker library", throwable);
        }
    }

    private void loadPreferences(String package_name) {
        try {
            SharedPreferences settings = getRemotePreferences(PREF_GROUP);
            display_mode_id = getIntPref(settings, "display_mode_id", -1);
            delay = getIntPref(settings, "delay", 5);
            fps = getIntPref(settings, "fps", 90);
            mod_opcode = settings.getBoolean("mod_opcode", true);
            scale = getFloatPref(settings, "scale", -1);

            display_mode_id = getIntPref(settings,
                    package_name + "_per_app_display_mode_id", display_mode_id);
            delay = getIntPref(settings, package_name + "_per_app_delay", delay);
            fps = getIntPref(settings, package_name + "_per_app_fps", fps);
            mod_opcode = settings.getBoolean(package_name + "_per_app_mod_opcode", mod_opcode);
            scale = getFloatPref(settings, package_name + "_per_app_scale", scale);
        } catch (Throwable throwable) {
            log(Log.ERROR, TAG, "Cannot read remote preferences", throwable);
        }
    }

    private static boolean containsType(Class<?>[] types, Class<?> expected) {
        for (Class<?> type : types) {
            if (type == expected) {
                return true;
            }
        }
        return false;
    }

    private static Activity findActivity(java.util.List<Object> args) {
        for (Object arg : args) {
            if (arg instanceof Activity) {
                return (Activity) arg;
            }
        }
        return null;
    }

    private int getIntPref(SharedPreferences settings, String key, int fallback) {
        String value = settings.getString(key, String.valueOf(fallback));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            log(Log.WARN, TAG, "Invalid integer preference value: " + value);
            return fallback;
        }
    }

    private float getFloatPref(SharedPreferences settings, String key, float fallback) {
        String value = settings.getString(key, String.valueOf(fallback));
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            log(Log.WARN, TAG, "Invalid float preference value: " + value);
            return fallback;
        }
    }
}
