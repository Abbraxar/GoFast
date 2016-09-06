package com.i3cnam.gofast.management.course;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.geo.DirectionsService;
import com.i3cnam.gofast.geo.GPSTracker;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.views.abstractViews.CourseServiceConnectedActivity;
import com.i3cnam.gofast.views.notifications.GeneralForegroundNotification;
import com.i3cnam.gofast.views.notifications.NewRequestNotification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import systr.cartographie.Operations;

public class CourseManagementService extends Service {

    private final IBinder myBinder = new LocalBinder();
    protected CourseManagementService thisService; // to access from other classes

    private CommInterface serverCom;
    private DriverCourse driverCourse;
    private List<Carpooling> requestedCarpoolings = new ArrayList<>();

    // for state storage
    List<Integer> requestedCarpoolsIndexes = new ArrayList<>();
    List<Integer> acceptedCarpoolsIndexes = new ArrayList<>();
    List<Integer> conflictCarpoolsIndexes = new ArrayList<>();
    List<Integer> achievedCarpoolsIndexes = new ArrayList<>();

    // for broadcast
    public static final String BROADCAST_INIT_COURSE_ACTION = "com.i3cnam.gofast.INIT_COURSE";
    public static final String BROADCAST_UPDATE_COURSE_ACTION = "com.i3cnam.gofast.UPDATE_COURSE";
    public static final String BROADCAST_UPDATE_CARPOOLING_ACTION = "com.i3cnam.gofast.UPDATE_CARPOOLING";
    private Intent broadcastCourseIntent;
    private Intent broadcastInitIntent;
    private Intent broadcastCarpoolingIntent;

    // temporary global variables to communicate between threads:
    private Carpooling carpoolingToAccept;
    private Carpooling carpoolingToRefuse;
    private Carpooling carpoolingToAbort;

    private ObserveCourse myCourseObserver;
    private GPSForNavigation navGPS;


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
        Log.d(TAG_LOG, "CREATED");

        super.onCreate();
        broadcastInitIntent = new Intent(BROADCAST_INIT_COURSE_ACTION);
        broadcastCourseIntent = new Intent(BROADCAST_UPDATE_COURSE_ACTION);
        broadcastCarpoolingIntent = new Intent(BROADCAST_UPDATE_CARPOOLING_ACTION);
        thisService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOG, "START");

        if (driverCourse == null) {
            // get the driver course from the intent bundle
            Bundle bundle = intent.getExtras();
            driverCourse = (DriverCourse)(bundle.getSerializable(CourseServiceConnectedActivity.PRIMARY_DATA));

            // init the communication module for the service
            serverCom = new Communication();

            // launch the thread for the management of the course
            myCourseObserver = (new ObserveCourse());
            new Thread(myCourseObserver).start();

        }
        else {
            // broadcast course init to the activity
            sendCourseInit();
        }

        Log.d(TAG_LOG, "Build notification");
        GeneralForegroundNotification.notify(this, R.drawable.ic_general_notification_vehicule);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG_LOG, "BOUND");

        return myBinder;
    }

    /**
     * verify if actual position is in the path
     * @return true if the user deviated from the path
     */
    private boolean courseChanged() {
        Log.d(TAG_LOG, "COURSE CHANGED?");
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
                Log.d(TAG_LOG, delta + " m");
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
                Log.d(TAG_LOG, "min; " + delta + " m");
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
        Log.d(TAG_LOG, "RECALCULANDO");
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
        Log.d(TAG_LOG, "entered sendCourseUpdate");

        sendBroadcast(broadcastCourseIntent);
    }

    /**
     * Broadcast the course initialisation
     */
    private void sendCourseInit() {
        Log.d(TAG_LOG, "entered sendCourseInit");

        // start the navigation listener
        navGPS = new GPSForNavigation(this);

        sendBroadcast(broadcastInitIntent);
    }

    /**
     * Broadcast the carpooling update
     */
    private void sendCarpoolUpdate() {
        Log.d(TAG_LOG, "entered sendCarpoolUpdate");

        for (Carpooling c : requestedCarpoolings) {
            Log.d(TAG_LOG, "carpool id : " + c.getId());
            if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
                // if it is a new demand
                if (!requestedCarpoolsIndexes.contains(c.getId())) {
                    Log.d(TAG_LOG, "send notification");
                    new NewRequestNotification().notify(this, c.getPickupPoint().toString(), c.getId(), 1);
                    // and add it to the indexes
                    requestedCarpoolsIndexes.add(c.getId());
                }
            }
            if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
                // if it is a new acceptation
                if (!acceptedCarpoolsIndexes.contains(c.getId())) {
                    Log.d(TAG_LOG, "acquit notification");
                    new NewRequestNotification().cancel(this, c.getId());
                    // and add it to the indexes
                    acceptedCarpoolsIndexes.add(c.getId());
                    if (!requestedCarpoolsIndexes.contains(c.getId())) {
                        if (!requestedCarpoolsIndexes.contains(c.getId())) {
                            requestedCarpoolsIndexes.remove((Integer)c.getId());
                        }
                    }
                }
            }
            if (c.getState().equals(Carpooling.CarpoolingState.CONFLICT)) {
                // if it is a new abort
                if (!conflictCarpoolsIndexes.contains(c.getId())) {
                    Log.d(TAG_LOG, "show notification");
                    new NewRequestNotification().notify(this, c.getPickupPoint().toString(), c.getId(), 1);
                    // and add it to the indexes
                    conflictCarpoolsIndexes.add(c.getId());
                    if (!acceptedCarpoolsIndexes.contains(c.getId())) {
                        acceptedCarpoolsIndexes.remove((Integer)c.getId());
                    }
                }
            }
        }
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

    public void abortCourse() {
        new AsynchronousAbortCourse().execute();
    }

    public DriverCourse getDriverCourse() {
        return driverCourse;
    }

    public List<Carpooling> getRequestedCarpoolings() {
        return requestedCarpoolings;
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

        private static final String TAG_LOG = "ProcessLocationChanged";

        public ProcessLocationChanged(Location newLocation) {
            this.newLocation = newLocation;
        }
        @Override
        public void run() {
            Log.d(TAG_LOG,"LOCATION CHANGED");
            // store the new position and the time
            driverCourse.setActualPosition(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
            driverCourse.setPositioningTime(new Date());
            // verify if user has deviated and consequently recalculate the path
            if (courseChanged()) {
                recalculatePath();
                Log.d(TAG_LOG,"COURSE POSITION UPDATE");
                serverCom.updateCourse(driverCourse);
            }
            else {
                Log.d(TAG_LOG,"SENDING POSITION UPDATE");
                serverCom.updatePosition(driverCourse);
            }
            sendCourseUpdate();
        }
    }


    /**
     * Process course observe in a new thread
     */
    private class ObserveCourse implements Runnable {
        List<Carpooling> lastList;
        private volatile boolean running = true;
        private static final String TAG_LOG = "ObserveCourse";

        public void terminate(){
            running = false;
        }
        @Override
        public void run() {

            // first declare the course on the server or
            // recover course from the server if course is not provided by intent nor service
            if (driverCourse.getDestination() == null) {
                // recover course from the server
                driverCourse = serverCom.getDriverCourse(User.getMe(thisService));
            } else {
                // declare the course on the server
                int courseID = serverCom.declareCourse(driverCourse);
                // set the returned id to the object
                driverCourse.setId(courseID);
                Log.d(TAG_LOG, "the course was declared with ID: " + courseID);
            }

            // broadcast course to the activity
            sendCourseInit();

            // if the id is 0, the course has not been registered into database, abort
            if (driverCourse.getId() == 0) {
                // with an empty object, the view will restart
                driverCourse = new DriverCourse();
            }
            else {
                // then do one query every second
                while (running) {
                    // wait one second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // do the query
                    serverCom.observeCarpoolCourse(driverCourse);
                    lastList = serverCom.getCarpoolCourseState(driverCourse);
                    // compare results
                    if (searchStateChanges()) {
                        requestedCarpoolings = lastList;
                        sendCarpoolUpdate();
                    }
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
    private class AsynchronousAbortCourse extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Course aborted");
            myCourseObserver.terminate();
            serverCom.abortCourse(driverCourse);
            navGPS.stopUsingGPS();
            return null;
        }
    }
}
