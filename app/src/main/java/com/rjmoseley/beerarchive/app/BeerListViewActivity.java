package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beer_list_view);

        Parse.initialize(this, "7TlbR0Q2rGmZDaHsmDh6YwVBwkREhlQObLY6kvvo", "2h6aF1mhOnShpJ77Ky1PgWENL14WDC39ZWk4gBjL");

        beerListView = (ListView) findViewById(R.id.beerListView);
        setListViewContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Refresh the beer list when resuming this activity
        setListViewContent();
    }

    public void setListViewContent() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.beerListView).setVisibility(View.GONE);
        ParseQuery query = new ParseQuery("BeerList");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                List<Map<String, String>> beerList = new ArrayList<Map<String, String>>();
                for (ParseObject obj : objects) {
                    Map<String, String> t = new HashMap<String, String>();
                    t.put("objectId", obj.getObjectId());
                    t.put("beer", obj.getString("beer"));
                    t.put("brewery", obj.getString("brewery"));
                    beerList.add(t);
                }
                //Sort the beers by sortKey1
                Collections.sort(beerList, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> m1, Map<String, String> m2) {
                        return m1.get(sortKey1).compareTo(m2.get(sortKey1));
                    }
                });
                //Sort the beers by sortKey2
                Collections.sort(beerList, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> m1, Map<String, String> m2) {
                        return m1.get(sortKey2).compareTo(m2.get(sortKey2));
                    }
                });
                SimpleAdapter listAdapter = new SimpleAdapter(BeerListViewActivity.this, beerList,
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
                    }
                });
            }
        });

    }

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
