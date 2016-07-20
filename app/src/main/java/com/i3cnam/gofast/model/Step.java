package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Nestor on 18/07/2016.
 */
public class Step {
    private LatLng startPoint;
    private LatLng endPoint;
    private String encodedPoints;

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public String getEncodedPoints() {
        return encodedPoints;
    }

    public void setEncodedPoints(String encodedPoints) {
        this.encodedPoints = encodedPoints;
    }
}
