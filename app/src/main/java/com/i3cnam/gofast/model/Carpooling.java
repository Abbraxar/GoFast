package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;

/**
 * Created by Nestor on 18/07/2016.
 */
public class Carpooling {
    // etat du covoiturage
    public enum CarpoolingState{POTENTIAL, IN_DEMAND, IN_PROGRESS, REFUSED, CONFLICT, ACHIEVED};

    private DriverCourse driver;
    private PassengerTravel passenger;

    private LatLng pickup_point;
    private LatLng dropoff_point;
    private Time pickupTime;

    private CarpoolingState state;

    public DriverCourse getDriver() {
        return driver;
    }

    public void setDriver(DriverCourse driver) {
        this.driver = driver;
    }

    public PassengerTravel getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerTravel passenger) {
        this.passenger = passenger;
    }

    public LatLng getPickup_point() {
        return pickup_point;
    }

    public void setPickup_point(LatLng pickup_point) {
        this.pickup_point = pickup_point;
    }

    public LatLng getDropoff_point() {
        return dropoff_point;
    }

    public void setDropoff_point(LatLng dropoff_point) {
        this.dropoff_point = dropoff_point;
    }

    public Time getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Time pickupTime) {
        this.pickupTime = pickupTime;
    }

    public CarpoolingState getState() {
        return state;
    }

    public void setState(CarpoolingState state) {
        this.state = state;
    }
}
