package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class BeerListActivity extends Activity {

    private ArrayList<Beer> beerList = new ArrayList<Beer>();

    //BeerAdapter beerAdapter;

    private ListView beerListView ;

    private EditText beerFilterText;

    private String sortKey1 = "brewery";
    private String sortKey2 = "beer";

    final Globals g = Globals.getInstance();

    private static final String TAG = "BeerList";

    public final static String AUTH_ACTION = "com.rjmoseley.beerator.app.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beer_list);

        Crashlytics.log(Log.INFO, TAG, "Created");

        beerListView = (ListView) findViewById(R.id.beerListView);
        beerFilterText = (EditText) findViewById(R.id.filter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.INFO, TAG, "Resumed");
        if (g.getBeerList() == null || g.getBeerList().isEmpty()) {
            Crashlytics.log(Log.INFO, TAG, "Beer download needed");
            downloadBeers();
        } else {
            Crashlytics.log(Log.INFO, TAG, "No beer download needed");
            beerList = g.getBeerList();
            //Sort the beers
            if (beerList.size() > 0) {
                Crashlytics.log(Log.INFO, TAG, "Sorting beers");
                sortBeers("name");
                sortBeers("brewery");
            }
            setListViewContent();
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                Crashlytics.log(Log.INFO, TAG, "Intent bundle exists, checking for com.parse.Data");
                Intent intent = getIntent();
                String action = intent.getAction();
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                Crashlytics.log(Log.INFO, TAG, "Got action " + action + " with:");
                Iterator itr = json.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    Crashlytics.log(Log.INFO, TAG, "..." + key + " => " + json.getString(key));
                }
                String objectId = json.getString("beerObjectId");
                Crashlytics.log(Log.INFO, TAG, "Beer in notification is " + objectId);
                Crashlytics.log(Log.INFO, TAG, "Launching BeerDetailsActivity");
                Intent launchBeerDetails = new Intent(getApplicationContext(), BeerDetailsActivity.class);
                launchBeerDetails.putExtra("objectId", objectId);
                startActivity(launchBeerDetails);
            } catch (JSONException e) {
                Log.d(TAG, "JSONException: " + e.getMessage());
            }
        }




    }

    private void downloadBeers() {
        Crashlytics.log(Log.INFO, TAG, "Downloading beers");
        Toast.makeText(this, "Downloading beers", Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beer");
        query.orderByAscending(sortKey1);
        query.addAscendingOrder(sortKey2);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                Crashlytics.log(Log.INFO, TAG, "Query finished");
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
                    }
                    Crashlytics.log(Log.INFO, TAG, "Beers downloaded: " + beerList.size());
                    Toast.makeText(BeerListActivity.this, beerList.size()+ " beers downloaded",
                            Toast.LENGTH_SHORT).show();
                    setListViewContent();
                    Crashlytics.log(Log.INFO, TAG, "Saving beerList to Globals");
                    g.setBeerlist(beerList);
                } else {
                    Toast.makeText(BeerListActivity.this, "Beer download failed", Toast.LENGTH_SHORT).show();
                    Crashlytics.log(Log.INFO, TAG, "Beer download failed");
                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                            + ", Message: " + e.getMessage());
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void setListViewContent() {
        Crashlytics.log(Log.INFO, TAG, "Setting ListView content");
        final BeerAdapter beerAdapter = new BeerAdapter(this, R.layout.beer_list_item, beerList);
        Crashlytics.log(Log.INFO, TAG, "Beers listed: " + beerList.size());

        beerFilterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Crashlytics.log(Log.INFO, "FilterText", "Text: " + charSequence.toString() + ", Start: " + start
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
                Crashlytics.log(Log.INFO, TAG, "Selected beer is " + objectId);
                Intent launchBeerDetails = new Intent(getApplicationContext(), BeerDetailsActivity.class);
                launchBeerDetails.putExtra("objectId", objectId);
                startActivity(launchBeerDetails);
            }
        });
    }

    public void sortBeers(final String key) {
        Collections.sort(beerList, new Comparator<Beer>() {
            @Override
            public int compare(Beer beer1, Beer beer2) {
                return beer1.get(key).compareTo(beer2.get(key));
            }
        });
    }

    @Override
    public void onBackPressed(){
        Crashlytics.log(Log.INFO, TAG, "Back button pressed, launching home screen");
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
            Crashlytics.log(Log.INFO, TAG, "Settings selected from menu");
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_add) {
            Crashlytics.log(Log.INFO, TAG, "Add selected from menu");
            Intent launchAddBeer = new Intent(this, BeerAddActivity.class);
            startActivity(launchAddBeer);
            return true;
        }
        else if (id == R.id.action_refresh) {
            Crashlytics.log(Log.INFO, TAG, "Refresh selected from menu");
            Toast.makeText(this, "Refreshing Beer List", Toast.LENGTH_SHORT).show();
            beerFilterText.setText("");
            downloadBeers();
        }
        else if (id == R.id.action_logout) {
            Crashlytics.log(Log.INFO, TAG, "Logout selected from menu");
            Intent launchBeerLoginActivity = new Intent(this, BeerLoginActivity.class);
            String message = "logout";
            launchBeerLoginActivity.putExtra(AUTH_ACTION, message);
            startActivity(launchBeerLoginActivity);
            return true;
        } else if (id == R.id.action_recent) {
            Crashlytics.log(Log.INFO, TAG, "Recent ratings selected from menu");
            Intent i = new Intent(this, RecentRatingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
