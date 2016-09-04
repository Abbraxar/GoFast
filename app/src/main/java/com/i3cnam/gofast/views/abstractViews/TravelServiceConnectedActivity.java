package com.i3cnam.gofast.views.abstractViews;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.tools.activityRestarter.ActivityRestarterImpl;
import com.i3cnam.gofast.views.Main;

import java.io.Serializable;

public abstract class TravelServiceConnectedActivity extends AppCompatActivity {

    protected CarpoolingManagementService myService;
    protected boolean isBound = false;
    protected boolean isDataInit = false;

    public final static String PRIMARY_DATA = "com.i3cnam.gofast.PRIMARY_DATA";

    private final static String TAG_LOG = "ServiceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "CREATE");

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG_LOG, "DESTROY");

        unbindService(myConnection);
        super.onDestroy();
    }

    /**
     * Start service and bind it to activity
     * @param dataForService is the data given to service
     */
    protected void launchAndBindService(Serializable dataForService) {
        // new intent for publication:
        Intent serviceIntent = new Intent(this, CarpoolingManagementService.class);
        // new bundle
        Bundle serviceBundle = new Bundle();
        serviceBundle.putSerializable(PRIMARY_DATA, dataForService);
        serviceIntent.putExtras(serviceBundle);
        // start service with th intent and bind it
        startService(serviceIntent);
        Log.d(TAG_LOG, "Bind Service");
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
    }

    protected void stopServiceAndCloseActivity() {
        // stop service
        if(isBound) {
            myService.stopForeground(true);
            myService.stopSelf();
        }

        // save main activity as activity to restart
        ActivityRestarterImpl.getInstance().clearActivityToRestart();

        // open main activity
        Intent intent = new Intent(this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.d(TAG_LOG, "Service connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            myService = ((CarpoolingManagementService.LocalBinder) service).getService();
            isBound = true;
            afterServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    protected abstract void afterServiceConnected();
}
