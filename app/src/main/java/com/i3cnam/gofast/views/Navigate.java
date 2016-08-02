package com.i3cnam.gofast.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.management.carpooling.CarpoolingManagementService;
import com.i3cnam.gofast.management.course.CourseManagementService;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;


public class Navigate extends AppCompatActivity {

    public final static String COURSE = "com.i3cnam.gofast.COURSE";
    private DriverCourse driverCourse;
    CourseManagementService myService;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        driverCourse = new DriverCourse();
        driverCourse.setOrigin((Place)bundle.getSerializable(DestinationMap.ORIGIN));
        driverCourse.setDestination((Place)bundle.getSerializable(EnterDestination.DESTINATION));
        driverCourse.setDriver(User.getMe());
        driverCourse.setEncodedPoints(intent.getStringExtra(DestinationMap.ENCODED_POINTS));


        // new intent for publication:
        Intent serviceIntent = new Intent(this, CourseManagementService.class);
        // new bundle
        Bundle serviceBundle = new Bundle();
        serviceBundle.putSerializable(COURSE, driverCourse);
        serviceIntent.putExtras(serviceBundle);
        // start service with th intent and bind it
        startService(serviceIntent);
        System.out.println("Bind Service");
        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);

    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CourseManagementService.LocalBinder binder = (CourseManagementService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };





}
