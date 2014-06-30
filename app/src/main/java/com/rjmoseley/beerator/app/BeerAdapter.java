package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.view.LayoutInflater;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.http.impl.conn.tsccm.ConnPoolByRoute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom Adapter to hold beer objects
 */
public class BeerAdapter extends ArrayAdapter<Beer> implements SectionIndexer {

    private CopyOnWriteArrayList<Beer> beerList;
    private CopyOnWriteArrayList<Beer> beerListOrig;
    private Context context;
    private int layoutResourceId;
    private BeerFilter beerFilter;
    public final Object mLock = new Object();
    HashMap<String, Integer> indexer;
    String[] sections;
    private static final String TAG = "BeerAdapter";

    public BeerAdapter(Context context, int layoutResourceId, ArrayList<Beer> beerList) {
        super(context, layoutResourceId, beerList);
        this.beerList = new CopyOnWriteArrayList<Beer>(beerList);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.beerListOrig = new CopyOnWriteArrayList<Beer>(beerList);

        //Store the first character of the Beers to sort and their position
        //Should end up with most of an alphabet
        indexer = new HashMap<String, Integer>();

        //iterate through the beerList
        int size = beerList.size();
        for (int i = size - 1; i >= 0; i--) {
            //Get the first character of the brewery string and add it to the indexer
            String brewery = beerList.get(i).getBrewery();
            indexer.put(brewery.substring(0, 1), i);
        }

        //We need the keys to be in alphabetical order
        //Get the keys
        Set<String> keys = indexer.keySet();
        //Convert to something that can be sorted
        Iterator<String> it = keys.iterator();
        ArrayList<String> keyList = new ArrayList<String>();
        while (it.hasNext()) {
            String key = it.next();
            keyList.add(key);
        }
        //Sort it
        Collections.sort(keyList);

        //Convert to an array of Strings
        sections = new String[keyList.size()];
        keyList.toArray(sections);
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
                    final List<Beer> nBeerList = new CopyOnWriteArrayList<Beer>();

                    for (Beer b : beerList) {
                        if (b.getName().toUpperCase().startsWith(filterString.toUpperCase())) {
                            nBeerList.add(b);
                            Crashlytics.log(Log.INFO, TAG, "Adding by Name: " + b.toString());
                        }
                        else if (b.getBrewery().toUpperCase().startsWith(filterString.toUpperCase())) {
                            nBeerList.add(b);
                            Crashlytics.log(Log.INFO, TAG, "Adding by Brewery: " + b.toString());
                        } else {
                            final String[] nameWords = b.getName().split(" ");
                            final String[] breweryWords = b.getName().split(" ");
                            // Start at index 0, in case valueText starts with space(s)
                            if (nameWords.length > 1) {
                                for (String word : nameWords) {
                                    if (word.toUpperCase().startsWith(filterString.toUpperCase())) {
                                        Crashlytics.log(Log.INFO, TAG, "Adding by Name after split: " + b.toString());
                                        nBeerList.add(b);
                                        break;
                                    }
                                }
                            } else if (breweryWords.length > 1) {
                                // Start at index 0, in case valueText starts with space(s)
                                for (String word : breweryWords) {
                                    if (word.toUpperCase().startsWith(filterString.toUpperCase())) {
                                        Crashlytics.log(Log.INFO, TAG, "Adding by Brewery after split: " + b.toString());
                                        nBeerList.add(b);
                                        break;
                                    }
                                }
                            }
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

    @Override
    public int getCount() {
        return beerList.size();
    }

    @Override
    public Beer getItem(int pos) {
        return beerList.get(pos);
    }

    @Override
    public int getPositionForSection(int section) {
        String letter = sections[section];
        return indexer.get(letter);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0; //We'll never call this
    }

    @Override
    public Object[] getSections() {
        return sections; //To be used for the display
    }
}