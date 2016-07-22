package com.i3cnam.gofast.model;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.geo.PlacesService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Nestor on 16/07/2016.
 */
public class Place implements Serializable{
    private String placeName;
    private LatLng coordinates;
    private String placeId;

    public Place(String placeName) {
        this.placeName = placeName;
    }

    public Place(String placeName, String placeId) {
        this.placeName = placeName;
        this.placeId = placeId;
    }

    public Place(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public LatLng getCoordinates() {
        if (coordinates == null) {
            if (placeId != null) {
                this.coordinates = PlacesService.getPlaceLocation(placeId);
            }
        }
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getPlaceId() {
        if (placeId == null) {
            placeId = ((Place) PlacesService.autocomplete(placeName).get(0)).getPlaceId();
        }
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return "Place[place_name=" + this.placeName + ", place_id=" + this.placeId + ", lat=" + this.coordinates.latitude +  ", long=" + this.coordinates.longitude + "]";
    }

    private void readObject(final ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        this.placeName = (String) ois.readObject();
        this.placeId = (String) ois.readObject();
        double latitude = (double) ois.readObject();
        double longitude = (double) ois.readObject();
        if (latitude != 99.9 && longitude != 99.9) {
            this.coordinates = new LatLng(latitude, longitude);
        }
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.placeName);
        oos.writeObject(this.placeId);
        oos.writeObject(coordinates == null ? 99.9 : this.coordinates.latitude);
        oos.writeObject(coordinates == null ? 99.9 : this.coordinates.longitude);
    }



}
