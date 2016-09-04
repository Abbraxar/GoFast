package com.i3cnam.gofast.views;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.i3cnam.gofast.geo.PlacesService;
import com.i3cnam.gofast.model.Place;

/**
 * Request for place name in a new thread
 */
public class TryToCompletePlaceName extends AsyncTask<LatLng, String,String> {
    Marker marker;
    TextView textView;
    String type;
    Place myPlace;

    public TryToCompletePlaceName(Marker marker, TextView textView, String type) {
        this.marker = marker;
        this.textView = textView;
        this.type = type;
    }

    protected String doInBackground(LatLng... latLngs) {
        this.myPlace = PlacesService.getPlaceByCoordinates(latLngs[0]);
        return null;
    }

    protected void onPostExecute(String result) {
        if (marker != null) {marker.setSnippet(myPlace.getPlaceName());}
        textView.setText((type == null ? "" : type + ": " ) + myPlace.getPlaceName());
    }
}