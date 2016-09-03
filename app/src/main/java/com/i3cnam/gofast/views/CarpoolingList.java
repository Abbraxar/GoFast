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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService.LocalBinder;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;

import java.util.ArrayList;
import java.util.List;


public class CarpoolingList extends ListActivity {

    /** globals */
    public final static String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private PassengerTravel passengerTravel;
    CarpoolingManagementService myService;
    boolean isBound = false;

    private final String TAG_LOG = "Carpooling View";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all the data of the intent and create a new travel object
        passengerTravel = new PassengerTravel();

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
        registerReceiver(broadcastReceiver, filter);

        // save current activity as last activity opened
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastActivity", getClass().getName());
        editor.commit();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG_LOG, "PAUSE");

        unregisterReceiver(broadcastReceiver);
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
                        stopServiceAndCloseAvtivity();
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
        List<Carpooling> carpoolings = myService.getCarpoolingPossibilities();
        ListAdapter adapter =  new CarpoolingPassengerArrayAdapter(this, R.layout.list_item_carpooling_passenger, carpoolings);

        setListAdapter(adapter);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "Broadcast received");
            Toast.makeText(getApplicationContext(), "Broadcast received", Toast.LENGTH_SHORT).show();

            prepareListData();
        }
    };

    /*
    ------------------------------------------------------------------------------------------------
    */

    private void stopServiceAndCloseAvtivity() {
        // stop service
        myService.stopForeground(true);
        myService.stopSelf();

        // save main activity as activity to restart
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("lastActivity");
        editor.commit();

        // open main activity
        Intent intent = new Intent(this, Main.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
