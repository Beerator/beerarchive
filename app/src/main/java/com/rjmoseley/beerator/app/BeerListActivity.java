package com.rjmoseley.beerator.app;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class BeerListActivity extends Activity {

    private ArrayList<Beer> beerList = new ArrayList<Beer>();

    //BeerAdapter beerAdapter;

    private ListView beerListView ;

    private EditText beerFilterText;

    private String sortKey1 = "brewery";
    private String sortKey2 = "beer";

    final Globals g = Globals.getInstance();

    public final static String AUTH_ACTION = "com.rjmoseley.beerator.app.MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beer_list);

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

        beerFilterText = (EditText) findViewById(R.id.filter);
        ParseQuery query = new ParseQuery("beer");
        query.orderByAscending(sortKey1);
        query.addAscendingOrder(sortKey2);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    beerList.clear();
                    for (ParseObject obj : objects) {
                        Beer b = new Beer(obj.getString("beerName"),
                                obj.getString("brewery"),
                                obj.getObjectId());
                        if (obj.getString("abv") != null) {
                            b.setABV(obj.getString("abv"));
                        }
                        beerList.add(b);
                        //Log.i("Beer download", "Beer added: " + b.toString());
                    }
                    Log.i("Beer download", "Beers downloaded: " + beerList.size());
                    setListViewContent();
                } else {
                    Log.i("Beer download", "Beer download failed");
                }
                g.setBeerlist(beerList);
            }
        });
    }


    private void setListViewContent() {
        final BeerAdapter beerAdapter = new BeerAdapter(this, R.layout.beer_list_item, beerList);
        Log.i("Beer List", "Beers listed: " + beerList.size());

        beerFilterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.i("FilterText", "Text: " + charSequence.toString() + ", Start: " + start
                                            + ", Before: " + before + ", Count: " + count);
                if (count < before) beerAdapter.resetData();
                if (count == 0) beerAdapter.resetData();
                beerAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        beerListView.setAdapter(beerAdapter);

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

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
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
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_add) {
            Intent launchAddBeer = new Intent(this, BeerAddActivity.class);
            startActivity(launchAddBeer);
            return true;
        }
        else if (id == R.id.action_refresh) {
            beerFilterText.setText("");
            downloadBeers();
        }
        else if (id == R.id.action_logout) {
            Intent launchBeerLoginActivity = new Intent(this, BeerLoginActivity.class);
            String message = "logout";
            launchBeerLoginActivity.putExtra(AUTH_ACTION, message);
            startActivity(launchBeerLoginActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
