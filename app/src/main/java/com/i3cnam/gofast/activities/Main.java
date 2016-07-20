package com.i3cnam.gofast.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.i3cnam.gofast.R;

import java.util.List;

public class Main extends AppCompatActivity {
    public final static String USER_TYPE = "com.i3cnam.gofast.USER_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> myProviders = locationManager.getAllProviders();

        for (String oneProvider : myProviders) {
            System.out.println(oneProvider);
        }

//        LocationProvider locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

//        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,0,0) == PackageManager.PERMISSION_GRANTED) {
//            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }
        System.out.println("=============LOCATION=============");

        Context context ;
        context = getApplicationContext();

        System.out.println("=============ACCESS_FINE_LOCATION:" + PermissionChecker.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) + "=============");
        System.out.println("=============PERMISSION_GRANTED:" + PackageManager.PERMISSION_GRANTED + "=============");

        if (ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            System.out.println("=============PERMISSION OK=============");
            LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = mgr.getAllProviders();
            if (providers != null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
                Location loc = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loc != null) {
                    System.out.println(loc.getLatitude() + "*" + loc.getLongitude());
                }
            }
        }
        else {
            System.out.println("=============PERMISSION KO=============");
        }



        int status = context.getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName());
        System.out.println("=============status: " + status + "=============");
        System.out.println("=============required: " + PackageManager.PERMISSION_GRANTED + "=============");
        if (status == PackageManager.PERMISSION_GRANTED) {
            LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = mgr.getAllProviders();
            if (providers != null && providers.contains(LocationManager.NETWORK_PROVIDER)) {
                Location loc = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loc != null) {
                    System.out.println(loc.getLatitude() + "*" + loc.getLongitude());
                }
            }
        }
        System.out.println("=============FIN LOCATION=============");


    }

    public void selectUserType(View view) {
        Intent intent = new Intent(this, EnterDestination.class);
//        Intent intent = new Intent(this, ConfigureTravel.class);
        String userType = view.getTag().toString();
        intent.putExtra(USER_TYPE, userType);
        startActivity(intent);
    }
}
