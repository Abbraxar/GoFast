package com.i3cnam.gofast.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.GPSTracker;
import com.i3cnam.gofast.geo.PlacesService;
import com.i3cnam.gofast.model.Place;

import java.util.ArrayList;

public class EnterDestination extends Activity implements OnItemClickListener {
    public final static String DESTINATION = "com.i3cnam.gofast.DESTINATION";
    public final static String RADIUS = "com.i3cnam.gofast.RADIUS";
    private GooglePlacesAutocompleteAdapter autocompleteAdapter;
    private Place selectedPlace;
    private String userType;
    private NumberPicker radius;

    private final static String TAG_LOG = "EnterDestination view";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.userType = intent.getStringExtra(Main.USER_TYPE);

        setContentView(R.layout.activity_enter_destination);

        // only show radius selector to the pedestrian
        if (userType.equals("passenger")) {
            radius = (NumberPicker) findViewById(R.id.radius);
            radius.setVisibility(View.VISIBLE);
            radius.setMinValue(1);
            radius.setMaxValue(10);
            radius.setWrapSelectorWheel(true);
            String[] values = {"100 m", "200 m", "300 m", "400 m", "500 m", "600 m", "700 m", "800 m", "900 m", "1000 m"};
            radius.setDisplayedValues(values);
            radius.setValue(5);

            TextView tv = (TextView) findViewById(R.id.radiusLabel);
            tv.setVisibility(View.VISIBLE);
        }

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        autocompleteAdapter = (GooglePlacesAutocompleteAdapter)autoCompView.getAdapter();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG_LOG, "DESTROY");

        super.onDestroy();
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        System.out.println("onItemClick OK");
//        String str = ((Place) adapterView.getItemAtPosition(position)).getPlaceName();
        String str = (adapterView.getItemAtPosition(position)).toString();
        selectedPlace = autocompleteAdapter.getResultList().get(position);
        System.out.println("SELECTED PLACE:");
        System.out.println(selectedPlace.getPlaceName());
        System.out.println(selectedPlace.getPlaceId());
    }



    public void validConfigureTravel(View view) {

        if (selectedPlace == null) {
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
            selectedPlace = new Place(textView.getText().toString());
        }

        Intent intent = new Intent(this, DestinationMap.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(DESTINATION, selectedPlace);
        intent.putExtras(bundle);
        intent.putExtra(Main.USER_TYPE, userType);
        if (userType.equals("passenger")) {
            intent.putExtra(RADIUS, radius.getValue() * 100);
        }

        startActivity(intent);

    }


    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList<Place> resultList;
        private Context context;
        private GPSTracker gps;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
            gps = new GPSTracker(context);
        }

        public ArrayList<Place> getResultList() {
            return resultList;
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).getPlaceName();
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        Location loc = gps.getLocation();
                        LatLng coord = null;
                        try {
                            coord = new LatLng(loc.getLatitude(), loc.getLongitude());
                        }
                        catch (NullPointerException e) {
                            // JE SUIS PLACE DU CAPITOLE
//                            coord = new LatLng(43.6032661,1.4422609);
                        }
                        resultList = PlacesService.autocomplete(constraint.toString(), coord);

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}