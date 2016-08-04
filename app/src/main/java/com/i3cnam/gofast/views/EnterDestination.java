package com.i3cnam.gofast.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.NumberPicker;
import android.widget.Toast;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.userType = intent.getStringExtra(Main.USER_TYPE);

        setContentView(R.layout.activity_enter_destination);

        NumberPicker np;
        np = (NumberPicker) findViewById(R.id.radius);
        // only show radius selector to the pedestrian
        if (userType.equals("driver")) {
            String[] values = {"100","200","300","400","500","600","700","800","900","1000"};
            np.setDisplayedValues(values);
            np.setVisibility(View.INVISIBLE);
        }
        np.setWrapSelectorWheel(false);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        autocompleteAdapter = (GooglePlacesAutocompleteAdapter)autoCompView.getAdapter();
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        System.out.println("onItemClick OK");
//        String str = ((Place) adapterView.getItemAtPosition(position)).getPlaceName();
        String str = (adapterView.getItemAtPosition(position)).toString();
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
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
//        intent.putExtra(RADIUS, Integer.parseInt(((NumberPicker) findViewById(R.id.radius)).getValue());
        intent.putExtra(RADIUS, 500);

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