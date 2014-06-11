package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    private List<Map<String, String>> beerList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> beerListToDisplay = new ArrayList<Map<String, String>>();

    SimpleAdapter listAdapter;

    private ListView beerListView ;

    private EditText beerFilter;

    private String sortKey1 = "brewery";
    private String sortKey2 = "beer";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beer_list_view);

        Parse.initialize(this, "7TlbR0Q2rGmZDaHsmDh6YwVBwkREhlQObLY6kvvo", "2h6aF1mhOnShpJ77Ky1PgWENL14WDC39ZWk4gBjL");

        beerListView = (ListView) findViewById(R.id.beerListView);

        downloadBeers();

    }

    @Override
    public void onResume() {
        super.onResume();
        //Refresh the beer list when resuming this activity
        //setListViewContent();
    }

    private void downloadBeers() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.beerListView).setVisibility(View.GONE);
        ParseQuery query = new ParseQuery("BeerList");
        query.orderByAscending(sortKey1).addAscendingOrder(sortKey2)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            for (ParseObject obj : objects) {
                                Map<String, String> t = new HashMap<String, String>();
                                t.put("objectId", obj.getObjectId());
                                t.put("beer", obj.getString("beer"));
                                t.put("brewery", obj.getString("brewery"));
                                beerList.add(t);

                            }
                            Log.i("Beer download", "Beers downloaded: " + beerList.size());
                            beerListToDisplay = beerList;
                            setListViewContent();
                        } else {
                            Log.i("Beer download", "Beer download failed");
                        }
                    }
                });
    }


    private void setListViewContent() {
        SimpleAdapter listAdapter = new SimpleAdapter(BeerListViewActivity.this, beerListToDisplay,
                R.layout.beer_list_item,
                new String[] {"brewery", "beer", "objectId"},
                new int[] {R.id.text1,
                        R.id.text2,
                        R.id.objectId});

        beerListView.setAdapter(listAdapter);

        Log.i("Beer List", "Beers listed: " + beerListToDisplay.size());

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

    private void filterBeerList(String constraint) {

    }

    /*private class BeerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = beerList;
                results.count = beerList.size();
            }
            else {
                // We perform filtering operation
                List<Map<String, String>> nBeerList = new ArrayList<Map<String, String>>();

                for (Map<String, String> b : beerList) {
                    if (b.get("beer").toUpperCase().startsWith(constraint.toString().toUpperCase())) {
                        nBeerList.add(b);
                    } else if (b.get("brewery").toUpperCase().startsWith(constraint.toString().toUpperCase())) {
                        nBeerList.add(b);
                    }
                }

                results.values = nBeerList;
                results.count = nBeerList.size();
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            // Now we have to inform the adapter about the new list filtered
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                planetList = (List<Planet>) results.values;
                notifyDataSetChanged();
            }

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
            downloadBeers();
        }
        return super.onOptionsItemSelected(item);
    }

}
