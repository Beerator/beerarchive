package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BeerListViewActivity extends Activity {

    private ListView beerListView ;

    private String sortKey = "brewery";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_list_view);

        Parse.initialize(this, "7TlbR0Q2rGmZDaHsmDh6YwVBwkREhlQObLY6kvvo", "2h6aF1mhOnShpJ77Ky1PgWENL14WDC39ZWk4gBjL");

        beerListView = (ListView) findViewById(R.id.beerListView);
        setListViewContent();
    }

    public void setListViewContent() {

        ParseQuery query = new ParseQuery("BeerList");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                List<Map<String, String>> beerList = new ArrayList<Map<String, String>>();
                for (ParseObject obj : objects) {
                    Map<String, String> t = new HashMap<String, String>();
                    t.put("beer", obj.getString("beer"));
                    t.put("brewery", obj.getString("brewery"));
                    beerList.add(t);
                }
                //Sort the beers
                Collections.sort(beerList, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> m1, Map<String, String> m2) {
                        return m1.get(sortKey).compareTo(m2.get(sortKey));
                    }
                });
                SimpleAdapter listAdapter = new SimpleAdapter(BeerListViewActivity.this, beerList,
                        R.layout.beer_list_item,
                        new String[] {"brewery", "beer"},
                        new int[] {android.R.id.text1,
                                android.R.id.text2});
                beerListView.setAdapter(listAdapter);
            }
        });

    }

        /*
        ParseObject beer = new ParseObject("BeerList");
        beer.put("beer", "Nicks");
        beer.put("brewery", "Milk Street");
        beer.saveInBackground();
        beer = new ParseObject("BeerList");
        beer.put("beer", "Beer");
        beer.put("brewery", "Milk Street");
        beer.saveInBackground();
*/

 /*       //Create list of beers
        String[] beers = new String[] {"Beer 1", "Beer 2", "Beer 3", "Beer 4"};
        String[] breweries = new String[] {"Brewery 9", "Brewery 8", "Brewery 7", "Brewery 6"};
*/

//        List<Map<String,String>> beerList = new ArrayList<Map<String, String>>();

/*        for (int i=0; i<4; i++) {
            Map<String, String> entry = new HashMap<String, String>(2);
            entry.put("beer", beers[i]);
            entry.put("brewery", breweries[i]);
            beerList.add(entry);
        }*/

/*        //Sort the beers
        Collections.sort(beerList, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> m1, Map<String, String> m2) {
                return m1.get(sortKey).compareTo(m2.get(sortKey));
            }
        });*/
/*

        beerListView = (ListView) findViewById(R.id.beerListView);
        listAdapter = new SimpleAdapter(this, beerList,
                                        R.layout.beer_list_item,
                                        new String[] {"brewery", "beer"},
                                        new int[] {android.R.id.text1,
                                                   android.R.id.text2});
        beerListView.setAdapter( listAdapter );
*/

/*        //Working implementation using the ParseQueryAdapter
        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(this, "BeerList");
        adapter.setTextKey("beer");
        ListView beerListView = (ListView) findViewById(R.id.beerListView);
        beerListView.setAdapter(adapter);*/

/*    }*/


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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
