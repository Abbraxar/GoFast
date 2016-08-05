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
import android.view.View;
import android.widget.Toast;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.management.carpooling.CarpoolListEncapsulated;
import com.i3cnam.gofast.management.course.CourseManagementService;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;

import java.util.List;


public class Navigate extends AppCompatActivity {

    public final static String COURSE = "com.i3cnam.gofast.COURSE";
    private DriverCourse driverCourse;
    CourseManagementService myService;
    boolean isBound = false;
    private List<Carpooling> requestedCarpoolings;

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
        Intent serviceIntent = new Intent(Navigate.this, CourseManagementService.class);
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
        // for the course changes
        IntentFilter courseFilter = new IntentFilter();
        courseFilter.addAction(CourseManagementService.BROADCAST_UPDATE_COURSE_ACTION);
        registerReceiver(broadcastCourseReceiver, courseFilter);

        // for the carpooling request changes
        IntentFilter carpoolingFilter = new IntentFilter();
        carpoolingFilter.addAction(CourseManagementService.BROADCAST_UPDATE_CARPOOLING_ACTION);
        registerReceiver(broadcastCarpoolingReceiver, carpoolingFilter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastCarpoolingReceiver);
        unregisterReceiver(broadcastCourseReceiver);
        super.onPause();
    }


    /** Boutons de test */
    public void acceptCarpool(View view) {
        Log.d(TAG_LOG, "acceptCarpool");
        myService.acceptCarpooling(requestedCarpoolings.get(0));
    }

    public void refuseCarpool(View view) {
        Log.d(TAG_LOG, "refuseCarpool");
        myService.refuseCarpooling(requestedCarpoolings.get(0));
    }

    public void abortCarpooling(View view) {
        Log.d(TAG_LOG, "abortCarpooling");
        myService.abortCarpooling(requestedCarpoolings.get(0));
    }



    private BroadcastReceiver broadcastCourseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
            Log.d("BroadcastReceiver", "Broadcast received");
            Toast.makeText(getApplicationContext(), "Course received", Toast.LENGTH_SHORT).show();

            Bundle bundle = intent.getExtras();
            driverCourse = (DriverCourse)(bundle.getSerializable("COURSE"));

            Toast.makeText(getApplicationContext(), "New position is : \n" + driverCourse.getActualPosition(), Toast.LENGTH_LONG).show();
        }
    };

    private BroadcastReceiver broadcastCarpoolingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO
            Log.d("BroadcastReceiver", "Broadcast received");
            Toast.makeText(getApplicationContext(), "Carpooling received", Toast.LENGTH_SHORT).show();

            Bundle bundle = intent.getExtras();
            CarpoolListEncapsulated listEncapsulated= (CarpoolListEncapsulated) (bundle.getSerializable("CARPOOL"));
            requestedCarpoolings = listEncapsulated.list;
            // TODO
            // DO SOMETHING
            String s;
            for (Carpooling c : requestedCarpoolings) {
                s = "Carpooling " + c.getId() + "\n" +
                        "pick up: " + c.getPickupPoint() + "\n" +
                        "drop off: " + c.getDropoffPoint() + "\n" +
                        "time: " + c.getPickupTime() + "\n" +
                        "state: " + c.getState() + "\n" +
                        "fare: " + c.getFare() + "\n";

                Log.d("BroadcastReceiver", s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        }
    };


}