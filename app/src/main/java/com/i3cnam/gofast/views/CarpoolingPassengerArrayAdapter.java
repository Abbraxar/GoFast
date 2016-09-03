package com.i3cnam.gofast.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.model.Carpooling;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alix on 03/09/2016.
 */
public class CarpoolingPassengerArrayAdapter extends ArrayAdapter<Carpooling> {
    List<Carpooling> carpoolings;

    public CarpoolingPassengerArrayAdapter(Context context, int resource, List<Carpooling> carpoolings) {
        super(context, resource, carpoolings);

        this.carpoolings = carpoolings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_carpooling_passenger, parent, false);



        return rowView;
    }
}
