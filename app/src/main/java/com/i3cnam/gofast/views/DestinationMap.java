package com.i3cnam.gofast.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.geo.LocationService;
import com.i3cnam.gofast.model.Place;

import java.util.List;

public class DestinationMap extends FragmentActivity implements OnMapReadyCallback {

    public final static String ORIGIN = "com.i3cnam.gofast.ORIGIN";
    public final static String ENCODED_POINTS = "com.i3cnam.gofast.ENCODED_POINTS";
    private Place destination;
    private Place origin;
    private int radius;
    private String encodedPoints;
    private GoogleMap mMap;
    private String userType;
    private DirectionsService path;
    private LatLngBounds mapBounds;
    private List<LatLng> pathPoints;
    private Marker homeMarker, destinationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("ON CREATE");
        setContentView(R.layout.activity_destination_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.destination = (Place)bundle.getSerializable(EnterDestination.DESTINATION);
        this.userType = intent.getStringExtra(Main.USER_TYPE);
        this.radius = intent.getIntExtra(EnterDestination.RADIUS,500);

        /*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
*/
//        destination.getCoordinates();
        TaskGetCoordinates MaTask =  new TaskGetCoordinates();
        MaTask.execute("");

    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("ON_RESUME");
//        TaskGetCoordinates MaTask =  new TaskGetCoordinates();
//        MaTask.execute("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("ON_START");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("ON_RESTART");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("ON_STOP");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("ON_PAUSE");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ON_DESTROY");
    }

    private class TaskGetCoordinates extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
//            System.out.println(":::::: Destination coordinates : " + destination.getCoordinates());
            destination.getCoordinates();
            return null;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("ON_MAP_READY");
        mMap = googleMap;

        // get the position of the device
        LatLng devicePosition = LocationService.getActualLocation(this);

        if (devicePosition == null) {
//          JE SUIS PLACE DU CAPITOLE
            devicePosition = new LatLng(43.6032661,1.4422609);
        }
        origin = new Place(devicePosition);

        // set the bounds for the map
        new TaskZoomMap().execute("");

        // set the ORIGIN marker
        homeMarker = mMap.addMarker(new MarkerOptions().position(devicePosition).title(getResources().getString(R.string.origin_title)));
        // set the destination marker
        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination.getCoordinates())
                .title(getResources().getString(R.string.destination_title))
                .snippet(destination.getPlaceName()));
        destinationMarker.showInfoWindow();

        if (userType.equals("passenger")) {
            // draw a circle with the desired radius to both markers
            mMap.addCircle(new CircleOptions()
                    .center(devicePosition)
                    .radius(radius)
                    .strokeColor(0x7F00AA00)
                    .fillColor(0x1F00AA00)
            );
            mMap.addCircle(new CircleOptions()
                    .center(destination.getCoordinates())
                    .radius(radius)
                    .strokeColor(0x7F00AA00)
                    .fillColor(0x1F00AA00)
            );
            // change the icon of the origin marker
            homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pedestrian_50));
        }
        else if (userType.equals("driver")) {

            // launch an asynchronous task to compute and draw the path
            new TaskComputeAndDrawPath().execute("");

        }


    }

    /**
     * inner class that computes and draws the path
     */
    private class TaskComputeAndDrawPath extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            // calculate the path between the two points
            System.out.println(origin);
            System.out.println(destination);

            path = new DirectionsService();
            path.setOrigin(origin);
            path.setDestination(destination);
            path.computeDirections();
            encodedPoints = path.getEncodedPolyline();
            pathPoints = path.getPathPoints();
            return null;
        }
        protected void onPostExecute(String result) {
            // draw the path
            Polyline pathPolyline = mMap.addPolyline(new PolylineOptions());
            pathPolyline.setPoints(pathPoints);
            /*
            // rotate the car icon
            //      (the cap function calculates the cap with the reference of the north pole
            //       and clockwise then, we need to add 270 deg)
            float angle = (float)(Operations.toDegrees(Operations.cap(pathPoints.get(0),pathPoints.get(1))));
            angle += 270;
            while (angle > 360 ) { angle -= 360; }
            homeMarker.setRotation(angle);

            // change the icon of the origin marker
            if (angle > 90 && angle < 270) {
                homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50_flip));
            }
            else {
                homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50));
            }
            */
            homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50));
        }
    }


    private class TaskZoomMap extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            destination.getCoordinates();
            return null;
        }
        protected void onPostExecute(String result) {
            double northest = Math.max(destination.getCoordinates().latitude, origin.getCoordinates().latitude);
            double southest = Math.min(destination.getCoordinates().latitude, origin.getCoordinates().latitude);
            double westest = Math.max(destination.getCoordinates().longitude, origin.getCoordinates().longitude);
            double eastest = Math.min(destination.getCoordinates().longitude, origin.getCoordinates().longitude);

            double latMargin = Math.abs(northest - southest) * 0.2;
            double longMargin = Math.abs(westest - eastest) * 0.2;

            mapBounds = new LatLngBounds( new LatLng(southest - latMargin, eastest - longMargin),
                                        new LatLng(northest + latMargin, westest + longMargin));
            // set the camera to the calculated bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0));

        }
    }

    public void closeMap(View view) {
        System.out.println("closing activity");
        this.finish();
    }

    public void validate(View view) {

        Intent intent;

        if (userType.equals("driver")) {
            intent = new Intent(this, Navigate.class);
            intent.putExtra(ENCODED_POINTS, encodedPoints);
        }
        else {
            intent = new Intent(this, CarpoolingList.class);
            intent.putExtra(EnterDestination.RADIUS, radius);
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(EnterDestination.DESTINATION, destination);
        bundle.putSerializable(ORIGIN, origin);

        intent.putExtras(bundle);
        startActivity(intent);
    }
}
