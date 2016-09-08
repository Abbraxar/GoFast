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
public class CarpoolingPassengerArrayAdapter extends ArrayAdapter<Carpooling> {
    private List<Carpooling> carpoolings;
    private final static DateFormat formatDate = new SimpleDateFormat("HH:mm");
    final CarpoolingList context;

    public CarpoolingPassengerArrayAdapter(CarpoolingList context,
                                           int resource,
                                           List<Carpooling> carpoolings) {
        super(context, resource, carpoolings);

        this.carpoolings = carpoolings;
        this.context = context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.wtf("WTF","getViewFunction");
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

        pickupTime.setText(formatDate.format(c.getPickupTime()));
        fare.setText("€ " + c.getFare());
        pickupDistance.setText((int) Operations.dist2PointsEnM(context.getTravel().getOrigin().getCoordinates(),c.getPickupPoint()) + " m");
        dropoffDistance.setText((int) Operations.dist2PointsEnM(context.getTravel().getDestination().getCoordinates(),c.getDropoffPoint()) + " m");

        new TryToCompletePlaceName(null,
                pickupInfo,
                null)
                .execute(c.getPickupPoint());


        new TryToCompletePlaceName(null,
                dropOffInfo,
                null)
                .execute(c.getDropoffPoint());

        Button btRequest = (Button) rowView.findViewById(R.id.btRequest);
        Button btDetails = (Button) rowView.findViewById(R.id.btDetails);


        // following the carpooling, change the background color ***********************************
        // POTENTIAL :
        if (c.getState().equals(Carpooling.CarpoolingState.POTENTIAL)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
            btDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
            btRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRequested));
        }
        // IN_DEMAND
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRequested));
            btDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRequested));
            btRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
        }
        // IN_PROGRESS
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccepted));
            btDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccepted));
            btRequest.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // REFUSED
        else if (c.getState().equals(Carpooling.CarpoolingState.REFUSED)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
            btRequest.setVisibility(View.INVISIBLE);
            btDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // CONFLICT
        else if (c.getState().equals(Carpooling.CarpoolingState.CONFLICT)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
            btRequest.setVisibility(View.INVISIBLE);
            btDetails.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRefused));
        }
        // ACHIEVED
        else if (c.getState().equals(Carpooling.CarpoolingState.ACHIEVED)) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPotential));
            btRequest.setVisibility(View.INVISIBLE);
            btDetails.setVisibility(View.INVISIBLE);
        }

        // following the carpooling, change the action button **************************************
        // POTENTIAL : Action request
        if (c.getState().equals(Carpooling.CarpoolingState.POTENTIAL)) {
            btRequest.setText(R.string.requestCarpoolLabel);
            btRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.requestCarpool(position);
                }
            });
        }
        // IN_DEMAND : Action cancel request
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_DEMAND)) {
            btRequest.setText(R.string.cancelRequestLabel);
            btRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.cancelRequest(position);
                }
            });
        }
        // IN_PROGRESS : Action abort carpool
        else if (c.getState().equals(Carpooling.CarpoolingState.IN_PROGRESS)) {
            btRequest.setText(R.string.abortCarpoolingLabel);
            btRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.abortCarpooling(position);
                }
            });
        }

        btDetails.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,CarpoolingDetails.class);

                Bundle serviceBundle = new Bundle();
                serviceBundle.putSerializable(CarpoolingDetails.CARPOOLING, c);
                serviceBundle.putSerializable(CarpoolingDetails.TRAVEL, context.getTravel());
                intent.putExtras(serviceBundle);
                context.startActivity(intent);
            }
        });

        return rowView;
    }

    public void setCarpoolings(List<Carpooling> carpoolings) {
        this.carpoolings = carpoolings;
        this.notifyDataSetChanged();
        Log.d("array adapter", "set carpoolings");
    }
}
