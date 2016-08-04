package com.i3cnam.gofast.views;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.management.course.CourseManagementService;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;


public class Navigate extends AppCompatActivity {

    public final static String COURSE = "com.i3cnam.gofast.COURSE";
    private DriverCourse driverCourse;
    CourseManagementService myService;
    boolean isBound = false;

    private final static String TAG_LOG = "Navigate view";

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

        // launch and bind CourseManagementService
        launchAndBindCourseManagementService();

    }

    private void launchAndBindCourseManagementService()  {

        // new intent for publication:
        Intent serviceIntent = new Intent(this, CourseManagementService.class);
        // new bundle
        Bundle serviceBundle = new Bundle();
        serviceBundle.putSerializable(COURSE, driverCourse);
        serviceIntent.putExtras(serviceBundle);
        // start service with th intent and bind it
        startService(serviceIntent);
        Log.d(TAG_LOG, "Bind Service");
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


    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CourseManagementService.BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
            Log.d("BroadcastReceiver", "Broadcast received");
            Toast.makeText(getApplicationContext(), "Broadcast received", Toast.LENGTH_SHORT).show();

            Bundle bundle = intent.getExtras();
            driverCourse = (DriverCourse)(bundle.getSerializable("UPDATED_COURSE"));

            Toast.makeText(getApplicationContext(), "New position is : \n" + driverCourse.getActualPosition(), Toast.LENGTH_LONG).show();
        }
    };
}