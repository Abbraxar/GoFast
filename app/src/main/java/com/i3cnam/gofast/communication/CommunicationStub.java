package com.i3cnam.gofast.communication;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nestor on 22/07/2016.
 */
public class CommunicationStub implements CommInterface {

    @Override
    public List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel) {
        List<Carpooling> returnList = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("y/M/d H:m");

        Carpooling carpooling = new Carpooling();

        //---------------------------------------------------------------
        carpooling.setId(1);
        carpooling.setPickupPoint(new LatLng(43.61 , 1.45));
        carpooling.setDropoffPoint(new LatLng(43.66 , 1.44));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:51"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        carpooling = new Carpooling();
        carpooling.setId(2);
        carpooling.setPickupPoint(new LatLng(43.62 , 1.46));
        carpooling.setDropoffPoint(new LatLng(43.65 , 1.42));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:52"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        carpooling = new Carpooling();
        carpooling.setId(3);
        carpooling.setPickupPoint(new LatLng(43.63 , 1.47));
        carpooling.setDropoffPoint(new LatLng(43.64 , 1.40));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------

        returnList.add(carpooling);
        return returnList;
    }

    @Override
    public int declareCourse(DriverCourse driverCourse) {
        return 1;
    }

    @Override
    public void requestCarpool(Carpooling carpooling) {

    }

    @Override
    public void acceptCarpool(Carpooling carpooling) {

    }

    @Override
    public void updatePosition(DriverCourse driverCourse) {

    }

    @Override
    public void updateCourse(DriverCourse driverCourse) {

    }

    @Override
    public void observeCourse(DriverCourse driverCourse) {

    }

    @Override
    public void unobserveCourse(DriverCourse driverCourse) {

    }

    @Override
    public List<Carpooling> getTravelState(PassengerTravel passengerTravel) {
        List<Carpooling> returnList = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("y/M/d H:m");

        Carpooling carpooling = new Carpooling();

        //---------------------------------------------------------------
        carpooling.setId(1);
        carpooling.setPickupPoint(new LatLng(43.61 , 1.45));
        carpooling.setDropoffPoint(new LatLng(43.66 , 1.44));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:51"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        carpooling = new Carpooling();
        carpooling.setId(2);
        carpooling.setPickupPoint(new LatLng(43.62 , 1.46));
        carpooling.setDropoffPoint(new LatLng(43.65 , 1.42));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:52"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        carpooling = new Carpooling();
        carpooling.setId(3);
        carpooling.setPickupPoint(new LatLng(43.63 , 1.47));
        carpooling.setDropoffPoint(new LatLng(43.64 , 1.40));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------

        returnList.add(carpooling);
        return returnList;
    }

    @Override
    public void observeTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public void unobserveTravel(PassengerTravel passengerTravel) {

    }
}
