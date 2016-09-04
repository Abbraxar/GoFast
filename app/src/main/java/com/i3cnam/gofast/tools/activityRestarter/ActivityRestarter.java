package com.i3cnam.gofast.tools.activityRestarter;

/**
 * Interface for activity restarter
 */
public interface ActivityRestarter {
    void clearActivityToRestart();

    void setActivityToRestart(String activityName);

    void startActivityToRestart();
}
