package com.i3cnam.gofast.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.destination = (Place)bundle.getSerializable(EnterDestination.DESTINATION);
        this.userType = intent.getStringExtra(Main.USER_TYPE);
//        this.radius = Integer.parseInt(intent.getStringExtra(EnterDestination.RADIUS));
        this.radius = intent.getIntExtra(EnterDestination.RADIUS,500);


        System.out.println("===========   CONSIDERING: ");
        System.out.println(Main.USER_TYPE + " = " + userType);
        System.out.println(EnterDestination.RADIUS + " = " + radius);

        System.out.println("==== Destination: ");
//        System.out.println(destination.getPlaceId());
        System.out.println(destination.getPlaceName());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        destination.getCoordinates();
        System.out.println(destination.getPlaceId());

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // get the position of the device
        LatLng devicePosition = LocationService.getActualLocation();
        origin = new Place(devicePosition);

        // set the bounds for the map
        LatLngBounds mapBounds = new LatLngBounds(
                new LatLng(Math.min(destination.getCoordinates().latitude, devicePosition.latitude) - 0.02,
                        Math.min(destination.getCoordinates().longitude, devicePosition.longitude) - 0.02),
                new LatLng(Math.max(destination.getCoordinates().latitude, devicePosition.latitude) + 0.02,
                        Math.max(destination.getCoordinates().longitude, devicePosition.longitude) + 0.02));

        // set the ORIGIN marker
        Marker homeMarker = mMap.addMarker(new MarkerOptions().position(devicePosition).title("You are here"));

        // set the destination marker
        Marker destinationMarker = mMap.addMarker(new MarkerOptions().position(destination.getCoordinates())
                .title("You want to arrive here")
                .snippet(destination.getPlaceName()));
        destinationMarker.showInfoWindow();

        // set the camera to the calculated bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0));

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

            // calculate the path between the two points
            path = new DirectionsService();
            path.setDestination(destination);
            path.computeDirections();
            encodedPoints = path.getEncodedPolyline();

            // draw the path
            Polyline myPolyline = mMap.addPolyline(new PolylineOptions());
            myPolyline.setPoints(path.getPathPoints());

            // change the icon of the origin marker
            homeMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.driver_50));
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
