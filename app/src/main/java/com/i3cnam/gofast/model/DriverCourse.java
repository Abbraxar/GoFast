package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Nestor on 18/07/2016.
 */
public class DriverCourse implements Serializable{

    private int id;
    private User driver; // the user thar makes the course
    private Place origin; // start position of the driver
    private Place destination; // final position of the driver
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

    public List<LatLng> getPath() {
        return PolyUtil.decode(encodedPoints);
    }

    public String getParametersString() {
        String returnString = "driver=" + getDriver().getNickname();
        returnString += "&origin=";
        returnString += origin.getCoordinates().latitude + ",";
        returnString += origin.getCoordinates().longitude;
        returnString += "&destination=";
        returnString += destination.getCoordinates().latitude + ",";
        returnString += destination.getCoordinates().longitude;
        returnString += "&encoded_points=" + getEncodedPoints();
        return returnString;
    }


    private void readObject(final ObjectInputStream ois) throws IOException,
            ClassNotFoundException {

        this.driver = (User) ois.readObject();
        this.origin = (Place) ois.readObject();
        this.destination = (Place) ois.readObject();
        int stepsNr = ois.readInt();
        for (int i = 0 ; i < stepsNr ; i++) {
            steps.add((Step)ois.readObject());
        }
        this.encodedPoints = (String) ois.readObject();

        double latitude = ois.readDouble();
        double longitude = ois.readDouble();
        if (latitude != 99.9 && longitude != 99.9) {
            this.actualPosition = new LatLng(latitude, longitude);
        }
        this.positioningTime = (Date) ois.readObject();
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.driver);
        oos.writeObject(this.origin);
        oos.writeObject(this.destination);
        if (steps == null) {
            oos.writeInt(0);
        }
        else {
            oos.writeInt(steps.size());
            for (Step s : steps) {
                oos.writeObject(s);
            }
        }
        oos.writeObject(this.encodedPoints);
        oos.writeDouble(this.actualPosition == null ? 99.9 : actualPosition.latitude);
        oos.writeDouble(this.actualPosition == null ? 99.9 : actualPosition.longitude);
        oos.writeObject(this.positioningTime);
    }



}
