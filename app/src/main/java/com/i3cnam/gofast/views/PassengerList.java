package com.i3cnam.gofast.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;
import com.i3cnam.gofast.views.abstractViews.CourseServiceConnectedActivity;

public class PassengerList extends CourseServiceConnectedActivity {

    private final String TAG_LOG = "PassengerList";
    private ListView myListView;
    private CarpoolingDriverArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpooling_list);

        // get all the data of the intent and create a new travel object
        PassengerTravel passengerTravel = new PassengerTravel();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            passengerTravel.setOrigin((Place) bundle.getSerializable(DestinationMap.ORIGIN));
            passengerTravel.setDestination((Place) bundle.getSerializable(EnterDestination.DESTINATION));
            passengerTravel.setPassenger(User.getMe(this));
            passengerTravel.setRadius(intent.getIntExtra(EnterDestination.RADIUS, 500));
        }

        myListView = (ListView) findViewById(R.id.carpoolsListView);

        launchAndBindService(passengerTravel);
    }

    @Override
    protected void afterServiceConnected() {
        adapter =  new CarpoolingDriverArrayAdapter(this, R.layout.list_item_carpooling_driver, myService.getRequestedCarpoolings());
        myListView.setAdapter(adapter);
    }

    /** Action : pressed Accept button */
    public void acceptCarpool(int position) {
        Log.d(TAG_LOG, "acceptCarpool");
        // do the action to accept the carpooling
        myService.acceptCarpooling(myService.getRequestedCarpoolings().get(position));
    }

    /** Action : pressed Refuse button */
    public void refuseCarpool(int position) {
        Log.d(TAG_LOG, "refuseCarpool");
        // do the action to refuse the carpooling
        myService.refuseCarpooling(myService.getRequestedCarpoolings().get(position));
    }

    /** Action : pressed Abort button */
    public void abortCarpool(int position) {
        Log.d(TAG_LOG, "abortCarpool");
        // do the action to refuse the carpooling
        myService.abortCarpooling(myService.getRequestedCarpoolings().get(position));
    }

    /** Action : pressed Validate button */
    public void validateCarpool(int position) {
        Log.d(TAG_LOG, "validateCarpool");
        // do the action to refuse the carpooling
        myService.validateCarpooling(myService.getRequestedCarpoolings().get(position));
    }
}
