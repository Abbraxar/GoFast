package com.i3cnam.gofast.activities;

/**
 * Created by nadege on 08/07/16.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.PlaceClass;
import com.i3cnam.gofast.model.User;


public class CarpoolingList extends FragmentActivity implements OnMapReadyCallback {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private GoogleMap mMap;
    public final static String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private PassengerTravel passengerTravel;
    private ListView carpoolingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_list);

        passengerTravel = new PassengerTravel();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        passengerTravel.setOrigin((PlaceClass)bundle.getSerializable(DestinationMap.ORIGIN));
        passengerTravel.setDestination((PlaceClass)bundle.getSerializable(EnterDestination.DESTINATION));
        passengerTravel.setUser(User.getMe());
        passengerTravel.setRadius(intent.getIntExtra(EnterDestination.RADIUS,500));

        //carpoolingsList = (ListView) findViewById(R.id.carpoolingsList);

//        intent = new Intent(this, CarpoolingManagementService.class);
        /*
        bundle = new Bundle();
        bundle.putSerializable(TRAVEL, passengerTravel);
        intent.putExtras(bundle);
*/
//        startService(intent);
        System.out.println("Send request");
        List<Carpooling> possibilities = Communication.findCarpoolingPossibilities(passengerTravel);
        System.out.println("Request sent");


        for (Carpooling onePossibility: possibilities) {
            System.out.println("====================== C A R P O O L I N G ======================");
            System.out.println(onePossibility.getPickup_point());
        }

        System.out.println("Finish");

        // Renvoi vers le ExpandableListView de activity_passenger_result
       expListView = (ExpandableListView) findViewById(R.id.covoitResult);

        // preparing list data
        prepareListData();

        listAdapter = new PassengerResultExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
       listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Covoiturage 1");
        listDataHeader.add("Covoiturage 2");
        listDataHeader.add("Covoiturage 3");

        // Adding child data
        List<String> covoiturage1 = new ArrayList<String>();
        covoiturage1.add("map à venir");

        List<String> covoiturage2 = new ArrayList<String>();
        covoiturage2.add("map à venir");


        List<String> covoiturage3 = new ArrayList<String>();
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
}
