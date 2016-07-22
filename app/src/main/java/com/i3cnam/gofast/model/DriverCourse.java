package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

/**
 * Created by Nestor on 18/07/2016.
 */
public class DriverCourse {

    private int id;
    private User driver; // the user thar makes the course
    private PlaceClass origin; // start position of the driver
    private PlaceClass destination; // final position of the driver
    private List<Step> steps; // set of steps
    private String encodedPoints; // encoded gps points of path
    private LatLng actualPosition; // actual position of the driver
    private Date positioningTime; // last time when the position was updated

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
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

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getEncodedPoints() {
        return encodedPoints;
    }

    public void setEncodedPoints(String encodedPoints) {
        this.encodedPoints = encodedPoints;
    }

    public LatLng getActualPosition() {
        return actualPosition;
    }

    public void setActualPosition(LatLng actualPosition) {
        this.actualPosition = actualPosition;
    }

    public Date getPositioningTime() {
        return positioningTime;
    }

    public void setPositioningTime(Date positionningTime) {
        this.positioningTime = positionningTime;
    }

    public String getParametersString() {
        String returnString = "driver=" + getDriver().getNickname();
        returnString += "&origin=";
        returnString += origin.getCoordinates().latitude + ",";
        returnString += origin.getCoordinates().longitude;
        returnString += "&destination=";
        returnString += destination.getCoordinates().latitude + ",";
        returnString += destination.getCoordinates().longitude;
        returnString += "&points=" + getEncodedPoints();
        return returnString;
    }


}
