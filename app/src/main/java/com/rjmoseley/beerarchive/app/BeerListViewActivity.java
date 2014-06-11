package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BeerListViewActivity extends Activity {

    private ListView beerListView ;

    private String sortKey1 = "beer";
    private String sortKey2 = "brewery";
    private List<ParseObject> beerList = new ArrayList<ParseObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beer_list_view);

        Parse.initialize(this, "7TlbR0Q2rGmZDaHsmDh6YwVBwkREhlQObLY6kvvo", "2h6aF1mhOnShpJ77Ky1PgWENL14WDC39ZWk4gBjL");

        beerListView = (ListView) findViewById(R.id.beerListView);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.beerListView).setVisibility(View.GONE);

        downloadBeers();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Refresh the beer list when resuming this activity
        //setListViewContent();
    }

    private void downloadBeers(){
        ParseQuery query = new ParseQuery("BeerList");
        query.orderByAscending("brewery").addAscendingOrder("beer")
                .findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    beerList = objects;
                    setListViewContent();
                } else {
                    Log.i("Beer download", "Beer download failed");
                }
            }
        });
    }


    private void setListViewContent() {
        //Convert ParseObjects to Map<String, String>
        List<Map<String, String>> beersToDisplay = new ArrayList<Map<String, String>>();
        for (ParseObject beer : beerList) {
            Map<String, String> t = new HashMap<String, String>();
            t.put("objectId", beer.getObjectId());
            t.put("beer", beer.getString("beer"));
            t.put("brewery", beer.getString("brewery"));
            beersToDisplay.add(t);
        }

        SimpleAdapter listAdapter = new SimpleAdapter(BeerListViewActivity.this, beersToDisplay,
                R.layout.beer_list_item,
                new String[] {"brewery", "beer", "objectId"},
                new int[] {R.id.text1,
                        R.id.text2,
                        R.id.objectId});
        beerListView.setAdapter(listAdapter);
        Log.i("Beer List", beerList.size() + " beers listed");
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        findViewById(R.id.beerListView).setVisibility(View.VISIBLE);

        beerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout rl = (RelativeLayout)view;
                TextView tv = (TextView) rl.findViewById(R.id.objectId);
                String objectId = tv.getText().toString();
                Log.i("Beer List", "Selected beer is " + objectId);
                Intent launchBeerDetails = new Intent(getApplicationContext(), BeerDetailsActivity.class);
                launchBeerDetails.putExtra("objectId", objectId);
                startActivity(launchBeerDetails);
            }
        });

    }

/*    private class beerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = beerList;
                results.count = beerList.size();
            }
            else {
                // We perform filtering operation
                List<Planet> nPlanetList = new ArrayList<Planet>();

                for (Planet p : beerList) {
                    if (p.getName().toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        nPlanetList.add(p);
                }

                results.values = nPlanetList;
                results.count = nPlanetList.size();

            }
    return results;
        }
        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
            //
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beer_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id ==R.id.action_search) {
            return true;
        }
        else if (id == R.id.action_add) {
            Intent launchAddBeer = new Intent(this, BeerAddActivity.class);
            startActivity(launchAddBeer);
            return true;
        }
        else if (id == R.id.action_refresh) {
            setListViewContent();
        }
        return super.onOptionsItemSelected(item);
    }

}
