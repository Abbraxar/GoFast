package com.i3cnam.gofast.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.i3cnam.gofast.R;
import com.i3cnam.gofast.model.Carpooling;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import systr.cartographie.Operations;

/**
 * Created by Alix on 03/09/2016.
 */
public class CarpoolingDriverArrayAdapter extends ArrayAdapter<Carpooling> {
    private List<Carpooling> carpoolings;
    private final static DateFormat formatDate = new SimpleDateFormat("HH:mm");
    final PassengerList context;

    public CarpoolingDriverArrayAdapter(PassengerList context,
                                        int resource,
                                        List<Carpooling> carpoolings) {
        super(context, resource, carpoolings);

        this.carpoolings = carpoolings;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_carpooling_passenger, parent, false);

        final Carpooling c = carpoolings.get(position);

        TextView pickupInfo = (TextView) rowView.findViewById(R.id.pickupInfo);
        TextView dropOffInfo = (TextView) rowView.findViewById(R.id.dropOffInfo);
        TextView pickupTime = (TextView) rowView.findViewById(R.id.pickupTime);
        TextView fare = (TextView) rowView.findViewById(R.id.fare);
        TextView pickupDistance = (TextView) rowView.findViewById(R.id.pickupDistance);
        TextView dropoffDistance = (TextView) rowView.findViewById(R.id.dropoffDistance);

        pickupDistance.setVisibility(View.INVISIBLE);
        dropoffDistance.setVisibility(View.INVISIBLE);

        pickupTime.setText(formatDate.format(c.getPickupTime()));
        fare.setText("€ " + c.getFare());

        new TryToCompletePlaceName(null,
                pickupInfo,
                null)
                .execute(c.getPickupPoint());


        new TryToCompletePlaceName(null,
                dropOffInfo,
                null)
                .execute(c.getDropoffPoint());

        Button btLeft = (Button) rowView.findViewById(R.id.btRequest);
        Button btRight = (Button) rowView.findViewById(R.id.btDetails);

        btLeft.setVisibility(View.VISIBLE);
        btRight.setVisibility(View.VISIBLE);

        // IN_DEMAND
        if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRequested));
            btLeft.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRequested));
            btRight.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
        }
        // IN_PROGRESS
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccepted));
            btLeft.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccepted));
            btRight.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // REFUSED
        else if (c.getState().equals(Carpooling.CarpoolingState.REFUSED)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
            btRight.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // CONFLICT
        else if (c.getState().equals(Carpooling.CarpoolingState.CONFLICT)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
            btRight.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // ACHIEVED
        else if (c.getState().equals(Carpooling.CarpoolingState.ACHIEVED)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
            btRight.setVisibility(View.INVISIBLE);
            btLeft.setVisibility(View.INVISIBLE);
        }

        // following the carpooling, change the action button **************************************
        // IN_DEMAND : Action cancel request
        if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
            btLeft.setText(R.string.acceptCarpool);
            btLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.acceptCarpool(position);
                }
            });
            btRight.setText(R.string.refuseCarpool);
            btRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.refuseCarpool(position);
                }
            });
        }
        // IN_PROGRESS : Action abort carpool
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
            btLeft.setText(R.string.validate_end_carpool);
            btLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.validateCarpool(position);
                }
            });
            btRight.setText(R.string.abortCarpoolingLabel);
            btRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.abortCarpool(position);
                }
            });
        }
        // REFUSED : Action abort carpool
        else if (c.getState().equals(Carpooling.CarpoolingState.REFUSED)) {
            btLeft.setVisibility(View.INVISIBLE);
            btRight.setVisibility(View.INVISIBLE);
        }
        // CONFLICT : Action abort carpool
        else if (c.getState().equals(Carpooling.CarpoolingState.CONFLICT)) {
            btLeft.setVisibility(View.INVISIBLE);
            btRight.setVisibility(View.INVISIBLE);
        }

        return rowView;
    }
}
