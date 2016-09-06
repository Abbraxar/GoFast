package com.i3cnam.gofast.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.views.abstractViews.CourseServiceConnectedActivity;

import java.util.List;

public class CarpoolingDetails extends FragmentActivity implements OnMapReadyCallback {
    public static final String CARPOOLING = "com.i3cnam.gofast.CARPOOLING";
    public static final String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private static final String TAG_LOG = "CarpoolingDetails";
    private GoogleMap mMap;
    private Carpooling carpooling;
    private PassengerTravel travel;
    // maps bounds
    double northest;
    double southest;
    double westest;
    double eastest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_details);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        carpooling = (Carpooling)(bundle.getSerializable(CARPOOLING));
        travel = (PassengerTravel) (bundle.getSerializable(TRAVEL));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // asynchronous computation of both paths
        new TaskComputeAndDrawPath(travel.getOrigin().getCoordinates(),
                carpooling.getPickupPoint(),
                BitmapDescriptorFactory.fromResource(R.drawable.walking),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).execute();
        new TaskComputeAndDrawPath(carpooling.getDropoffPoint(),
                travel.getDestination().getCoordinates(),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).execute();

        // calculate bounds
        double northest = Math.max(travel.getOrigin().getCoordinates().latitude,
                travel.getDestination().getCoordinates().latitude);
        double southest = Math.min(travel.getOrigin().getCoordinates().latitude,
                travel.getDestination().getCoordinates().latitude);
        double westest = Math.max(travel.getOrigin().getCoordinates().longitude,
                travel.getDestination().getCoordinates().longitude);
        double eastest = Math.min(travel.getOrigin().getCoordinates().longitude,
                travel.getDestination().getCoordinates().longitude);

        northest = Math.max(northest,carpooling.getPickupPoint().latitude);
        northest = Math.max(northest,carpooling.getDropoffPoint().latitude);

        southest = Math.min(southest,carpooling.getPickupPoint().latitude);
        southest = Math.min(southest,carpooling.getDropoffPoint().latitude);

        westest = Math.max(westest,carpooling.getPickupPoint().longitude);
        westest = Math.max(westest,carpooling.getDropoffPoint().longitude);

        eastest = Math.min(eastest,carpooling.getPickupPoint().longitude);
        eastest = Math.min(eastest,carpooling.getDropoffPoint().longitude);

        double latMargin = Math.abs(northest - southest) * 0.2;
        double longMargin = Math.abs(westest - eastest) * 0.2;

        LatLngBounds mapBounds = new LatLngBounds( new LatLng(southest - latMargin, eastest - longMargin),
                new LatLng(northest + latMargin, westest + longMargin));

        // set the camera to the calculated bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0));

    }


    /**
     * inner class that computes and draws one path
     */
    private class TaskComputeAndDrawPath extends AsyncTask<String, String,String> {
        private List<LatLng> pathPoints;
        private LatLng origin, destination;
        private BitmapDescriptor originIcon, destinationIcon;

        public TaskComputeAndDrawPath(LatLng origin, LatLng destination, BitmapDescriptor originIcon, BitmapDescriptor destinationIcon) {
            this.origin = origin;
            this.destination = destination;
            this.originIcon = originIcon;
            this.destinationIcon = destinationIcon;
        }

        protected String doInBackground(String... urls) {
            // calculate the path between the two points

            DirectionsService path = new DirectionsService();
            path.setOrigin(origin);
            path.setDestination(destination);
            path.setMode("walking");
            path.computeDirections();
            pathPoints = path.getPathPoints();
            return null;
        }

        protected void onPostExecute(String result) {
            // draw the path
            Polyline pathPolyline = mMap.addPolyline(new PolylineOptions());
            pathPolyline.setPoints(pathPoints);
            pathPolyline.setColor(Color.GREEN);

            mMap.addMarker(new MarkerOptions().position(origin).icon(originIcon));
            mMap.addMarker(new MarkerOptions().position(destination).icon(destinationIcon));
        }
    }
}
