package com.i3cnam.gofast.geo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.model.PlaceClass;

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

/**
 * Created by Nestor on 14/07/2016.
 */
public class PlacesService {

    private static final String LOG_TAG = "Autocomplete Places";

    /**
     * autocomplete :
     * param input: the string pattern to be found
     * returns a list of PlaceClass matching with the pattern
     */

    public static ArrayList autocomplete(String input) {

        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GeoConstants.API_BASE + GeoConstants.PLACES_API  + GeoConstants.TYPE_AUTOCOMPLETE + GeoConstants.OUT_JSON);
            sb.append("?key=" + GeoConstants.API_KEY);
//            sb.append("&components=country:fr");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            LatLng myPosition = LocationService.getActualLocation();
            sb.append("&location=" + myPosition.latitude + "," + myPosition.longitude);
            sb.append("&radius=100000");

            System.out.println(sb.toString());

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
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                PlaceClass toBeAdded = new PlaceClass(predsJsonArray.getJSONObject(i).getString("description"),
                        predsJsonArray.getJSONObject(i).getString("place_id"));
                resultList.add(toBeAdded);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }


    public static LatLng getPlaceLocation(String pladeId) {

        LatLng resultCoordinates = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GeoConstants.API_BASE +
                    GeoConstants.PLACES_API  +
                    GeoConstants.TYPE_DETAILS +
                    GeoConstants.OUT_JSON);
            sb.append("?key=" + GeoConstants.API_KEY);
//            sb.append("&components=country:fr");
            sb.append("&placeid=" + URLEncoder.encode(pladeId, "utf8"));

            System.out.println(sb.toString());

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            System.out.println("connection OK");
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            System.out.println("InputStreamReader OK");

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

            System.out.println(jsonResults);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultCoordinates;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultCoordinates;
        }/* finally {
            if (conn != null) {
                conn.disconnect();
            }
        }*/

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject resultsObj = jsonObj.getJSONObject("result");
            JSONObject geometryObj = resultsObj.getJSONObject("geometry");
            JSONObject locationObj = geometryObj.getJSONObject("location");
            resultCoordinates = new LatLng(locationObj.getDouble("lat"), locationObj.getDouble("lng"));
            // Extract the Place descriptions from the results
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultCoordinates;
    }

}
