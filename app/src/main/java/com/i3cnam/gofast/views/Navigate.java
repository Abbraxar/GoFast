package com.i3cnam.gofast.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.LocationService;
import com.i3cnam.gofast.management.course.CourseManagementService;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.tools.activityRestarter.ActivityRestarterImpl;
import com.i3cnam.gofast.views.abstractViews.CourseServiceConnectedActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Navigate extends CourseServiceConnectedActivity implements OnMapReadyCallback {

//    public final static String COURSE = "com.i3cnam.gofast.COURSE";
//    CourseManagementService myService;
//    boolean isBound = false;
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    Polyline pathPolyline;
    Marker homeMarker;
    Marker destinationMarker;
    ProgressBar waitingSignal;
    RelativeLayout newDemandDialog;
    RelativeLayout ongoingCarpoolsLayout;
    ImageView showOnoingCarpoolsButton;
    Marker requestedCarpoolPickupMarker;
    Marker requestedCarpoolDropoffMarker;

    Carpooling newRequestedCarpool;

    List<Integer> acceptedCarpoolsIndexes = new ArrayList<>();
    List<Integer> conflictCarpoolsIndexes = new ArrayList<>();
    List<Integer> achievedCarpoolsIndexes = new ArrayList<>();
    List<Marker> pickUpPointMarkers = new ArrayList<>();
    List<Marker> dropoffPointMarkers = new ArrayList<>();

    boolean ongoingCarpoolsVisible = false;

    boolean mapIsReady = false; // for synchronisation
    boolean courseIsInitialised = false; // for synchronisation

    private final static String TAG_LOG = "Navigate view";
    Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get params from Intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        DriverCourse driverCourse = new DriverCourse();
        if (bundle != null) {
            // instanciate driver course
            driverCourse.setOrigin((Place) bundle.getSerializable(DestinationMap.ORIGIN));
            driverCourse.setDestination((Place) bundle.getSerializable(EnterDestination.DESTINATION));
            driverCourse.setDriver(User.getMe(this));
            driverCourse.setActualPosition(driverCourse.getOrigin().getCoordinates());
            driverCourse.setPositioningTime(new java.util.Date());
            driverCourse.setEncodedPoints(intent.getStringExtra(DestinationMap.ENCODED_POINTS));
        }

        // wait signal
        waitingSignal = (ProgressBar) findViewById(R.id.waiting);
        waitingSignal.setVisibility(View.VISIBLE);
        showOnoingCarpoolsButton = (ImageView) findViewById(R.id.hitchingImg);
        showOnoingCarpoolsButton.setVisibility(View.INVISIBLE);

        //get context for other classes
        thisContext = this;

        // launch and bind CourseManagementService
        launchAndBindService(driverCourse);
    }


    /*
    ------------------------------------------------------------------------------------------------
        ACTIVITY STATE CHANGES:
    ------------------------------------------------------------------------------------------------
     */
    @Override
    protected void afterServiceConnected() {

    }

    @Override
    protected void onResume() {
        Log.d(TAG_LOG, "RESUME");

        // for the course init
        IntentFilter initFilter = new IntentFilter();
        initFilter.addAction(CourseManagementService.BROADCAST_INIT_COURSE_ACTION);
        registerReceiver(broadcastCourseInitReceiver, initFilter);


        // for the course init
        IntentFilter noServerFilter = new IntentFilter();
        noServerFilter.addAction(CourseManagementService.BROADCAST_SERVER_UNAVAILABLE);
        registerReceiver(broadcastServerUnavailableReceiver, noServerFilter);

        // for the course changes
        IntentFilter courseFilter = new IntentFilter();
        courseFilter.addAction(CourseManagementService.BROADCAST_UPDATE_COURSE_ACTION);
        registerReceiver(broadcastCourseReceiver, courseFilter);

        // for the carpooling request changes
        IntentFilter carpoolingFilter = new IntentFilter();
        carpoolingFilter.addAction(CourseManagementService.BROADCAST_UPDATE_CARPOOLING_ACTION);
        registerReceiver(broadcastCarpoolingReceiver, carpoolingFilter);

        // save current activity as last activity opened
        ActivityRestarterImpl.getInstance().setActivityToRestart(getClass().getName());

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG_LOG, "PAUSE");
        unregisterReceiver(broadcastCarpoolingReceiver);
        unregisterReceiver(broadcastCourseReceiver);
        unregisterReceiver(broadcastCourseInitReceiver);
        unregisterReceiver(broadcastServerUnavailableReceiver);

        super.onPause();
    }


    @Override
    public void onBackPressed() {
          new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.titleAbortCourseDialog)
                .setMessage(R.string.textAbortCourseDialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isBound) {
                          myService.abortCourse();
                        }

                        stopServiceAndCloseActivity();

                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
       }


    /*
    ------------------------------------------------------------------------------------------------
        USER ACTIONS ON CARPOOLS:
    ------------------------------------------------------------------------------------------------
     */
    /** Action : pressed Accept button */
    public void acceptCarpool(View view) {
        Log.d(TAG_LOG, "acceptCarpool");
        // do the action to accept the carpooling
        myService.acceptCarpooling(newRequestedCarpool);

        // hide dialog
        newDemandDialog.setVisibility(View.INVISIBLE);

        // remove markers
        requestedCarpoolPickupMarker.remove();
        requestedCarpoolDropoffMarker.remove();
    }

    /** Action : pressed Refuse button */
    public void refuseCarpool(View view) {
        Log.d(TAG_LOG, "refuseCarpool");
        // do the action to refuse the carpooling
        myService.refuseCarpooling(newRequestedCarpool);

        // hide dialog
        newDemandDialog.setVisibility(View.INVISIBLE);

        // remove markers
        requestedCarpoolPickupMarker.remove();
        requestedCarpoolDropoffMarker.remove();
    }


    /*
    ------------------------------------------------------------------------------------------------
        OTHER USER ACTIONS :
    ------------------------------------------------------------------------------------------------
     */
    public void showOngoingCarpools(View view) {
        Log.d(TAG_LOG, "showOngoingCarpools");
        // change state of visibility
        ongoingCarpoolsVisible = !ongoingCarpoolsVisible;
        // get layer
        ongoingCarpoolsLayout = (RelativeLayout) findViewById(R.id.ongoingCarpoolsLayout);

        int rowViewMaxWidth = ongoingCarpoolsLayout.getWidth() - 100;

        if (ongoingCarpoolsVisible) {

            LayoutInflater inflater;
            View rowView;
            TextView passenger, pickupInfo, dropOffInfo, pickupTime, fare ;
            DateFormat formatDate = new SimpleDateFormat("HH:mm");
            int bottomMargin = 0;


            for (Carpooling c : myService.getRequestedCarpoolings()) {
                if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
                    inflater = (LayoutInflater) this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.list_item_carpooling_driver, ongoingCarpoolsLayout, false);



                    passenger = (TextView) rowView.findViewById(R.id.passenger);
                    pickupInfo = (TextView) rowView.findViewById(R.id.pickupInfo);
                    dropOffInfo = (TextView) rowView.findViewById(R.id.dropOffInfo);
                    pickupTime = (TextView) rowView.findViewById(R.id.pickupTime);
                    fare = (TextView) rowView.findViewById(R.id.fare);

                    passenger.setText(c.getPassenger().getNickname());
                    pickupTime.setText(formatDate.format(c.getPickupTime()));
                    fare.setText("€ " + c.getFare());

                    new TryToCompletePlaceName(null,
                            pickupInfo,
                            null)
                            .execute(c.getPickupPoint());

                    new TryToCompletePlaceName(null,
                            dropOffInfo,
                            null)
                            .execute(c.getDropoffPoint());

                    ongoingCarpoolsLayout.addView(rowView);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT));
                    // Align bottom-right, and add bottom-margin
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.bottomMargin = bottomMargin;
                    params.leftMargin = 100;
                    rowView.setLayoutParams(params);
                    bottomMargin += 370;
                }
            }
        }
        else {
            // remove all text views
            for (int i = 0 ; i < ongoingCarpoolsLayout.getChildCount() ; i++) {
                if (ongoingCarpoolsLayout.getChildAt(i) instanceof RelativeLayout) {
                    ongoingCarpoolsLayout.removeViewAt(i);
                    i--;
                }
            }
        }

    }


    /*
    ------------------------------------------------------------------------------------------------
        BROADCAST RECEIVERS:
    ------------------------------------------------------------------------------------------------
     */
    /**
     * Event : The course object has been initialised into service
     */
    private BroadcastReceiver broadcastCourseInitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update the boolean and attempt to init the map
            isDataInit = true;
            initMap();
            handleCarpoolingChanges();
        }
    };

    /**
     * Event : The server is not available
     */
    private BroadcastReceiver broadcastServerUnavailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean available = intent.getBooleanExtra("AVAILABLE",true);
            Log.d("BroadcastReceiver", "Server available " + available);

            TextView tv = (TextView) findViewById(R.id.serverUnavailable);
            // show the message if not available
            if (available) {
                tv.setVisibility(View.INVISIBLE);
                waitingSignal.setVisibility(View.INVISIBLE);
            }
            else {
                tv.setVisibility(View.VISIBLE);
                waitingSignal.setVisibility(View.VISIBLE);
            }

        }
    };


    /**
     * Event : The position of the user has benn updated ; the path has been updated
     */
    private BroadcastReceiver broadcastCourseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
            Log.d("BroadcastReceiver", "Broadcast received");

//            Toast.makeText(getApplicationContext(), "Course received", Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), "New position is : \n" + myService.getDriverCourse().getActualPosition(), Toast.LENGTH_LONG).show();

            homeMarker.setPosition(myService.getDriverCourse().getActualPosition());
            pathPolyline.setPoints(PolyUtil.decode(myService.getDriverCourse().getEncodedPoints()));
        }
    };

    /**
     * Event : at least one carpooling has changed
     */
    private BroadcastReceiver broadcastCarpoolingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleCarpoolingChanges();
        }
    };


    /*
    ------------------------------------------------------------------------------------------------
    */

    /**
     * Update view from carpooling changes
     */
    private void handleCarpoolingChanges() {
        // TODO
        Log.d("BroadcastReceiver", "Broadcast received");
        Toast.makeText(getApplicationContext(), "Carpooling received", Toast.LENGTH_SHORT).show();

        String s;
        if (isBound) {
            for (Carpooling c : myService.getRequestedCarpoolings()) {
                s = "Carpooling " + c.getId() + "\n" +
                        "pick up: " + c.getPickupPoint() + "\n" +
                        "drop off: " + c.getDropoffPoint() + "\n" +
                        "time: " + c.getPickupTime() + "\n" +
                        "state: " + c.getState() + "\n" +
                        "fare: " + c.getFare() + "\n";

                Log.d("BroadcastReceiver", s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

                if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
                    // NEW CARPOOL DEMAND
                    newRequestedCarpool = c;
                    // instantiate dialog
                    newDemandDialog = (RelativeLayout) findViewById(R.id.carpoolingDemandDialog);
                    // show dialog
                    newDemandDialog.setVisibility(View.VISIBLE);

                    // add markers (pick up and drop off)
                    requestedCarpoolPickupMarker = mMap.addMarker(new MarkerOptions().
                            position(c.getPickupPoint()).title(getString(R.string.pickupLabel)));

                    requestedCarpoolDropoffMarker = mMap.addMarker(new MarkerOptions().
                            position(c.getDropoffPoint()).title(getString(R.string.dropoffLabel)));

                    // try to find places names
                    new TryToCompletePlaceName(requestedCarpoolPickupMarker,
                            (TextView) findViewById(R.id.carpoolingPickupText),
                            getString(R.string.pickupLabel))
                            .execute(requestedCarpoolPickupMarker.getPosition());
                    new TryToCompletePlaceName(requestedCarpoolDropoffMarker,
                            (TextView) findViewById(R.id.carpoolingDropoffText),
                            getString(R.string.dropoffLabel))
                            .execute(requestedCarpoolDropoffMarker.getPosition());

                    // set markers green
                    requestedCarpoolPickupMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    requestedCarpoolDropoffMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
                    if (!acceptedCarpoolsIndexes.contains(c.getId())) {
                        // add to list
                        acceptedCarpoolsIndexes.add(c.getId());
                        pickUpPointMarkers.add(mMap.addMarker(new MarkerOptions().
                                position(c.getPickupPoint()).title(c.getPassenger().getNickname() + " " + getString(R.string.pickupLabel))));
                        dropoffPointMarkers.add(mMap.addMarker(new MarkerOptions().
                                position(c.getDropoffPoint()).title(c.getPassenger().getNickname() + " " +  getString(R.string.dropoffLabel))));

                        // try to find places names
                        new TryToCompletePlaceName(pickUpPointMarkers.get(pickUpPointMarkers.size()-1),
                                (TextView) findViewById(R.id.carpoolingPickupText),
                                getString(R.string.pickupLabel))
                                .execute(c.getPickupPoint());
                        new TryToCompletePlaceName(dropoffPointMarkers.get(dropoffPointMarkers.size()-1),
                                (TextView) findViewById(R.id.carpoolingDropoffText),
                                getString(R.string.dropoffLabel))
                                .execute(c.getDropoffPoint());

                        // set markers blue
                        pickUpPointMarkers.get(pickUpPointMarkers.size()-1).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        dropoffPointMarkers.get(dropoffPointMarkers.size()-1).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    }

                } else if (c.getState().equals(Carpooling.CarpoolingState.CONFLICT)) {
                    if (!conflictCarpoolsIndexes.contains(c.getId())) {
                        conflictCarpoolsIndexes.add(c.getId());
                        if (acceptedCarpoolsIndexes.contains(c.getId())) {
                            // notifier

                            new AlertDialog.Builder(thisContext)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(R.string.canceledCarpoolTitle)
                                    .setMessage(c.getPassenger().getNickname() + getString(R.string.canceledCarpoolText))
                                    .setPositiveButton(R.string.ok, null)
                                    .show();

                            int index = acceptedCarpoolsIndexes.indexOf(c.getId());
                            Marker oneMarker = pickUpPointMarkers.get(index);
                            oneMarker.remove();
                            oneMarker = dropoffPointMarkers.get(index);
                            oneMarker.remove();

                            acceptedCarpoolsIndexes.remove(index);
                            pickUpPointMarkers.remove(index);
                            dropoffPointMarkers.remove(index);

                        } else if (achievedCarpoolsIndexes.contains(c.getId())) {
                            // notifier


                            new AlertDialog.Builder(thisContext)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(R.string.conflictTitle)
                                    .setMessage(c.getPassenger().getNickname() + getString(R.string.conflictText))
                                    .setPositiveButton(R.string.ok, null)
                                    .show();


                            achievedCarpoolsIndexes.remove(c.getId());
                        }
                    }
                } else if (c.getState().equals(Carpooling.CarpoolingState.ACHIEVED)) {
                    if (!achievedCarpoolsIndexes.contains(c.getId())) {
                        achievedCarpoolsIndexes.add(c.getId());
                    }

                    if (acceptedCarpoolsIndexes.contains(c.getId())) {
                        // notifier

                        new AlertDialog.Builder(thisContext)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("Covoiturage terminé")
                                .setMessage(c.getPassenger().getNickname() + " a fini son covoiturage")
                                .setPositiveButton(R.string.ok, null)
                                .show();

                        int index = acceptedCarpoolsIndexes.indexOf(c.getId());
                        Marker oneMarker = pickUpPointMarkers.get(index);
                        oneMarker.remove();
                        oneMarker = dropoffPointMarkers.get(index);
                        oneMarker.remove();

                        acceptedCarpoolsIndexes.remove(index);
                        pickUpPointMarkers.remove(index);
                        dropoffPointMarkers.remove(index);

                    }

                }

                // show or hide button
                if (acceptedCarpoolsIndexes.size() > 0) {
                    showOnoingCarpoolsButton.setVisibility(View.VISIBLE);
                }
                else {
                    showOnoingCarpoolsButton.setVisibility(View.INVISIBLE);
                }

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // update the boolean and attempt to init the map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationService.getActualLocation(this)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(6));
        mapIsReady = true;
        initMap();
    }

    /**
     * Init map method :
     * Il will only be activated while mapIsReady AND isDataInit AND isBound variables are true
     */
    public void initMap(){
        Log.d("NAV", (isBound ? "bound" : "not bound"));
        Log.d("NAV", (super.isBound ? "super.isBound" : "not super.isBound"));
        Log.d("NAV", (mapIsReady ? "mapIsReady" : "not mapIsReady"));
        Log.d("NAV", (isDataInit ? "isDataInit" : "not isDataInit"));
        if (mapIsReady && isDataInit && isBound) {
            // stop waiting
            waitingSignal.setVisibility(View.INVISIBLE);

            if (myService.getDriverCourse().getDestination() != null) {
                Log.d("NAV", ("deiver course not null"));
                DriverCourse course = myService.getDriverCourse();
                LatLng actualPosition = course.getActualPosition();
                Place destination = course.getDestination();
//                Place origin = course.getOrigin();

                Log.d("NAV", ("draw origin"));

                // set the ORIGIN marker
                homeMarker = mMap.addMarker(new MarkerOptions().position(actualPosition).title(getString(R.string.yourPossition)));
                // set the destination marker
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destination.getCoordinates())
                        .title(getResources().getString(R.string.destination_title))
                        .snippet(destination.getPlaceName()));
                destinationMarker.showInfoWindow();

                Log.d("NAV", ("draw path"));

                // draw the path
                List<LatLng> pathPoints = PolyUtil.decode(course.getEncodedPoints());
                pathPolyline = mMap.addPolyline(new PolylineOptions());
                pathPolyline.setPoints(pathPoints);
                homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50));

                // zoom map
                double northest = Math.max(destination.getCoordinates().latitude, actualPosition.latitude);
                double southest = Math.min(destination.getCoordinates().latitude, actualPosition.latitude);
                double westest = Math.max(destination.getCoordinates().longitude, actualPosition.longitude);
                double eastest = Math.min(destination.getCoordinates().longitude, actualPosition.longitude);

                double latMargin = Math.abs(northest - southest) * 0.2;
                double longMargin = Math.abs(westest - eastest) * 0.2;

                LatLngBounds mapBounds = new LatLngBounds( new LatLng(southest - latMargin, eastest - longMargin),
                        new LatLng(northest + latMargin, westest + longMargin));
                // set the camera to the calculated bounds
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0));

            }
            else {
                Log.d("NAV", ("deiver course null"));
                // si malgré tout on n'a pas d'objet course, on quite la vue

                stopServiceAndCloseActivity();
            }
        }
    }
}