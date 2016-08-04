package com.i3cnam.gofast.geo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Nestor on 15/07/2016.
 */
public class GeoConstants {
    /* base url of the google maps api */
    static final String API_BASE = "https://maps.googleapis.com/maps/api";

    /* urls for the api services */
    static final String PLACES_API = "/place"; // upl for the api places
    static final String DIRECTIONS_API = "/directions"; // url for the api directions
    static final String GEOCODE_API = "/geocode"; // url for the api geocode

    /* urls for the places api options */
    static final String TYPE_AUTOCOMPLETE = "/autocomplete"; // option google places autocomplete
    static final String TYPE_DETAILS = "/details"; // option google places details

    /* return format */
    static final String OUT_JSON = "/json"; // return json format
    static final String OUT_XML = "/xml"; // return xml format

    static final String API_KEY = "AIzaSyBY66w89CytiOdJR85KyK2CtxHV_OQpItQ"; // api key of gofast project

    static final String LOG_TAG = "Generic Log"; // tag for log messages

    /**
     * Connects to the server, makes the request with the string passed in parameter and returns the response
     * @param serviceString the request
     * @return the response
     */
    static String useService(String serviceString) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            Log.d(LOG_TAG, "trying to reach:");
            Log.d(LOG_TAG, serviceString);
            URL url = new URL(serviceString);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing URL", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to API", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return jsonResults.toString();

    }

    /**
     * Returns a string corresponding to google api format of coordinates (ie: "43.63508,1.39703")
     * @param coordinates coordinates to be formatted
     * @return a string like "43.63508,1.39703"
     */
    public static String coordinatesUrlParam(LatLng coordinates) {
        return coordinates.latitude + "," + coordinates.longitude;
    }

}
