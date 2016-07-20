package com.i3cnam.gofast.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.geo.PlacesService;
import com.i3cnam.gofast.model.PlaceClass;

import java.util.ArrayList;

public class EnterDestination extends Activity implements OnItemClickListener {
    public final static String DESTINATION = "com.i3cnam.gofast.DESTINATION";
    public final static String RADIUS = "com.i3cnam.gofast.RADIUS";
    private GooglePlacesAutocompleteAdapter autocompleteAdapter;
    private PlaceClass selectedPlace;
    private String userType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.userType = intent.getStringExtra(Main.USER_TYPE);

        setContentView(R.layout.activity_enter_destination);

        NumberPicker np;
        np = (NumberPicker) findViewById(R.id.radius);
        String[] values = {"100","200","300","400","500","600","700","800","900","1000"};
        np.setDisplayedValues(values);
        if (userType.equals("driver")) {
            np.setVisibility(View.INVISIBLE);
        }
        /*
        np.setValue(500);
        np.setMinValue(100);
        np.setMaxValue(1000);
*/

        np.setWrapSelectorWheel(false);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        autocompleteAdapter = (GooglePlacesAutocompleteAdapter)autoCompView.getAdapter();
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        System.out.println("onItemClick OK");
//        String str = ((PlaceClass) adapterView.getItemAtPosition(position)).getPlaceName();
        String str = (adapterView.getItemAtPosition(position)).toString();
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        selectedPlace = autocompleteAdapter.getResultList().get(position);
        System.out.println("SELECTED PLACE:");
        System.out.println(selectedPlace.getPlaceName());
        System.out.println(selectedPlace.getPlaceId());
    }



    public void validConfigureTravel(View view) {

//        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
//        System.out.println("le texte: " + textView.getText());
//        intent.putExtra(DESTINATION, selectedPlace);
        if (selectedPlace == null) {

            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
//            selectedPlace = new PlaceClass("toto");
            selectedPlace = new PlaceClass(textView.getText().toString());
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
        private ArrayList<PlaceClass> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            System.out.println("GooglePlacesAutocompleteAdapter OK");
        }

        public ArrayList<PlaceClass> getResultList() {
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
                        resultList = PlacesService.autocomplete(constraint.toString());

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