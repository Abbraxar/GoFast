package com.i3cnam.gofast.communication;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.User;

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

    private int counter = 0;


    @Override
    public int declareCourse(DriverCourse driverCourse) {
        return 1;
    }

    @Override
    public int declareTravel(PassengerTravel passengerTravel) {
        return 2;
    }

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
        carpooling.setFare(3.25f);
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
        carpooling.setFare(3.30f);
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
        carpooling.setFare(3.50f);
        try {
            carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        return returnList;
    }

    @Override
    public void requestCarpool(Carpooling carpooling) {

    }

    @Override
    public void acceptCarpool(Carpooling carpooling) {

    }

    @Override
    public void refuseCarpool(Carpooling carpooling) {

    }

    @Override
    public void cancelRequest(Carpooling carpooling) {

    }

    @Override
    public void abortCarpool(Carpooling carpooling) {

    }

    @Override
    public void abortCourse(DriverCourse course) {

    }

    @Override
    public void abortTravel(PassengerTravel travel) {

    }

    @Override
    public void updatePosition(DriverCourse driverCourse) {

    }

    @Override
    public void updateCourse(DriverCourse driverCourse) {

    }

    @Override
    public void observeCarpoolCourse(DriverCourse driverCourse) {

    }

    @Override
    public void unobserveCarpoolCourse(DriverCourse driverCourse) {

    }

    @Override
    public void observeCarpoolTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public void unobserveCarpoolTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public List<Carpooling> getCarpoolTravelState(PassengerTravel passengerTravel) {
        counter = counter > 60 ? 0 : counter + 1 ;
        List<Carpooling> returnList = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("y/M/d H:m");

        Carpooling carpooling = new Carpooling();

        //---------------------------------------------------------------
        carpooling.setId(1);
        carpooling.setPickupPoint(new LatLng(43.61 , 1.45));
        carpooling.setDropoffPoint(new LatLng(43.66 , 1.44));
        carpooling.setState(Carpooling.CarpoolingState.POTENTIAL);
        carpooling.setFare(3.25f);
        try {
            if (counter > 15) {
                carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
            }else {
                carpooling.setPickupTime(format.parse("2016/08/01 8:51"));
            }
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
        carpooling.setFare(3.30f);
        try {
            if (counter > 40) {
                carpooling.setPickupTime(format.parse("2016/08/01 8:52"));
            } else if (counter > 30) {
                carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
            } else {
                carpooling.setPickupTime(format.parse("2016/08/01 8:52"));
            }
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
        carpooling.setFare(3.50f);
        try {
            if (counter > 50) {
                carpooling.setPickupTime(format.parse("2016/08/01 8:54"));
            }
            else {
                carpooling.setPickupTime(format.parse("2016/08/01 8:53"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        returnList.add(carpooling);
        //---------------------------------------------------------------
        return returnList;
    }

    @Override
    public List<Carpooling> getCarpoolCourseState(DriverCourse driverCourse) {
        counter++;

        List<Carpooling> returnList = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("y/M/d H:m");

        Carpooling carpooling = new Carpooling();

        //---------------------------------------------------------------
        carpooling.setId(1);
        carpooling.setPickupPoint(new LatLng(43.61 , 1.45));
        carpooling.setDropoffPoint(new LatLng(43.66 , 1.44));
        carpooling.setState(Carpooling.CarpoolingState.IN_DEMAND);
        carpooling.setFare(3.25f);
        try {

            carpooling.setPickupTime(format.parse("2016/08/01 8:51"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (counter > 15) {returnList.add(carpooling);}
        return returnList;
    }

    @Override
    public DriverCourse getDriverCourse(User driver) {
        // prepare the return variable
        DriverCourse driverCourse = new DriverCourse();

        return driverCourse;
    }

    @Override
    public PassengerTravel getPassengerTravel(User passenger) {
        // prepare the return variable
        PassengerTravel passengerTravel = new PassengerTravel();

        return passengerTravel;
    }

    @Override
    public String declareUser(User user) {
        return null;
    }

    @Override
    public String retrieveAccount(String phoneNumber) {
        return null;
    }

}
