package com.i3cnam.gofast.management.carpooling;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
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
        System.out.println("Service CREATE");
        Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onDestroy()
    {
        System.out.println("Service DESTROY");

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Service START");

        Bundle bundle = intent.getExtras();

        passengerTravel = (PassengerTravel)(bundle.getSerializable(CarpoolingList.TRAVEL));
        System.out.println(passengerTravel.getParametersString());


        serverCom = new CommunicationStub();
        System.out.println("Send request");
        carpoolingPossibilities = serverCom.findCarpoolingPossibilities(passengerTravel);
        System.out.println("Request sent");

        observeTravel = new Thread(new ObserveTravel());
        observeTravel.run();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void updateStatus() {
        System.out.println("Status to be updated");

    }


    public void requestCarpool() {
        System.out.println("Carpool requested");
        System.out.println(passengerTravel.getParametersString());
    }

    public void cancelRequest() {
        System.out.println("Request canceled");
        System.out.println(passengerTravel.getParametersString());
    }

    public void abortCarpooling() {
        System.out.println("Carpooling aborted");
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
                System.out.println("My LOG");
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
                System.out.println("new carpooling");
                return true;
            }
            for (Carpooling newCarpool : lastList) {
                if (!carpoolingPossibilities.contains(newCarpool)) {
                    System.out.println("change detected");
                    return true;
                }

            }
            return false;

        }

    }

}
