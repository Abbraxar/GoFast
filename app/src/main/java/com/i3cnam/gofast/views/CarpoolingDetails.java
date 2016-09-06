package com.i3cnam.gofast.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.views.abstractViews.CourseServiceConnectedActivity;

import java.util.List;

public class CarpoolingDetails extends FragmentActivity implements OnMapReadyCallback {
    public static final String CARPOOLING = "com.i3cnam.gofast.CARPOOLING";
    private static final String TAG_LOG = "CarpoolingDetails";
    private GoogleMap mMap;
    private Carpooling carpooling;


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        new TaskComputeAndDrawPath().execute();
    }


    /**
     * inner class that computes and draws the path
     */
    private class TaskComputeAndDrawPath extends AsyncTask<String, String,String> {
        private List<LatLng> pathPoints;
        protected String doInBackground(String... urls) {
            // calculate the path between the two points
            Log.d(TAG_LOG, carpooling.getPassengerTravel().getOrigin().getCoordinates().toString());
            Log.d(TAG_LOG, carpooling.getPickupPoint().toString());

            DirectionsService path = new DirectionsService();
            path.setOrigin(carpooling.getPassengerTravel().getOrigin().getCoordinates());
            path.setDestination(carpooling.getPickupPoint());
            path.setMode("walking");
            path.computeDirections();
            String encodedPoints = path.getEncodedPolyline();
            pathPoints = path.getPathPoints();
            return null;
        }
        protected void onPostExecute(String result) {
            // draw the path
            Polyline pathPolyline = mMap.addPolyline(new PolylineOptions());
            pathPolyline.setPoints(pathPoints);

//            homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50));
        }
    }
}


