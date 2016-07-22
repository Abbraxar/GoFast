package com.i3cnam.gofast.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.Communication;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.Place;
import com.i3cnam.gofast.model.User;


public class Navigate extends AppCompatActivity {

    private DriverCourse driverCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        driverCourse = new DriverCourse();
        driverCourse.setOrigin((Place)bundle.getSerializable(DestinationMap.ORIGIN));
        driverCourse.setDestination((Place)bundle.getSerializable(EnterDestination.DESTINATION));
        driverCourse.setDriver(User.getMe());
        driverCourse.setEncodedPoints(intent.getStringExtra(DestinationMap.ENCODED_POINTS));

        CommInterface serverCom = new Communication();
        System.out.println("Send request");
        int travelID = serverCom.declareCourse(driverCourse);
        System.out.println("the course was declared with ID: " + travelID);
        System.out.println("Request sent");

    }
}
