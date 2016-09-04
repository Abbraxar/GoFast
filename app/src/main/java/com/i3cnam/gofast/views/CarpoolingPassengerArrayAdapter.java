package com.i3cnam.gofast.views;

import android.content.Context;
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

/**
 * Created by Alix on 03/09/2016.
 */
public class CarpoolingPassengerArrayAdapter extends ArrayAdapter<Carpooling> {
    private List<Carpooling> carpoolings;
    private final static DateFormat formatDate = new SimpleDateFormat("HH:mm");
    private final CarpoolingList context;

    public CarpoolingPassengerArrayAdapter(CarpoolingList context, int resource, List<Carpooling> carpoolings) {
        super(context, resource, carpoolings);

        this.carpoolings = carpoolings;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_carpooling_passenger, parent, false);

        Carpooling c = carpoolings.get(position);

        TextView pickupInfo = (TextView) rowView.findViewById(R.id.pickupInfo);
        TextView dropOffInfo = (TextView) rowView.findViewById(R.id.dropOffInfo);
        TextView pickupTime = (TextView) rowView.findViewById(R.id.pickupTime);
        TextView fare = (TextView) rowView.findViewById(R.id.fare);

        // pickupInfo.setText(carpoolings.get(position).getPickupPoint());
        pickupInfo.setText("pickupInfo: Ici, là ou ailleurs" + c.getId());
        dropOffInfo.setText("dropOffInfo: 19 rue claudius rougenet " + c.getId());
        pickupTime.setText(formatDate.format(c.getPickupTime()) + " " + c.getId());
        fare.setText("3 pesos " + c.getId());

        Button btRequest = (Button) rowView.findViewById(R.id.btRequest);
        Button btDetails = (Button) rowView.findViewById(R.id.btDetails);

        btRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                context.requestCarpool(position);
            }
        });

        btDetails.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // context.requestCarpool(position);
            }
        });

        return rowView;
    }

    public void setCarpoolings(List<Carpooling> carpoolings) {
        this.carpoolings = carpoolings;
        this.notifyDataSetChanged();
    }
}
