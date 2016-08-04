package com.i3cnam.gofast.management.carpooling;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.i3cnam.gofast.communication.CommInterface;
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
    private Thread observeTravel;
    private PassengerTravel passengerTravel;

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
        Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();

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

        serverCom = new CommunicationStub();
        Log.d(TAG_LOG, "Send request");
        carpoolingPossibilities = serverCom.findCarpoolingPossibilities(passengerTravel);
        Log.d(TAG_LOG, "Request sent");

        observeTravel = new Thread(new ObserveTravel());
//        observeTravel.run();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void updateStatus() {
        Log.d(TAG_LOG, "Status to be updated");

    }


    public void requestCarpool() {
        Log.d(TAG_LOG, "Carpool requested");
        Log.d(TAG_LOG, passengerTravel.getParametersString());
    }

    public void cancelRequest() {
        Log.d(TAG_LOG, "Request canceled");
        Log.d(TAG_LOG, passengerTravel.getParametersString());
    }

    public void abortCarpooling() {
        Log.d(TAG_LOG, "Carpooling aborted");
        observeTravel.stop();
    }

    private class ObserveTravel implements Runnable {
        List<Carpooling> lastList;
        @Override
        public void run() {
            while (true) {
                /*
                serverCom.observeTravel(passengerTravel);
                lastList = serverCom.getTravelState(passengerTravel);
                if (searchStateChanges()) {
                    updateStatus();
                }
*/
                Log.d(TAG_LOG, "My LOG");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean searchStateChanges() {
            // compare lists sizes
            if (lastList.size() != carpoolingPossibilities.size()) {
                Log.d(TAG_LOG, "new carpooling");
                return true;
            }
            for (Carpooling newCarpool : lastList) {
                if (!carpoolingPossibilities.contains(newCarpool)) {
                    Log.d(TAG_LOG, "change detected");
                    return true;
                }

            }
            return false;

        }

    }

}
