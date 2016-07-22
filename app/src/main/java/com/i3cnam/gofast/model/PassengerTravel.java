package com.i3cnam.gofast.model;

import java.io.Serializable;

/**
 * Created by Nestor on 18/07/2016.
 */
public class PassengerTravel implements Serializable{

    private int id;
    private User passenger; // passenger doing or asking for a travel
    private Place origin; // initial position of the passenger
    private Place destination; // place where the passenger wants to go to
    private int radius = 500; // distance that the passenger accepts to walk to the pickup and dropoff points

    /*
    GETTERS AND SETTERS ----------------------------------------------------------------------------
     */
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public Place getOrigin() {
        return origin;
    }

    public void setOrigin(Place origin) {
        this.origin = origin;
    }

    public Place getDestination() {
        return destination;
    }

    public void setDestination(Place destination) {
        this.destination = destination;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getParametersString() {
        String returnString = "passenger=" + getPassenger().getNickname();
        returnString += "&origin=";
        returnString += origin.getCoordinates().latitude + ",";
        returnString += origin.getCoordinates().longitude;
        returnString += "&destination=";
        returnString += destination.getCoordinates().latitude + ",";
        returnString += destination.getCoordinates().longitude;
        returnString += "&radius=";
        returnString += Integer.toString(radius);
        return returnString;
    }

}
