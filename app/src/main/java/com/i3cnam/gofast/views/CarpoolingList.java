package com.i3cnam.gofast.views;

/**
 * Created by nadege on 08/07/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.tools.activityRestarter.ActivityRestarterImpl;
import com.i3cnam.gofast.views.abstractViews.TravelServiceConnectedActivity;

import java.util.ArrayList;
import java.util.List;


public class CarpoolingList extends TravelServiceConnectedActivity {

    private final String TAG_LOG = "CarpoolingList";
    private ListView myListView;
    private CarpoolingPassengerArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_list);

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

        myListView = (ListView) findViewById(R.id.carpoolsListView);
        adapter =  new CarpoolingPassengerArrayAdapter(this, R.layout.list_item_carpooling_passenger, new ArrayList<Carpooling>());
        myListView.setAdapter(adapter);

        launchAndBindService(passengerTravel);
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

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        View empty = findViewById(R.id.empty);
        ListView list = (ListView) findViewById(R.id.carpoolsListView);
        list.setEmptyView(empty);
    }


    /*
     * Preparing the list data
     */
    private void prepareListData() {
        Log.d(TAG_LOG, "prepareListData: enter function. bound: " + isBound + ", init: " + isDataInit);

        if(isBound && isDataInit) {
            Log.d(TAG_LOG, "prepareListData: enter function. myService: " + myService + ", cp: " + myService.getCarpoolingPossibilities().size());
            Log.d(TAG_LOG, "prepareListData: set adapter");
            List<Carpooling> carpoolings = myService.getCarpoolingPossibilities();

            ListAdapter adapter =  new CarpoolingPassengerArrayAdapter(this, R.layout.list_item_carpooling_passenger, carpoolings);
            myListView.setAdapter(adapter);
//            adapter.setCarpoolings(carpoolings);
        }
    }

    /**
     * public getter for carpooling details fragment
     * @return
     */
    public PassengerTravel getTravel() {
        return myService.getTravel();
    }

    /** Boutons de tests */
    public void requestCarpool(int position) {
        Log.d(TAG_LOG, "requestCarpool");
        myService.requestCarpool(myService.getCarpoolingPossibilities().get(position));
    }

    public void cancelRequest(int position) {
        Log.d(TAG_LOG, "cancelRequest");
        myService.cancelRequest(myService.getCarpoolingPossibilities().get(position));
    }

    public void abortCarpooling(int position) {
        Log.d(TAG_LOG, "abortCarpooling");
        myService.abortCarpool(myService.getCarpoolingPossibilities().get(position));
    }

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
            isDataInit = true;
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


    @Override
    protected void afterServiceConnected() {
        prepareListData();
    }
}
