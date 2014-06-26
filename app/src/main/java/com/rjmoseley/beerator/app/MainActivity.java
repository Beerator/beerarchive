package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.FacebookRequestError;
import com.facebook.LoginActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("MainActivity", "Initialising Parse & Facebook integration");
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

        // Save the current Installation to Parse.
        ParseInstallation.getCurrentInstallation().saveInBackground();

        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());

        launchBeerLoginActivity();
    }

    private void launchBeerLoginActivity() {
        Log.i("MainActivity", "Launching login");
        Intent launchBeerLoginActivity = new Intent(this, BeerLoginActivity.class);
        startActivity(launchBeerLoginActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
