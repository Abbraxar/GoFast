package com.i3cnam.gofast.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.tools.activityRestarter.ActivityRestarterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends AppCompatActivity {
    public final static String USER_TYPE = "com.i3cnam.gofast.USER_TYPE";
    public final static int PERMISSIONS_REQUEST_LOCATION = 1;
    public final static int PERMISSIONS_TELEPHONY = 2;

    // flag which is a workaround for Android bug https://code.google.com/p/android/issues/detail?id=23761
    // more info on http://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
    private boolean stateWorkaroundFlagShowRationaleFragment = false;

    private final static String TAG_LOG = "Main view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FOR TEST
        /*
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_activity", CarpoolingList.class.getName());
        editor.commit();
*/

        // check permissions
        checkAndRequestPermissions();

        // recover user account
        if (User.getMe(this) == null) {
            Log.d("user","not found");
            startActivity(new Intent(this, ConfigureAccount.class));
        }
        else {
            Log.d("nickname", User.getMe(this).getNickname());
            Log.d("phone", User.getMe(this).getPhoneNumber());

            ActivityRestarterImpl.getInstance().startActivityToRestart();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG_LOG, "DESTROY");

        super.onDestroy();
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
        int telephonyPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        List<String> listPermissionsLocation = new ArrayList<>();
        List<String> listPermissionsTel = new ArrayList<>();
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsLocation.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsLocation.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (telephonyPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsTel.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!listPermissionsLocation.isEmpty()) {
            ActivityCompat.requestPermissions(this,listPermissionsLocation.toArray(new String[listPermissionsLocation.size()]), PERMISSIONS_REQUEST_LOCATION);
            return false;
        }
        if (!listPermissionsTel.isEmpty()) {
            ActivityCompat.requestPermissions(this,listPermissionsTel.toArray(new String[listPermissionsTel.size()]), PERMISSIONS_TELEPHONY);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        Map<String, Integer> perms = new HashMap<>();
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION:
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
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
            case PERMISSIONS_TELEPHONY:
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        perms.put(permissions[i], grantResults[i]);
                    }
                    // Check for permissions
                    if (perms.get(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // Permission denied
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
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