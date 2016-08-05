package com.i3cnam.gofast.geo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.i3cnam.gofast.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 15/07/2016.
 */
public class DirectionsService {

    private static final String LOG_TAG = "DirectionsService";

    /* directions options */
    private boolean alternatives; // when alternatives is true, multiple results can be returned
    private Place origin = null; // if origin is null, the device position is used
    private Place destination = null; // mandatory
    private String mode = "driving"; // Specifies the mode of transport to use when calculating directions (driving, walking bicycling)
    private List<LatLng> waypoints = null; // Specifies an array of waypoints. Waypoints alter a route by routing it through the specified location(s)

    private JSONObject resultJson = null;

    /*
     Getters and setters for all options -----------------------------------------------------------
     */

    public void setAlternatives(boolean alternatives) {
        this.alternatives = alternatives;
    }

    public void setOrigin(Place origin) {
        this.origin = origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = new Place(origin);
    }

    public void setDestination(Place destination) {
        this.destination = destination;
    }

    public void unsetOrigin() {
        this.origin = null;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<LatLng> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<LatLng> waypoints) {
        this.waypoints = waypoints;
    }

    public void unsetWaypoints(List<LatLng> waypoints) {
        this.waypoints = null;
    }

    public void addWaypoint(LatLng waypoint) {
        if (this.waypoints == null) {
            this.waypoints = new ArrayList<>();
        }
        waypoints.add(waypoint);
    }

    public void removeWaypoint(LatLng waypoint) {
        waypoints.remove(waypoint);
    }

    /*
    String builders for all options ----------------------------------------------------------------
     */

    private String AlternativeString() {
        if (alternatives) {return "alternatives=true";}
        else {return "";}
    }

    private String OriginString() {
        if (origin.getCoordinates() != null) {
            return "&origin=" +  Double.toString(origin.getCoordinates().latitude) + ","
                    + Double.toString(origin.getCoordinates().longitude);
        }
        else if (origin.getPlaceId() != null) {
            return "&origin=place_id:" + origin.getPlaceId();
        }
        else return "&origin=" + origin.getPlaceName();
    }

    private String DestinationString() {
        if (destination.getPlaceId() != null) {
            return "&destination=place_id:" + destination.getPlaceId();
        }
        else if (destination.getCoordinates() != null) {
            return "&destination=" +  Double.toString(destination.getCoordinates().latitude) + ","
                    + Double.toString(destination.getCoordinates().longitude);
        }
        else return "&destination=" + destination.getPlaceName();
    }

    private String ModeString() {
        if (!mode.equals("driving")) {
            return "&mode=" + mode;
        }
        else return "";
    }

    private String WaypointsString() {
        if (waypoints == null) return "";
        else {
            String returnString = "&waypoints=";
            for (LatLng oneWaypoint: waypoints) {
                if (!returnString.equals("&waypoints=")) returnString += "|";
                returnString += oneWaypoint.latitude + "," + oneWaypoint.longitude;
            }
            return returnString;
        }
    }


    /**
     * ---------------------------------------------------------------------------------------------
     * Requests the Google DirectionsService API for the configured path
     * (it takes into account all the options)
     */
    public void computeDirections() {

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GeoConstants.API_BASE + GeoConstants.DIRECTIONS_API + GeoConstants.OUT_JSON);
            sb.append("?key=" + GeoConstants.API_KEY);

            sb.append(AlternativeString());
            sb.append(OriginString());
            sb.append(DestinationString());
            sb.append(ModeString());
            sb.append(WaypointsString());

            Log.d(LOG_TAG, sb.toString());

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            resultJson = new JSONObject(jsonResults.toString());

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * returns the encoded polyline of a computed path
     * @return the encoded polyline
     */
    public String getEncodedPolyline() {
        try {
            JSONArray routesJsonArray = resultJson.getJSONArray("routes");
            JSONObject polylineJsonObj = routesJsonArray.getJSONObject(0).getJSONObject("overview_polyline");

            return polylineJsonObj.getString("points");
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
            return null;
        }
    }

    public List<LatLng> getPathPoints() {
            return PolyUtil.decode(getEncodedPolyline());
    }

    /**
     * static call of directions api
     * @param destination destination Place
     * @return a list of LatLng points
     */
    public static List getDirections(Place origin, Place destination) {

        List resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GeoConstants.API_BASE + GeoConstants.DIRECTIONS_API + GeoConstants.OUT_JSON);
            sb.append("?key=" + GeoConstants.API_KEY);
            sb.append("&components=country:fr");

            sb.append("&origin=place_id:" + URLEncoder.encode(origin.getPlaceId(), "utf8"));
            sb.append("&destination=place_id:" + URLEncoder.encode(destination.getPlaceId(), "utf8"));

            Log.d(LOG_TAG, sb.toString());

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            Log.d(LOG_TAG, jsonResults.toString());

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray routesJsonArray = jsonObj.getJSONArray("routes");
            JSONObject polylineJsonObj = routesJsonArray.getJSONObject(0).getJSONObject("overview_polyline");

            resultList = PolyUtil.decode(polylineJsonObj.getString("points"));

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}
