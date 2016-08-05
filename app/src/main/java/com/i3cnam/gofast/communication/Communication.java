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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 19/07/2016.
 */
public class Communication implements CommInterface {

    static final String SERVER_IP = "http://10.0.2.2:9090"; // serveur local
//    static final String SERVER_IP = "http://92.222.82.175:9090/"; // serveur OVH
    static final String DECLARE_COURSE = "/declare_course";
    static final String DECLARE_TRAVEL = "/declare_travel";
    static final String FIND_MATCHES = "/find_matches";
    static final String REQUEST_CARPOOL = "/request_carpooling";
    static final String CANCEL_REQUEST = "/cancel_request";
    static final String ACCEPT_CARPOOL = "/accept_carpooling";
    static final String REFUSE_CARPOOL = "/refuse_carpooling";
    static final String ABORT_CARPOOL = "/abort_carpooling";
    static final String GET_TRAVEL = "/get_travel";
    static final String GET_COURSE = "/get_course";
    static final String UPDATE_POSITION = "/update_position";
    static final String UPDATE_COURSE = "/update_course";

    private static final String LOG_TAG = "Server Communication";
    private static final DateFormat format = new SimpleDateFormat("y/M/d H:m");


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
    public int declareTravel(PassengerTravel passengerTravel) {
        // prepare the return variable
        int returnValue = 0;

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + DECLARE_TRAVEL);
        sb.append("?" + passengerTravel.getParametersString());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());


        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get course id
            returnValue = jsonObject.getInt("travel_id");

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return returnValue;
    }


    @Override
    public List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel) {
        // prepare the return variable
        List<Carpooling> matchesList = new ArrayList<>();

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + FIND_MATCHES);
        sb.append("?travel_id" + travel.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get the element matches
            JSONArray routesJsonArray = jsonObject.getJSONArray("matches");

            // browse the element matches
            for (int i = 0; i < routesJsonArray.length(); i++) {
                matchesList.add(parseCarpoolingJsonObject(routesJsonArray.getJSONObject(i)));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return matchesList;
    }


    @Override
    public void requestCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + REQUEST_CARPOOL);
        sb.append("?carpool_id=" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());
    }


    @Override
    public void acceptCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + ACCEPT_CARPOOL);
        sb.append("?carpool_id=" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());
    }

    @Override
    public void refuseCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + REFUSE_CARPOOL);
        sb.append("?carpool_id=" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

    }


    @Override
    public void cancelRequest(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + CANCEL_REQUEST);
        sb.append("?carpool_id=" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

    }


    @Override
    public void abortCarpool(Carpooling carpooling) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + ABORT_CARPOOL);
        sb.append("?carpool_id=" + carpooling.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

    }


    @Override
    public void updatePosition(DriverCourse driverCourse) {
        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + UPDATE_POSITION);
        sb.append("?course_id=" + driverCourse.getId());
        sb.append("&new_position=" + GeoConstants.coordinatesUrlParam(driverCourse.getActualPosition()));

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

    }

    @Override
    public void updateCourse(DriverCourse driverCourse) {

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + UPDATE_COURSE);
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
    public void observeTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public void unobserveTravel(PassengerTravel passengerTravel) {

    }

    @Override
    public List<Carpooling> getTravelState(PassengerTravel travel) {
        // prepare the return variable
        List<Carpooling> matchesList = new ArrayList<>();

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + GET_TRAVEL);
        sb.append("?travel_id=" + travel.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get the element matches
            JSONArray routesJsonArray = jsonObject.getJSONArray("matches");

            // browse the element matches
            for (int i = 0; i < routesJsonArray.length(); i++) {

                matchesList.add(parseCarpoolingJsonObject(routesJsonArray.getJSONObject(i)));

            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return matchesList;
    }

    @Override
    public List<Carpooling> getCourseState(DriverCourse driverCourse) {
        // prepare the return variable
        List<Carpooling> requestedCarpoolings = new ArrayList<>();

        // prepare the string for the request
        StringBuilder sb = new StringBuilder(SERVER_IP + GET_COURSE);
        sb.append("?course_id=" + driverCourse.getId());

        // call the service and obtain a response
        String rawJSON = useService(sb.toString());

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObject = new JSONObject(rawJSON);

            // get the element matches
            JSONArray routesJsonArray = jsonObject.getJSONArray("matches");

            // browse the element matches
            for (int i = 0; i < routesJsonArray.length(); i++) {

                requestedCarpoolings.add(parseCarpoolingJsonObject(routesJsonArray.getJSONObject(i)));

            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return requestedCarpoolings;
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

    private static Carpooling parseCarpoolingJsonObject(JSONObject jsonCarpooling) {

        Carpooling carpooling = new Carpooling();
        try {
            int id = jsonCarpooling.getInt("id");
            carpooling.setId(id);

            JSONObject pickupPoint = jsonCarpooling.getJSONObject("pickup_point");
            JSONObject dropoffPoint = jsonCarpooling.getJSONObject("dropoff_point");

            carpooling.setPickupPoint(
                    new LatLng(Double.parseDouble(pickupPoint.getString("lat")),
                            Double.parseDouble(pickupPoint.getString("long")))
            );
            carpooling.setDropoffPoint(
                    new LatLng(Double.parseDouble(dropoffPoint.getString("lat")),
                            Double.parseDouble(dropoffPoint.getString("long")))
            );

            String pickupTime = jsonCarpooling.getString("pickup_time");
            try {
                carpooling.setPickupTime(format.parse(pickupTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String state = jsonCarpooling.getString("state");
            carpooling.setState(Carpooling.CarpoolingState.valueOf(state));

            float fare = (float) jsonCarpooling.getDouble("fare");
            carpooling.setFare(fare);
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return carpooling;
    }

}
