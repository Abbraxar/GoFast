package com.i3cnam.gofast.geo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import com.i3cnam.gofast.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Nestor on 14/07/2016.
 */
public class PlacesService {

    private static final String LOG_TAG = "Autocomplete Places";

    /**
     * autocomplete : retrieves a list of places that match with he input pattern
     * @param input: the string pattern to be found
     * @return a list of Place matching with the pattern
     */
    public static ArrayList autocomplete(String input) {

        ArrayList resultList = null;

        // prepare connection string
        StringBuilder sb = new StringBuilder(GeoConstants.API_BASE +
                GeoConstants.PLACES_API  +
                GeoConstants.TYPE_AUTOCOMPLETE +
                GeoConstants.OUT_JSON);
        sb.append("?key=" + GeoConstants.API_KEY);
        sb.append("&components=country:fr");
        try {
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error encoding URL", e);
        }
        LatLng myPosition = LocationService.getActualLocation();
        sb.append("&location=" + GeoConstants.coordinatesUrlParam(myPosition));
        sb.append("&radius=100000");

        // call the service and obtain a response
        String rawJSON = GeoConstants.useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(rawJSON);
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Place toBeAdded = new Place(predsJsonArray.getJSONObject(i).getString("description"),
                        predsJsonArray.getJSONObject(i).getString("place_id"));
                resultList.add(toBeAdded);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }


    /**
     * returns the coordinates of a place identified by its place_id
     * @param placeId
     * @return
     */
    public static LatLng getCoordinatesByPlaceId(String placeId) {

        LatLng resultCoordinates = null;

        StringBuilder sb = new StringBuilder(GeoConstants.API_BASE +
                GeoConstants.PLACES_API  +
                GeoConstants.TYPE_DETAILS +
                GeoConstants.OUT_JSON);
        sb.append("?key=" + GeoConstants.API_KEY);
        sb.append("&components=country:fr");
        try {
            sb.append("&placeid=" + URLEncoder.encode(placeId, "utf8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Error encoding URL", e);
        }

        // call the service and obtain a response
        String rawJSON = GeoConstants.useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(rawJSON);
            JSONObject locationObj = jsonObj.getJSONObject("result")
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            resultCoordinates = new LatLng(locationObj.getDouble("lat"), locationObj.getDouble("lng"));
            // Extract the Place descriptions from the results
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultCoordinates;
    }


    /**
     * returns the coordinates of a place identified by its place_id
     * @param coordinates
     * @return
     */
    public static Place getPlaceByCoordinates(LatLng coordinates) {

        Place foundPlace = new Place(coordinates);
        StringBuilder jsonResults = new StringBuilder();

        StringBuilder sb = new StringBuilder(GeoConstants.API_BASE +
                GeoConstants.GEOCODE_API  +
                GeoConstants.OUT_JSON);
        sb.append("?key=" + GeoConstants.API_KEY);
        sb.append("&components=country:fr");
        sb.append("&latlng=" + GeoConstants.coordinatesUrlParam(coordinates));

        // call the service and obtain a response
        String rawJSON = GeoConstants.useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject addressObj = jsonObj.getJSONObject("result").getJSONObject("adr_address");

            foundPlace.setPlaceId(addressObj.getString("place_id"));
            foundPlace.setPlaceName(addressObj.getString("formatted_address"));

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return foundPlace;
    }


}
