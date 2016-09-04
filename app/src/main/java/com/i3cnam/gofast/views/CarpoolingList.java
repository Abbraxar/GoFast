package com.i3cnam.gofast.views;

/**
 * Created by nadege on 08/07/16.
 */

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService.LocalBinder;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.tools.activityRestarter.ActivityRestarterImpl;

import java.util.List;


public class CarpoolingList extends ListActivity {

    /** globals */
    public final static String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private CarpoolingManagementService myService;
    protected boolean isBound = false;
    protected boolean isTravelInit = false;

    private final String TAG_LOG = "CarpoolingList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all the data of the intent and create a new travel object
        PassengerTravel passengerTravel = new PassengerTravel();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            passengerTravel.setOrigin((Place) bundle.getSerializable(DestinationMap.ORIGIN));
            passengerTravel.setDestination((Place) bundle.getSerializable(EnterDestination.DESTINATION));
            passengerTravel.setPassenger(User.getMe(this));
            passengerTravel.setRadius(intent.getIntExtra(EnterDestination.RADIUS, 500));
        }

        // new intent for publication:
        Intent serviceIntent = new Intent(this, CarpoolingManagementService.class);
        // new bundle
        Bundle serviceBundle = new Bundle();
        serviceBundle.putSerializable(TRAVEL, passengerTravel);
        serviceIntent.putExtras(serviceBundle);
        // start service with th intent and bind it
        startService(serviceIntent);
        Log.d(TAG_LOG, "Bind Service");
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG_LOG, "START");
        // tester l'appel au service
//        myService.requestCarpool();
    }

    @Override
    protected void onResume() {
        Log.d(TAG_LOG, "RESUME");

        IntentFilter filter = new IntentFilter();
        filter.addAction(CarpoolingManagementService.BROADCAST_ACTION);
        registerReceiver(broadcastCarpoolingReceiver, filter);

        IntentFilter filterTravelInit = new IntentFilter();
        filterTravelInit.addAction(CarpoolingManagementService.BROADCAST_TRAVEL_INIT_ACTION);
        registerReceiver(broadcastTravelInitReceiver, filterTravelInit);

        // save current activity as last activity opened
        ActivityRestarterImpl.getInstance().setActivityToRestart(getClass().getName());

        prepareListData();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG_LOG, "PAUSE");

        unregisterReceiver(broadcastCarpoolingReceiver);
        unregisterReceiver(broadcastTravelInitReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG_LOG, "DESTROY");

        unbindService(myConnection);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.titleAbortTravelDialog)
                .setMessage(R.string.textAbortTravelDialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myService.abortTravel();
                        stopServiceAndCloseActivity();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            prepareListData();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        Log.d(TAG_LOG, "prepareListData: enter function. bound: " + isBound + ", init: " + isTravelInit);
        if(isBound && isTravelInit) {
            Log.d(TAG_LOG, "prepareListData: set adapter");
            List<Carpooling> carpoolings = myService.getCarpoolingPossibilities();
            ListAdapter adapter =  new CarpoolingPassengerArrayAdapter(this, R.layout.list_item_carpooling_passenger, carpoolings);

            setListAdapter(adapter);
        }
    }

    /** Boutons de tests */
    public void requestCarpool(int position) {
        Log.d(TAG_LOG, "requestCarpool");
        myService.requestCarpool(myService.getCarpoolingPossibilities().get(position));
    }

    /*
    public void cancelRequest(View view) {
        Log.d(TAG_LOG, "cancelRequest");
        myService.cancelRequest(possibilities.get(0));
    }

    public void abortCarpooling(View view) {
        Log.d(TAG_LOG, "abortCarpooling");
        myService.abortCarpooling(possibilities.get(0));
    }
    */

    /*
    ------------------------------------------------------------------------------------------------
        BROADCAST RECEIVERS:
    ------------------------------------------------------------------------------------------------
    */

    private BroadcastReceiver broadcastCarpoolingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG_LOG, "Broadcast carpool received");
            Toast.makeText(getApplicationContext(), "Broadcast received", Toast.LENGTH_SHORT).show();

            prepareListData();

            searchForAcceptedCarpool();

        }
    };

    private BroadcastReceiver broadcastTravelInitReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG_LOG, "Broadcast travel received");
            isTravelInit = true;
            prepareListData();
        }
    };

    /*
    ------------------------------------------------------------------------------------------------
    */

    private void searchForAcceptedCarpool() {
        for (Carpooling c : myService.getCarpoolingPossibilities()) {
            if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
                Intent intent = new Intent(this,GuidePassenger.class);

            }
        }
    }


    private void stopServiceAndCloseActivity() {
        // stop service
        myService.stopForeground(true);
        myService.stopSelf();

        // save main activity as activity to restart
        ActivityRestarterImpl.getInstance().clearActivityToRestart();

        // open main activity
        Intent intent = new Intent(this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
