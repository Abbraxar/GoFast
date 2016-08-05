package com.i3cnam.gofast.management.carpooling;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.views.CarpoolingList;

import java.util.List;

/**
 * Created by Nestor on 18/07/2016.
 */
public class CarpoolingManagementService extends Service {

    private List<Carpooling> carpoolingPossibilities;
    private final IBinder myBinder = new LocalBinder();
    private CommInterface serverCom;
    private PassengerTravel passengerTravel;
    private Thread observeTravel;

    // temporary global variables to communicate between threads:
    private Carpooling carpoolingToRequest;
    private Carpooling carpoolingToCancel;
    private Carpooling carpoolingToAbort;

    // test pour le broadcast
    public static final String BROADCAST_ACTION = "com.i3cnam.gofast.UPDATE_CARPOOLING";
    private Intent broadcastIntent;


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
    public IBinder onBind(Intent arg0)
    {
        System.out.println("Service Carpool BOUND");
        return myBinder;
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG_LOG, "Service CREATE");
        broadcastIntent = new Intent(BROADCAST_ACTION);
    }


    @Override
    public void onDestroy()
    {
        Log.d(TAG_LOG, "Service DESTROY");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOG, "Service START");

        Bundle bundle = intent.getExtras();

        passengerTravel = (PassengerTravel)(bundle.getSerializable(CarpoolingList.TRAVEL));
        Log.d(TAG_LOG, passengerTravel.getParametersString());

        // create communication module
        serverCom = new CommunicationStub();

        // launch observer thread
        observeTravel = new Thread(new ObserveTravel());
        observeTravel.start();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Broadcast the carpooling update
     */
    private void updateStatus() {
        Log.d(TAG_LOG, "Status to be updated");
        Log.d("BroadcastService", "entered sendCarpoolingUpdate");
        broadcastIntent.putExtra("UPDATED_CARPOOLING", new CarpoolListEncapsulated(carpoolingPossibilities));
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

    public void abortCarpooling(Carpooling carpooling) {
        carpoolingToAbort = carpooling;
        new AsynchronousAbortCarpool().execute();
        observeTravel.stop();
    }

    /*
    ------------------------------------------------------------------------------------------------
    */

    private class ObserveTravel implements Runnable {
        List<Carpooling> lastList;
        @Override
        public void run() {
            // declare travel
            int id = serverCom.declareTravel(passengerTravel);
            passengerTravel.setId(id);

            // do first query
            Log.d(TAG_LOG, "Send request");
            carpoolingPossibilities = serverCom.findCarpoolingPossibilities(passengerTravel);

            // broadcast initial data
            updateStatus();

            // then do one query every second
            while (true) {
                // wait one second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // do the query
                serverCom.observeTravel(passengerTravel);
                lastList = serverCom.getTravelState(passengerTravel);
                // compare results
                if (searchStateChanges()) {
                    carpoolingPossibilities = lastList;
                    updateStatus();
                }
            }
        }

        private boolean searchStateChanges() {
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
    private class AsynchronousAbortCarpool extends AsyncTask<String, String,String> {
        protected String doInBackground(String... urls) {
            Log.d(TAG_LOG, "Carpooling aborted");
            Log.d(TAG_LOG, passengerTravel.getParametersString());
            serverCom.abortCarpool(carpoolingToAbort);
            return null;
        }
    }

}
