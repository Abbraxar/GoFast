package com.i3cnam.gofast.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.PlaceClass;
import com.i3cnam.gofast.model.User;

import java.util.List;

public class CarpoolingOptions extends AppCompatActivity {

    public final static String TRAVEL = "com.i3cnam.gofast.TRAVEL";
    private PassengerTravel passengerTravel;
    private ListView carpoolingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_options);

        passengerTravel = new PassengerTravel();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        passengerTravel.setOrigin((PlaceClass)bundle.getSerializable(PassengerMap.ORIGIN));
        passengerTravel.setDestination((PlaceClass)bundle.getSerializable(EnterDestination.DESTINATION));
        passengerTravel.setUser(User.getMe());
        passengerTravel.setRadius(intent.getIntExtra(EnterDestination.RADIUS,500));

        carpoolingsList = (ListView) findViewById(R.id.carpoolingsList);

//        intent = new Intent(this, CarpoolingManagementService.class);
        /*
        bundle = new Bundle();
        bundle.putSerializable(TRAVEL, passengerTravel);
        intent.putExtras(bundle);
*/
//        startService(intent);
        System.out.println("Send request");
        List<Carpooling> possibilities = Communication.findCarpoolingPossibilities(passengerTravel);
        System.out.println("Request sent");


        for (Carpooling onePossibility: possibilities) {
            System.out.println("====================== C A R P O O L I N G ======================");
            System.out.println(onePossibility.getPickup_point());
        }

        System.out.println("Finish");


    }
}
