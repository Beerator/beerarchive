package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseObject;


public class BeerAddActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_add);
    }

    public void addBeer(View view) {
        //Add the beer to the Parse DB
        EditText beerInput = (EditText) findViewById(R.id.beerName);
        EditText breweryInput = (EditText) findViewById(R.id.breweryName);
        String beerString = beerInput.getText().toString();
        String breweryString = breweryInput.getText().toString();
        if ((beerString.length() > 0) && (breweryString.length() > 0)) {
            ParseObject newBeer = new ParseObject("BeerList");
            newBeer.put("beer", beerString);
            newBeer.put("brewery", breweryString);
            newBeer.saveInBackground();
            finish();
        }
        else {
            Context context = getApplicationContext();
            CharSequence text = "Brewery and Beer Name cannot be empty";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    public void cancel(View view) {
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
