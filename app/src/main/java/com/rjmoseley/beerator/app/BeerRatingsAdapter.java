package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Adapter to hold and display beer ratings
 */
public class BeerRatingsAdapter extends ArrayAdapter<BeerRating> {

    private ArrayList<BeerRating> beerRatings;
    private Context context;
    private int layoutResourceId;

    private String ratingSystem = "1-5+";

    public BeerRatingsAdapter(Context context, int layoutResourceId, ArrayList<BeerRating> beerRatings) {
        super(context, layoutResourceId, beerRatings);
        this.beerRatings = beerRatings;
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RatingsHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RatingsHolder();
            holder.rating = (TextView)row.findViewById(R.id.rating);
            holder.details = (TextView)row.findViewById(R.id.details);

            row.setTag(holder);
        }
        else {
            holder = (RatingsHolder)row.getTag();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yy");

        BeerRating beerRating = beerRatings.get(position);

        String detailsString;

        if (beerRating.getUserName() == null) {
            detailsString = dateFormat.format(beerRating.getDate());
        } else {
            detailsString = beerRating.getUserName() + ", "
                    + dateFormat.format(beerRating.getDate());
        }

        //Log.i("BeerRatingsAdapter", beerRating.toString() + " " + detailsString);
        holder.rating.setText(beerRating.getRating(ratingSystem));
        holder.details.setText(detailsString);

        return row;
    }

    private class RatingsHolder {
        public TextView rating;
        public TextView details;
    }

}

