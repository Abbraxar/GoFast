package com.i3cnam.gofast.tools.activityRestarter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.tools.App;
import com.i3cnam.gofast.views.Main;

/**
 * Singleton implementation of activity restarter.
 * Use sharedPreferences to store the activity to restart name.
 */
public class ActivityRestarterImpl implements ActivityRestarter {
    // reference to singleton
    private static volatile ActivityRestarterImpl mInstance = null;

    private final static String defaultActivityToRestart = "Main";
    private final static String lastActivityKey  = "last_activity";

    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private Context context;

    /**
     * Return unique instance of class or lazy create one.
     * @return mInstance
     */
    public static ActivityRestarterImpl getInstance() {
        if(mInstance == null) {
            synchronized(ActivityRestarterImpl.class) {
                if(mInstance == null) {
                    mInstance = new ActivityRestarterImpl();
                }
            }
        }
        return mInstance;
    }

    /**
     * Private constructor.
     * Store the global application context.
     */
    private ActivityRestarterImpl() {
        this.context = App.get();
        this.prefs = this.context.getSharedPreferences(
                this.context.getString(R.string.app_name), this.context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    /**
     * Clear activityToRestart.
     */
    public void clearActivityToRestart() {
        this.editor
                .remove(this.lastActivityKey)
                .apply();
    }

    /**
     * Set activityToRestart.
     * @param activityName
     */
    public void setActivityToRestart(String activityName) {
        this.editor
                .putString(this.lastActivityKey, activityName)
                .apply();
    }

    /**
     * Launch activityToRestart.
     */
    public void startActivityToRestart() {
        Class<?> activityClass;
        try {
            activityClass = Class.forName(
                    this.prefs.getString(this.lastActivityKey, this.defaultActivityToRestart));
        } catch (ClassNotFoundException ex) {
            activityClass = Main.class;
        }

        context.startActivity(new Intent(context, activityClass));
    }
}
