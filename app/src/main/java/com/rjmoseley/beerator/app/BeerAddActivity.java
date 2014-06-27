package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;


public class BeerAddActivity extends Activity {

    private static final String TAG = "BeerAdd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_add);
        Crashlytics.log(Log.INFO, TAG, "Created");
    }

    public void addBeer(View view) {
        //Add the beer to the Parse DB
        Crashlytics.log(Log.INFO, TAG, "Adding a new beer");
        EditText beerInput = (EditText) findViewById(R.id.etBeerName);
        EditText breweryInput = (EditText) findViewById(R.id.etBreweryName);
        EditText abvInput = (EditText) findViewById(R.id.etAbv);
        final String beerString = beerInput.getText().toString();
        final String breweryString = breweryInput.getText().toString();
        final String abvString = abvInput.getText().toString();
        final String userString = ParseUser.getCurrentUser().getObjectId();
        final String countryOfOriginString = "";
        if ((beerString.length() > 0) && (breweryString.length() > 0)) {
            final ParseObject newParseBeer = new ParseObject("beer");
            newParseBeer.put("beerName", beerString);
            newParseBeer.put("brewery", breweryString);
            newParseBeer.put("userObjectId", userString);
            newParseBeer.put("countryOfOrigin", countryOfOriginString);

            ParseACL acl = new ParseACL();
            acl.setPublicReadAccess(true);
            newParseBeer.setACL(acl);

            if (abvString.length() > 0) {
                newParseBeer.put("abv", abvString);
            }
            Crashlytics.log(Log.INFO, TAG, "Adding: " + breweryString + ", " + beerString + ", " + abvString);
            newParseBeer.saveInBackground(new SaveCallback() {
                //Once saved to parse grab objectId and save locally
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        InputMethodManager imm = (InputMethodManager)BeerAddActivity
                                .this.getSystemService(Service.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                        Crashlytics.log(Log.INFO, TAG, "Beer saved successfully");
                        Toast.makeText(BeerAddActivity.this, "Beer saved successfully", Toast.LENGTH_SHORT).show();
                        final String objectId = newParseBeer.getObjectId();
                        Globals g = Globals.getInstance();
                        ArrayList<Beer> beerList = g.getBeerList();
                        Beer newBeer = new Beer(beerString, breweryString, objectId);
                        if (abvString.length() > 0) {
                            newBeer.setABV(abvString);
                        }
                        beerList.add(newBeer);
                        Crashlytics.log(Log.INFO, TAG, "Beer added to beerList");
                        g.setBeerlist(beerList);
                        Intent launchBeerDetails = new Intent(getApplicationContext(), BeerDetailsActivity.class);
                        launchBeerDetails.putExtra("objectId", objectId);
                        startActivity(launchBeerDetails);
                    } else {
                        Toast.makeText(BeerAddActivity.this, "Failed to add new beer", Toast.LENGTH_SHORT).show();
                        Crashlytics.log(Log.INFO, TAG, "Failed to save new beer");
                        Crashlytics.log(Log.INFO, TAG, e.getMessage());
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    }
                }
            });

        }
        else {
            Context context = getApplicationContext();
            CharSequence text = "Brewery and Beer Name cannot be empty";
            Crashlytics.log(Log.INFO, TAG, "Not adding beer, Brewery and Beer Name cannot be empty");
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void cancel(View view) {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beer_add, menu);
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
