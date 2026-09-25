package io.github.hexstr.UnityFPSUnlocker;

import android.content.SharedPreferences;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/** Holds the API 102 service delivered to the module's settings process. */
final class ServiceState {
    private static volatile XposedService service;
    private static volatile boolean registered;

    private ServiceState() {
    }

    static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService bound_service) {
                service = bound_service;
            }

            @Override
            public void onServiceDied(XposedService dead_service) {
                if (service == dead_service) {
                    service = null;
                }
            }
        });
    }

    static SharedPreferences getPreferences(String group, SharedPreferences fallback) {
        XposedService bound_service = service;
        if (bound_service == null) {
            return fallback;
        }
        try {
            return bound_service.getRemotePreferences(group);
        } catch (Throwable ignored) {
            return fallback;
        }
    }
}
