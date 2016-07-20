package com.i3cnam.gofast.management.carpooling;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.i3cnam.gofast.activities.CarpoolingList;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.PassengerTravel;

/**
 * Created by Nestor on 18/07/2016.
 */
public class CarpoolingManagementService extends Service {

    private PassengerTravel passengerTravel;

    @Override
    public IBinder onBind(Intent arg0)
    {
        Bundle bundle = arg0.getExtras();
        this.passengerTravel = (PassengerTravel) bundle.getSerializable(CarpoolingList.TRAVEL);

        return null;
    }


    @Override
    public void onCreate()
    {
        Communication.findCarpoolingPossibilities(passengerTravel);
    }


    @Override
    public void onDestroy()
    {

    }




}
