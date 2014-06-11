package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.SimpleAdapter;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom Adapter to hold beer objects
 */
public class BeerAdapter extends ArrayAdapter<Beer> {
    private ArrayList<Beer> beerList;
    private ArrayList<Beer> beerListOrig;
    private Context context;
    private int layoutResourceId;
    private BeerFilter beerFilter;

    public BeerAdapter(Context context, int layoutResourceId, ArrayList<Beer> beerList) {
        super(context, layoutResourceId, beerList);
        this.beerList = beerList;
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.beerListOrig = new ArrayList<Beer>(beerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BeerHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new BeerHolder();
            holder.beerName = (TextView)row.findViewById(R.id.text2);
            holder.breweryName = (TextView)row.findViewById(R.id.text1);
            holder.objectId = (TextView)row.findViewById(R.id.objectId);

            row.setTag(holder);
        }
        else {
            holder = (BeerHolder)row.getTag();
        }

        Beer beer = beerList.get(position);
        holder.beerName.setText(beer.getName());
        holder.breweryName.setText(beer.getBrewery());
        holder.objectId.setText(beer.getObjectId());

        return row;
    }

    static class BeerHolder {
        TextView beerName;
        TextView breweryName;
        TextView objectId;
    }

    private class BeerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            Log.i("Filter", "Filter started");
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                Log.i("Filter", "constraint is null");
                results.values = beerList;
                results.count = beerList.size();
            }
            else {
                // We perform filtering operation
                List<Beer> nBeerList = new ArrayList<Beer>();

                for (Beer b : beerList) {
                    if (b.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        nBeerList.add(b);
                }
                Log.i("Filter","Number of items found " + nBeerList.size());
                results.values = nBeerList;
                results.count = nBeerList.size();

            }
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Now we have to inform the adapter about the new list filtered
            beerList = (ArrayList<Beer>)results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l=results.count; i<l; i++) {
                add(beerList.get(i));
                notifyDataSetInvalidated();
            }


        }
    }

    @Override
    public Filter getFilter() {
        if (beerFilter == null) {
            Log.i("Filter", "bring created as currently null");
            beerFilter = new BeerFilter();
        }
        Log.i("Filter", "returning beerFilter");
        return beerFilter;
    }

    public void resetData() {
        beerList = beerListOrig;
    }
}