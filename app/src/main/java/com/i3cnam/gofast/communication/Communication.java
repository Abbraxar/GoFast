package com.i3cnam.gofast.communication;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.geo.GeoConstants;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.PassengerTravel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nestor on 19/07/2016.
 */
public class Communication {

    static final String SERVER_IP = "http://10.0.2.2:9090";
    static final String FIND_MATCHES = "/find_matches";

    private static final String LOG_TAG = "Server Communication";

    public static List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel) {
        // prepare the return variable
        List<Carpooling> matchesList = new ArrayList<Carpooling>();

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

                toBeAdded.setPickup_point(
                        new LatLng(Double.parseDouble(pickupPoint.getString("lat")),
                                Double.parseDouble(pickupPoint.getString("long")))
                );
                toBeAdded.setDropoff_point(
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


    /**
     * Connects to the server, makes the request with the string passed in parameter and returns the response
     * @param serviceString the request
     * @return the response
     */
    public static String useService(String serviceString) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            System.out.println(serviceString);
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
