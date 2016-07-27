package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Nestor on 18/07/2016.
 */
public class Carpooling {
    // etats du covoiturage
    public enum CarpoolingState{POTENTIAL, IN_DEMAND, IN_PROGRESS, REFUSED, CONFLICT, ACHIEVED};

    private int id;

    private DriverCourse driverCourse; // the driverCourse of the carpool driver
    private PassengerTravel passengerTravel; // the travel of the carpool passenger

    private LatLng pickupPoint; // coordinates of the point the passenger is supposed to be picked up
    private LatLng dropoffPoint; // coordinates of the point the passenger is supposed to be dropped off
    private Date pickupTime; // time when the passenger is supposed to be picked up

    private float fare; // amount of money to be applied to the carpooling

    private CarpoolingState state; // state of carpool


    /* --------------------------- GETTERS AND SETTERS --------------------------- */
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public DriverCourse getDriverCourse() {
        return driverCourse;
    }

    public void setDriverCourse(DriverCourse driverCourse) {
        this.driverCourse = driverCourse;
    }

    public PassengerTravel getPassengerTravel() {
        return passengerTravel;
    }

    public void setPassengerTravel(PassengerTravel passengerTravel) {
        this.passengerTravel = passengerTravel;
    }

    public LatLng getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(LatLng pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public LatLng getDropoffPoint() {
        return dropoffPoint;
    }

    public void setDropoffPoint(LatLng dropoffPoint) {
        this.dropoffPoint = dropoffPoint;
    }

    public Date getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Date pickupTime) {
        this.pickupTime = pickupTime;
    }

    public CarpoolingState getState() {
        return state;
    }

    public float getFare() {
        return fare;
    }

    public void setFare(float fare) {
        this.fare = fare;
    }

    public void setState(CarpoolingState state) {
        this.state = state;
    }

    public User getDriver() {
        return getDriverCourse().getDriver();
    }

    public User getPassenger() {
        return getPassengerTravel().getPassenger();
    }

}
