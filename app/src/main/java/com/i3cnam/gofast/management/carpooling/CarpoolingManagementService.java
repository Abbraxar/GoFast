package com.i3cnam.gofast.management.carpooling;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.views.abstractViews.TravelServiceConnectedActivity;
import com.i3cnam.gofast.views.notifications.GeneralForegroundNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 18/07/2016.
 */
public class CarpoolingManagementService extends Service {

    private List<Carpooling> carpoolingPossibilities = new ArrayList<>();
    private final IBinder myBinder = new LocalBinder();
    private CommInterface serverCom;
    private PassengerTravel passengerTravel;
    private Thread observeTravel;
    private ObserveTravel myTravelObserver;

    // temporary global variables to communicate between threads:
    private Carpooling carpoolingToRequest;
    private Carpooling carpoolingToCancel;
    private Carpooling carpoolingToAbort;

    // test pour le broadcast
    public static final String BROADCAST_ACTION = "com.i3cnam.gofast.UPDATE_CARPOOLING";
    private Intent broadcastIntent;
    public static  final String BROADCAST_TRAVEL_INIT_ACTION = "com.i3cnam.gofast.INIT_TRAVEL";
    private Intent broadcastTravelInitIntent;

    private CarpoolingManagementService thisService; // to access from other classes


    private final String TAG_LOG = "Carpooling Service"; // tag for log messages

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public CarpoolingManagementService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CarpoolingManagementService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Service Carpool BOUND");
        return myBinder;
    }


    @Override
    public void onCreate() {
        Log.d(TAG_LOG, "CREATE");
        broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastTravelInitIntent = new Intent(BROADCAST_TRAVEL_INIT_ACTION);
        thisService = this;
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG_LOG, "DESTROY");
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOG, "START");

        if (passengerTravel == null) {
            // get the driver course from the intent bundle
            Bundle bundle = intent.getExtras();
            passengerTravel = (PassengerTravel)(bundle.getSerializable(TravelServiceConnectedActivity.PRIMARY_DATA));

            // init the communication module for the service
            serverCom = new Communication();

            // launch observer thread
            myTravelObserver = new ObserveTravel();
            observeTravel = new Thread(myTravelObserver);
            observeTravel.start();
        }
        else {
            // broadcast course init to the activity
            sendTravelInit();
        }

        Log.d(TAG_LOG, "Build notification");
        GeneralForegroundNotification.notify(this, R.drawable.ic_general_notification_pedestrian);

        return START_NOT_STICKY;
    }

    /*
    ------------------------------------------------------------------------------------------------
        BROADCAST METHODS:
        They represent state changes of the TRAVEL AND CARPOOLING
        They are received by the activity to show the changes
        If the activity is not visible, the generate a notification
    ------------------------------------------------------------------------------------------------
     */

    /**
     * Broadcast the course update
     */
    private void sendTravelInit() {
        Log.d(TAG_LOG, "entered sendTravelInit");

        sendBroadcast(broadcastTravelInitIntent);
    }

    /**
     * Broadcast the carpooling update
     */
    private void updateStatus() {
        Log.d(TAG_LOG, "Status to be updated");
        Log.d("BroadcastService", "entered sendCarpoolingUpdate");
        sendBroadcast(broadcastIntent);

    }

    /*
    ------------------------------------------------------------------------------------------------
        PUBLIC METHODS
        (Called by the activity)
    ------------------------------------------------------------------------------------------------
     */

    public void requestCarpool(Carpooling carpooling) {
        carpoolingToRequest = carpooling;
        new AsynchronousRequestCarpool().execute();
    }

    public void cancelRequest(Carpooling carpooling) {
        carpoolingToCancel = carpooling;
        new AsynchronousCancelRequest().execute();
    }

    public void abortTravel() {
        new AsynchronousAbortTravel().execute();
    }

    public List<Carpooling> getCarpoolingPossibilities() {
        return carpoolingPossibilities;
    }

    /*
    ------------------------------------------------------------------------------------------------
    */

    private class ObserveTravel implements Runnable {
        List<Carpooling> lastList;
        private volatile boolean running = true;
        private static final String TAG_LOG = "ObserveTravel";

        public void terminate(){
            running = false;
        }
        @Override
        public void run() {

            // first declare the course on the server or
            // recover course from the server if course is not provided by intent nor service
            if (passengerTravel.getDestination() == null) {
                // recover course from the server
                passengerTravel = serverCom.getPassengerTravel(User.getMe(thisService));
            } else {
                // declare the course on the server
                int travelID = serverCom.declareTravel(passengerTravel);
                // set the returned id to the object
                passengerTravel.setId(travelID);
                Log.d(TAG_LOG, "the travel was declared with ID: " + travelID);
            }

            // broadcast course to the activity
            sendTravelInit();

            // if the id is 0, the course has not been registered into database, abort
            if (passengerTravel.getId() == 0) {
                // with an empty object, the view will restart
                passengerTravel = new PassengerTravel();
            }
            else {
                serverCom.findCarpoolingPossibilities(passengerTravel);
                // then do one query every second
                while (running) {
                    // wait one second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // do the query
                    serverCom.observeCarpoolTravel(passengerTravel);
                    lastList = serverCom.getCarpoolTravelState(passengerTravel);
                    // compare results
                    if (searchStateChanges()) {
                        carpoolingPossibilities = lastList;
                        updateStatus();
                    }
                }
            }
        }

        private boolean searchStateChanges() {
            if(carpoolingPossibilities == null) {
                return lastList != null;
            }
            // compare lists sizes
            if (lastList.size() != carpoolingPossibilities.size()) {
                Log.d(TAG_LOG, "new carpooling");
                return true;
            }
            // search if each carpool is identical than previous version
            for (Carpooling newCarpool : lastList) {
                if (!carpoolingPossibilities.contains(newCarpool)) {
                    Log.d(TAG_LOG, "change detected");
                    return true;
                }
            }
            // search if no carpool was deleted
            for (Carpooling newCarpool : carpoolingPossibilities) {
                if (!lastList.contains(newCarpool)) {
                    Log.d(TAG_LOG, "change detected");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Request a carpool in a new thread
     */
    private class AsynchronousRequestCarpool extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Carpool requested");
            Log.d(TAG_LOG, passengerTravel.getParametersString());
            serverCom.requestCarpool(carpoolingToRequest);
            return null;
        }
    }

    /**
     * Cancel a carpool request in a new thread
     */
    private class AsynchronousCancelRequest extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Request canceled");
            Log.d(TAG_LOG, passengerTravel.getParametersString());
            serverCom.cancelRequest(carpoolingToCancel);
            return null;
        }
    }

    /**
     * Abort a carpool in a new thread
     */
    private class AsynchronousAbortTravel extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Travel aborted");
            Log.d(TAG_LOG, passengerTravel.getParametersString());
            myTravelObserver.terminate();
            serverCom.abortTravel(passengerTravel);
            return null;
        }
    }

}
