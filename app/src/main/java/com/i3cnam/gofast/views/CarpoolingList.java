package com.i3cnam.gofast.views;

/**
 * Created by nadege on 08/07/16.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService.LocalBinder;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;


public class CarpoolingList extends FragmentActivity implements OnMapReadyCallback {

    /** variables globales */
    private GoogleMap mMap;
    public final static String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private PassengerTravel passengerTravel;
    CarpoolingManagementService myService;
    boolean isBound = false;
    // pour la liste
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private final String TAG_LOG = "Carpooling View";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_list);

        // get all the data of the intent and create a new travel object
        passengerTravel = new PassengerTravel();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        passengerTravel.setOrigin((Place)bundle.getSerializable(DestinationMap.ORIGIN));
        passengerTravel.setDestination((Place)bundle.getSerializable(EnterDestination.DESTINATION));
        passengerTravel.setPassenger(User.getMe());
        passengerTravel.setRadius(intent.getIntExtra(EnterDestination.RADIUS,500));

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

        // test du stub communication
        CommInterface serverCom = new CommunicationStub();
        System.out.println("Send request");
        List<Carpooling> possibilities = serverCom.findCarpoolingPossibilities(passengerTravel);
        System.out.println("Request sent");

        for (Carpooling onePossibility: possibilities) {
            System.out.println("====================== C A R P O O L I N G ======================");
            System.out.println(onePossibility.getPickupPoint());
            System.out.println(onePossibility.getDropoffPoint());
            System.out.println(onePossibility.getPickupTime());
        }


        // Renvoi vers le ExpandableListView de activity_passenger_result
       expListView = (ExpandableListView) findViewById(R.id.covoitResult);

        // preparing list data
        prepareListData();

        listAdapter = new PassengerResultExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG_LOG, "ON_START");
        // tester l'appel au service

//        myService.requestCarpool();

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
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding child data
        listDataHeader.add("Covoiturage 1");
        listDataHeader.add("Covoiturage 2");
        listDataHeader.add("Covoiturage 3");

        // Adding child data
        List<String> covoiturage1 = new ArrayList<>();
        covoiturage1.add("map à venir");

        List<String> covoiturage2 = new ArrayList<>();
        covoiturage2.add("map à venir");


        List<String> covoiturage3 = new ArrayList<>();
        covoiturage3.add("map à venir");

        listDataChild.put(listDataHeader.get(0), covoiturage1); // Header, Child data
        listDataChild.put(listDataHeader.get(1), covoiturage2);
        listDataChild.put(listDataHeader.get(2), covoiturage3);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /** Boutons de tests */
    public void requestCarpool(View view) {
        Log.d(TAG_LOG, "requestCarpool");
        myService.requestCarpool();

    }

    public void cancelRequest(View view) {
        Log.d(TAG_LOG, "cancelRequest");
        myService.cancelRequest();
    }

    public void abortCarpooling(View view) {
        Log.d(TAG_LOG, "abortCarpooling");
        myService.abortCarpooling();


    }



}
