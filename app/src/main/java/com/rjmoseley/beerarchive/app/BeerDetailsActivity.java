package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;


public class BeerDetailsActivity extends Activity {

    private ArrayList<Beer> beerList = new ArrayList<Beer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_details);

        final TextView breweryName = (TextView) findViewById(R.id.breweryName);
        final TextView beerName = (TextView) findViewById(R.id.beerName);

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        Log.i("Beer details", "objectId: " + objectId);

        Globals g = Globals.getInstance();
        beerList = g.getBeerList();
        for (Beer b : beerList) {
            if (b.getObjectId().equals(objectId)) {
                Log.i("Beer details", "Beer details found " + b.toString());
                beerName.setText(b.getName());
                breweryName.setText(b.getBrewery());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beer_details, menu);
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
        return super.onOptionsItemSelected(item);
    }
}
