package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to hold and display beer ratings
 */
public class BeerRatingsAdapter extends ArrayAdapter {

    private ArrayList<BeerRating> beerRatings;
    private Context context;
    private int layoutResourceId;

    public BeerRatingsAdapter(Context context, int layoutResourceId, ArrayList<BeerRating> beerRatings) {
        super(context, layoutResourceId, beerRatings);
        this.beerRatings = beerRatings;
        this.context = context;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RatingsHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RatingsHolder();
            holder.rating = (TextView)row.findViewById(R.id.rating);
            holder.date = (TextView)row.findViewById(R.id.date);

            row.setTag(holder);
        }
        else {
            holder = (RatingsHolder)row.getTag();
        }

        BeerRating beerRating = beerRatings.get(position);
        Log.i("BeerRatingsAdapter", beerRating.toString() + " " + beerRating.getDate().toString());
        holder.rating.setText(beerRating.toString());
        holder.date.setText(beerRating.getDate().toString());

        return row;
    }

    private class RatingsHolder {
        TextView rating;
        TextView date;
    }

}

