package com.example.smartconfig;

import android.content.Context;

/**
 * AppContext — holds the application context so repositories can reach the
 * database without threading a Context through every call. Set once in
 * MainApplication.onCreate().
 */
public class AppContext {
    private static Context appContext;

    public static void set(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Context get() {
        return appContext;
    }
}