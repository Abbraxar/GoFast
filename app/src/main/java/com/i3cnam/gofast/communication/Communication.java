package com.i3cnam.gofast.communication;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.geo.GeoConstants;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 19/07/2016.
 */
public class Communication implements CommInterface {

    static final String SERVER_IP = "http://10.0.2.2:9090";
    static final String FIND_MATCHES = "/find_matches";
    static final String DECLARE_COURSE = "/declare_course";
    static final String REQUEST_CARPOOL = "/request_carpooling";
    static final String ACCEPT_CARPOOL = "/accept_carpooling";
    static final String GET_TRAVEL = "/accept_carpooling";
    static final String UPDATE_POSITION = "/update_position";

    private static final String LOG_TAG = "Server Communication";

    @Override
    public List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel) {
        // prepare the return variable
        List<Carpooling> matchesList = new ArrayList<>();

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + FIND_MATCHES);
        sb.append("?" + travel.getParametersString());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get the element matches
            JSONArray routesJsonArray = jsonObject.getJSONArray("matches");

            // browse the element matches
            for (int i = 0; i < routesJsonArray.length(); i++) {
                Carpooling toBeAdded = new Carpooling();

                JSONObject pickupPoint = routesJsonArray.getJSONObject(i).getJSONObject("pickup_point");
                JSONObject dropoffPoint = routesJsonArray.getJSONObject(i).getJSONObject("dropoff_point");
                String pickupTime = routesJsonArray.getJSONObject(i).getString("pickup_time");

                toBeAdded.setPickupPoint(
                        new LatLng(Double.parseDouble(pickupPoint.getString("lat")),
                                Double.parseDouble(pickupPoint.getString("long")))
                );
                toBeAdded.setDropoffPoint(
                        new LatLng(Double.parseDouble(dropoffPoint.getString("lat")),
                                Double.parseDouble(dropoffPoint.getString("long")))
                );
//                toBeAdded.setPickupTime(Time.valueOf(pickupTime));

                toBeAdded.setState(Carpooling.CarpoolingState.POTENTIAL);

                matchesList.add(toBeAdded);
            }


            } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return matchesList;
    }

    @Override
    public int declareCourse(DriverCourse driverCourse) {
        // prepare the return variable
        int returnValue = 0;

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + DECLARE_COURSE);
        sb.append("?" + driverCourse.getParametersString());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());


        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get course id
            returnValue = jsonObject.getInt("course_id");

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return returnValue;
    }

    @Override
    public void requestCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + REQUEST_CARPOOL);
        sb.append("?carpool_id" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());
    }

    @Override
    public void acceptCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + ACCEPT_CARPOOL);
        sb.append("?carpool_id" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());
    }

    @Override
    public void updatePosition(DriverCourse driverCourse) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + ACCEPT_CARPOOL);
        sb.append("?course_id=" + driverCourse.getId());
        sb.append("&new_position=" + GeoConstants.coordinatesUrlParam(driverCourse.getActualPosition()));

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

    }

    @Override
    public void updateCourse(DriverCourse driverCourse) {

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + ACCEPT_CARPOOL);
        sb.append("?course_id=" + driverCourse.getId());
        sb.append("&new_position=" + GeoConstants.coordinatesUrlParam(driverCourse.getActualPosition()));
        sb.append("&new_encoded_points=" + driverCourse.getEncodedPoints());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());
    }

    @Override
    public void observeCourse(DriverCourse driverCourse) {

    }

    @Override
    public void unobserveCourse(DriverCourse driverCourse) {

    }

    @Override
    public List<Carpooling> getTravelState(PassengerTravel travel) {
        // prepare the return variable
        List<Carpooling> matchesList = new ArrayList<>();

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + FIND_MATCHES);
        sb.append("?" + travel.getParametersString());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get the element matches
            JSONArray routesJsonArray = jsonObject.getJSONArray("matches");

            // browse the element matches
            for (int i = 0; i < routesJsonArray.length(); i++) {
                Carpooling toBeAdded = new Carpooling();

                JSONObject pickupPoint = routesJsonArray.getJSONObject(i).getJSONObject("pickup_point");
                JSONObject dropoffPoint = routesJsonArray.getJSONObject(i).getJSONObject("dropoff_point");
                String pickupTime = routesJsonArray.getJSONObject(i).getString("pickup_time");

                toBeAdded.setPickupPoint(
                        new LatLng(Double.parseDouble(pickupPoint.getString("lat")),
                                Double.parseDouble(pickupPoint.getString("long")))
                );
                toBeAdded.setDropoffPoint(
                        new LatLng(Double.parseDouble(dropoffPoint.getString("lat")),
                                Double.parseDouble(dropoffPoint.getString("long")))
                );
//                toBeAdded.setPickupTime(Time.valueOf(pickupTime));

                toBeAdded.setState(Carpooling.CarpoolingState.POTENTIAL);

                matchesList.add(toBeAdded);
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return matchesList;    }

    @Override
    public void observeTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public void unobserveTravel(PassengerTravel passengerTravel) {

    }


    /**
     * Connects to the server, makes the request with the string passed in parameter and returns the response
     * @param serviceString the request
     * @return the response
     */
    private static String useService(String serviceString) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
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

}
