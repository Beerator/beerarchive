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

public class RecentRatingsAdapter extends ArrayAdapter<Beer> {

    private ArrayList<Beer> beerList;
    private Context context;
    private int layoutResourceId;

    private String ratingSystem = "1-5+";

    public RecentRatingsAdapter(Context context, int layoutResourceId, ArrayList<Beer> beerList) {
        super(context, layoutResourceId, beerList);
        this.beerList = beerList;
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
            holder.beer = (TextView)row.findViewById(R.id.text1);
            holder.rating = (TextView)row.findViewById(R.id.text3);
            holder.details = (TextView)row.findViewById(R.id.text2);

            row.setTag(holder);
        }
        else {
            holder = (RatingsHolder)row.getTag();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yy");

        BeerRating beerRating = beerList.get(position).getRatingsList().get(0);

        String detailsString;

        if (beerRating.getUserName() == null) {
            detailsString = dateFormat.format(beerRating.getDate());
        } else {
            detailsString = beerRating.getUserName() + ", "
                    + dateFormat.format(beerRating.getDate());
        }

        String beerString = beerList.get(position).getBrewery() + ", "
                + beerList.get(position).getName();

        holder.beer.setText(beerString);
        holder.rating.setText(beerRating.getRating(ratingSystem));
        holder.details.setText(detailsString);

        return row;
    }

    private class RatingsHolder {
        public TextView beer;
        public TextView rating;
        public TextView details;
    }

}
