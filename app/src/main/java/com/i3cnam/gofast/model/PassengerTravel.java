package com.i3cnam.gofast.model;

import java.io.Serializable;

/**
 * Created by Nestor on 18/07/2016.
 */
public class PassengerTravel implements Serializable{
    private User user;
    private PlaceClass origin;
    private PlaceClass destination;
    private int radius = 500;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlaceClass getOrigin() {
        return origin;
    }

    public void setOrigin(PlaceClass origin) {
        this.origin = origin;
    }

    public PlaceClass getDestination() {
        return destination;
    }

    public void setDestination(PlaceClass destination) {
        this.destination = destination;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getParametersString() {
        String returnString = "origin=";
        returnString += origin.getCoordinates().latitude + ",";
        returnString += origin.getCoordinates().longitude;
        returnString += "destination=";
        returnString += destination.getCoordinates().latitude + ",";
        returnString += destination.getCoordinates().longitude;
        returnString += "radius=";
        returnString += Integer.toString(radius);
        return returnString;
    }

}
