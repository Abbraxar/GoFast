package com.i3cnam.gofast.tools;

import android.app.Application;

/**
 * Used to get application context on Singletons
 */
public class App extends Application {
    private static App instance;
    public static App get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
