package com.i3cnam.gofast.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.i3cnam.gofast.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends AppCompatActivity {
    public final static String USER_TYPE = "com.i3cnam.gofast.USER_TYPE";
    public final static int PERMISSIONS_REQUEST_LOCATION = 1;

    // flag which is a workaround for Android bug https://code.google.com/p/android/issues/detail?id=23761
    // more info on http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
    private boolean stateWorkaroundFlagShowRationaleFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check permissions
        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(stateWorkaroundFlagShowRationaleFragment) {
            // Show a permission explanation to the user
            DialogFragment newFragment = new PermissionsRationale();
            newFragment.show(getSupportFragmentManager(), "rationale");
            stateWorkaroundFlagShowRationaleFragment = false;
        }
    }


    /**
     * Check and request application permissions
     * @return true if application has permissions
     */
    public  boolean checkAndRequestPermissions() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        perms.put(permissions[i], grantResults[i]);
                    }
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Permission denied
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // Show a permission explanation to the user
                            stateWorkaroundFlagShowRationaleFragment = true;
                        }
                        // Permission is denied and never ask again is checked
                        else {
                            // TODO: handle this case (say user can enable permissions from his settings and quit app)
                            finish();
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * Call activity when user select his type
     * @param view View
     */
    public void selectUserType(View view) {
        Intent intent = new Intent(this, EnterDestination.class);
        String userType = view.getTag().toString();
        intent.putExtra(USER_TYPE, userType);
        startActivity(intent);
    }
}