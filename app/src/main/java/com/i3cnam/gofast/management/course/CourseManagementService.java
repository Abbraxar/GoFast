package com.i3cnam.gofast.management.course;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.geo.GPSTracker;
import systr.cartographie.Operations;

import com.i3cnam.gofast.management.carpooling.CarpoolListEncapsulated;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.views.Navigate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseManagementService extends Service {

    private final IBinder myBinder = new LocalBinder();
    private CommInterface serverCom;
    private DriverCourse driverCourse;
    private List<Carpooling> requestedCarpoolings = new ArrayList<>();

    // test pour le broadcast
    public static final String BROADCAST_UPDATE_COURSE_ACTION = "com.i3cnam.gofast.UPDATE_COURSE";
    public static final String BROADCAST_UPDATE_CARPOOLING_ACTION = "com.i3cnam.gofast.UPDATE_CARPOOLING";
    private Intent broadcastCourseIntent;
    private Intent broadcastCarpoolingIntent;

    // temporary global variables to communicate between threads:
    private Carpooling carpoolingToAccept;
    private Carpooling carpoolingToRefuse;
    private Carpooling carpoolingToAbort;

    private final String TAG_LOG = "Course Service"; // tag for log messages


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
        broadcastCourseIntent = new Intent(BROADCAST_UPDATE_COURSE_ACTION);
        broadcastCarpoolingIntent = new Intent(BROADCAST_UPDATE_CARPOOLING_ACTION);
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
        serverCom = new Communication();

        // launch the thread for the management of the course
        new Thread(new ObserveCourse()).start();

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
                // save path
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


    /*
    ------------------------------------------------------------------------------------------------
        BROADCAST METHODS:
        They represent state changes of the course
        They are received by the activity to show the changes
        If the activity is not visible, the generate a notification
    ------------------------------------------------------------------------------------------------
     */

    /**
     * Broadcast the course update
     */
    private void sendCourseUpdate() {
        Log.d("BroadcastService", "entered sendCourseUpdate");

        broadcastCourseIntent.putExtra("COURSE", driverCourse);
        sendBroadcast(broadcastCourseIntent);
    }

    /**
     * Broadcast the carpooling update
     */
    private void sendCarpoolUpdate() {
        Log.d("BroadcastService", "entered sendCarpoolUpdate");

        broadcastCarpoolingIntent.putExtra("CARPOOL", new CarpoolListEncapsulated(requestedCarpoolings));
        sendBroadcast(broadcastCarpoolingIntent);
    }

    /*
    ------------------------------------------------------------------------------------------------
        PUBLIC METHODS
        (Called by the activity)
    ------------------------------------------------------------------------------------------------
     */

    public void acceptCarpooling(Carpooling carpooling) {
        carpoolingToAccept = carpooling;
        new AsynchronousAcceptCarpool().execute();
    }

    public void refuseCarpooling(Carpooling carpooling) {
        carpoolingToRefuse = carpooling;
        new AsynchronousRefuseCarpool().execute();
    }

    public void abortCarpooling(Carpooling carpooling) {
        carpoolingToAbort = carpooling;
        new AsynchronousAbortCarpool().execute();
    }

    /*
    ------------------------------------------------------------------------------------------------
    */


    /**
     * Management of the user change location
     */
    private class GPSForNavigation extends GPSTracker{

        public GPSForNavigation(Context context) {
            super(context);
        }

        @Override
        public void onLocationChanged(Location location) {
            new Thread(new ProcessLocationChanged(location)).start();
        }

    }

    /**
     * Process location change in a new thread
     */
    private class ProcessLocationChanged implements Runnable {
        Location newLocation;

        public ProcessLocationChanged(Location newLocation) {
            this.newLocation = newLocation;
        }
        @Override
        public void run() {
            Log.d("GPSForNavigation","LOCATION CHANGED");
            // store the new position and the time
            driverCourse.setActualPosition(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
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


    /**
     * Process location change in a new thread
     */
    private class ObserveCourse implements Runnable {
        List<Carpooling> lastList;

        @Override
        public void run() {
            // first declare the course on the server
            int courseID = serverCom.declareCourse(driverCourse);
            // set the returned id to the object
            driverCourse.setId(courseID);
            Log.d("CourseManagementService", "the course was declared with ID: " + courseID);

            // then do one query every second
            while (true) {
                // wait one second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // do the query
                serverCom.observeCourse(driverCourse);
                lastList = serverCom.getCourseState(driverCourse);
                // compare results
                if (searchStateChanges()) {
                    requestedCarpoolings = lastList;
                    sendCarpoolUpdate();
                }
            }
        }


        private boolean searchStateChanges() {
            // compare lists sizes
            if (lastList.size() != requestedCarpoolings.size()) {
                Log.d(TAG_LOG, "new carpooling");
                return true;
            }
            // search if each carpool is identical than previous version
            for (Carpooling newCarpool : lastList) {
                if (!requestedCarpoolings.contains(newCarpool)) {
                    Log.d(TAG_LOG, "change detected");
                    return true;
                }
            }
            // search if no carpool was deleted
            for (Carpooling newCarpool : requestedCarpoolings) {
                if (!lastList.contains(newCarpool)) {
                    Log.d(TAG_LOG, "change detected");
                    return true;
                }
            }
            return false;
        }


    }


    /**
     * Abort a carpool in a new thread
     */
    private class AsynchronousAcceptCarpool extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Carpooling accepted");
            serverCom.acceptCarpool(carpoolingToAccept);
            return null;
        }
    }

    /**
     * Abort a carpool in a new thread
     */
    private class AsynchronousRefuseCarpool extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Carpooling refused");
            serverCom.refuseCarpool(carpoolingToRefuse);
            return null;
        }
    }

    /**
     * Abort a carpool in a new thread
     */
    private class AsynchronousAbortCarpool extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Carpooling aborted");
            serverCom.abortCarpool(carpoolingToAbort);
            return null;
        }
    }

}
