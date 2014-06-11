package com.rjmoseley.beerarchive.app;

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
 * Created by richmose on 11/06/14.
 */
public class BeerAdapter extends ArrayAdapter<Beer> {
    private ArrayList<Beer> beerList;
    private Context context;

    public BeerAdapter(ArrayList<Beer> beerList, Context ctx) {
        super(ctx, R.layout.beer_list_item);
        this.beerList = beerList;
        this.context = ctx;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("BeerAdapter","getView");
        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.beer_list_item, parent, false);
        }
        // Now we can fill the layout with the right values
        TextView text1 = (TextView) convertView.findViewById(R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(R.id.text2);
        Beer b = beerList.get(position);

        text1.setText(b.getBrewery());
        Log.i("getView", b.getBrewery());
        text2.setText(b.getName());

        return convertView;
    }

    private class BeerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return null;
        }


        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {

        }
    }
}