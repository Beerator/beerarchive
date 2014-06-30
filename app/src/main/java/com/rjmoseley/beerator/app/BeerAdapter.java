package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom Adapter to hold beer objects
 */
public class BeerAdapter extends ArrayAdapter<Beer> {
    private CopyOnWriteArrayList<Beer> beerList;
    private CopyOnWriteArrayList<Beer> beerListOrig;
    private Context context;
    private int layoutResourceId;
    private BeerFilter beerFilter;
    public final Object mLock = new Object();
    private static final String TAG = "BeerAdapter";

    public BeerAdapter(Context context, int layoutResourceId, ArrayList<Beer> beerList) {
        super(context, layoutResourceId, beerList);
        this.beerList = new CopyOnWriteArrayList<Beer>(beerList);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.beerListOrig = new CopyOnWriteArrayList<Beer>(beerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BeerHolder holder;
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

        //To disable the display of the objectID uncomment below line
        holder.objectId.setVisibility(View.GONE);

        return row;
    }

    private class BeerHolder {
        TextView beerName;
        TextView breweryName;
        TextView objectId;
    }

    private class BeerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            String filterString = constraint.toString();
            try {
                // We implement here the filter logic
                if (constraint.length() == 0) {
                    // No filter implemented we return all the list
                    Crashlytics.log(Log.INFO, TAG, "Filter constraint is empty");
                    results.values = beerList;
                    results.count = beerList.size();
                }
                else {
                    Crashlytics.log(Log.INFO, TAG, "Filtering on: " + filterString);
                    // We perform filtering operation
                    List<Beer> nBeerList = new CopyOnWriteArrayList<Beer>();

                    for (Beer b : beerList) {
                        if (b.getName().toUpperCase().startsWith(filterString.toUpperCase())) {
                            nBeerList.add(b);
                            Crashlytics.log(Log.INFO, TAG, "Adding by Name: " + b.toString());
                        }
                        else if (b.getBrewery().toUpperCase().startsWith(filterString.toUpperCase())) {
                            nBeerList.add(b);
                            Crashlytics.log(Log.INFO, TAG, "Adding by Brewery: " + b.toString());
                        }
                    }
                    Crashlytics.log(Log.INFO, TAG, "Number of items found " + nBeerList.size());
                    results.values = nBeerList;
                    results.count = nBeerList.size();

                }
            } catch (ConcurrentModificationException e) {
                Crashlytics.log(Log.INFO, TAG, "Filtering failed, ConcurrentModificationException");
                Crashlytics.log(Log.INFO, TAG, e.getMessage());
                Crashlytics.logException(e);
                e.printStackTrace();
            } catch (Exception e) {
                Crashlytics.log(Log.INFO, TAG, "Filtering failed with an Exception");
                Crashlytics.log(Log.INFO, TAG, e.getMessage());
                Crashlytics.logException(e);
                e.printStackTrace();
            }
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized (mLock) {
                beerList = (CopyOnWriteArrayList<Beer>) results.values;
                clear();
                for (Beer b : beerList) {
                    add(b);
                }
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public Filter getFilter() {
        if (beerFilter == null) {
            beerFilter = new BeerFilter();
        }
        return beerFilter;
    }

    public void resetData() {
        Crashlytics.log(Log.INFO, TAG, "Resetting data");
        beerList = new CopyOnWriteArrayList<Beer>(beerListOrig);
        notifyDataSetChanged();
    }

    /* Possible fix for issue #22 suggested by
    https://stackoverflow.com/questions/15194835/filtering-custom-adapter-indexoutofboundsexception
     */
    @Override
    public int getCount() {
        return beerList.size();
    }

    @Override
    public Beer getItem(int pos) {
        return beerList.get(pos);
    }
}