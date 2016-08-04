package com.i3cnam.gofast.management.course;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.geo.GPSTracker;
import systr.cartographie.Operations;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.views.Navigate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;

public class CourseManagementService extends Service {

    private final IBinder myBinder = new LocalBinder();
    private CommInterface serverCom;
    private DriverCourse driverCourse;
    private List<Carpooling> requestedCarpoolings = new ArrayList<>();

    // test pour le broadcast
    public static final String BROADCAST_ACTION = "com.i3cnam.gofast.UPDATE_POSITION";
    private Intent broadcastIntent;
//    private final Handler handler = new Handler() ;


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public CourseManagementService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CourseManagementService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastIntent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("CourseManagementService", "Service Course BOUND");
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CourseManagementService", "Service START");

        // get the driver course from the intent bundle
        Bundle bundle = intent.getExtras();
        driverCourse = (DriverCourse)(bundle.getSerializable(Navigate.COURSE));
        Log.d("CourseManagementService", driverCourse.getParametersString());

        // init the comunication module for the service
        serverCom = new CommunicationStub();

        // declare the course on the server
        int courseID = serverCom.declareCourse(driverCourse);
        driverCourse.setId(courseID);
        Log.d("CourseManagementService","the course was declared with ID: " + courseID);

        // start the navigation listener
        new GPSForNavigation(this);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * verify if actual position is in the path
     * @return true if the user deviated from the path
     */
    private boolean courseChanged() {
        Log.d("CourseManagementService","COURSE CHANGED?");
        boolean returnValue = true;
        // TODO
        List<LatLng> actualPath = driverCourse.getPath();
        int i = 0 , j;
        double delta;
        double minDist = 100000;

        LatLng nearestPoint;
        // we browse each pair of consecutive points
        // (the loop is between the first and the second to last
        while (returnValue && i < (actualPath.size() - 1)) {

            // calculate nearest point to the segment
            nearestPoint = Operations.nearestPoint(actualPath.get(i), actualPath.get(i + 1),  driverCourse.getActualPosition());

            // calculate the distance to the nearest point
            delta = Operations.dist2PointsEnM(driverCourse.getActualPosition(), nearestPoint);
            if ( delta < 50 ) {
                // less than 50 m gap, we consider that the user is not deviated
                returnValue = false;
                Log.d("CourseManagementService", delta + " m");
                // update path
                // remove previous points
                for (j = 0 ; j <= i ; j++) {
                    actualPath.remove(0);
                }
                // replace current point
                actualPath.set(0 , driverCourse.getActualPosition());
                driverCourse.setEncodedPoints(PolyUtil.encode(actualPath));
            }
            if (minDist > delta) {
                Log.d("CourseManagementService", "min; " + delta + " m");
                minDist = delta;
            }
            i++;
        }
        return returnValue;
    }

    /**
     * update the path for the new position
     */
    private void recalculatePath() {
        Log.d("CourseManagementService","RECALCULANDO");
        // TODO
        // compute new path
        DirectionsService directions = new DirectionsService();
        directions.setOrigin(driverCourse.getActualPosition());
        directions.setDestination(driverCourse.getDestination());
        for (Carpooling c : requestedCarpoolings) {
            if (c.getState() == Carpooling.CarpoolingState.IN_PROGRESS) {
                directions.addWaypoint(c.getPickupPoint());
                directions.addWaypoint(c.getDropoffPoint());
            }
        }
        directions.computeDirections();
        // save path
        driverCourse.setEncodedPoints(directions.getEncodedPolyline());
    }

    /**
     * Management of the user change location
     */
    private class GPSForNavigation extends GPSTracker{

        public GPSForNavigation(Context context) {
            super(context);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d("GPSForNavigation","LOCATION CHANGED");
            // store the new position and the time
            driverCourse.setActualPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            driverCourse.setPositioningTime(new Date());
            // verify if user has deviated and consequently recalculate the path
            if (courseChanged()) {
                recalculatePath();
                Log.d("GPSForNavigation","COURSE POSITION UPDATE");
                serverCom.updateCourse(driverCourse);
            }
            else {
                Log.d("GPSForNavigation","SENDING POSITION UPDATE");
                serverCom.updatePosition(driverCourse);
            }

            sendCourseUpdate();

        }
    }

    private void sendCourseUpdate() {
        Log.d("BroadcastService", "entered sendCourseUpdate");

        broadcastIntent.putExtra("UPDATED_COURSE", driverCourse);
        sendBroadcast(broadcastIntent);
    }



}
