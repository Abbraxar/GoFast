package com.i3cnam.gofast.management.course;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.i3cnam.gofast.communication.CommInterface;
import com.i3cnam.gofast.communication.CommunicationStub;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.views.CarpoolingList;
import com.i3cnam.gofast.views.Navigate;

public class CourseManagementService extends Service {

    private final IBinder myBinder = new LocalBinder();
    private CommInterface serverCom;
    private DriverCourse driverCourse;

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
    public IBinder onBind(Intent intent) {
        System.out.println("Service Course BOUND");
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Service START");

        Bundle bundle = intent.getExtras();

        driverCourse = (DriverCourse)(bundle.getSerializable(Navigate.COURSE));
        System.out.println(driverCourse.getParametersString());

        serverCom = new CommunicationStub();
        System.out.println("Send request");
        int travelID = serverCom.declareCourse(driverCourse);
        System.out.println("the course was declared with ID: " + travelID);
        System.out.println("Request sent");

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }




}
