package com.example.smartconfig;

import android.app.Application;

/**
 * MainApplication — runs once when the app process starts.
 * Stores the app context and seeds the SQLite parts catalog on first launch.
 *
 * Registered in AndroidManifest.xml via android:name=".MainApplication".
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.set(this);
        PartsRepository.init(this);
    }
}