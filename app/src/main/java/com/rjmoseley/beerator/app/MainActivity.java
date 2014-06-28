package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
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

    private static final String TAG = "Main";
    public final static String AUTH_ACTION = "com.rjmoseley.beerator.app.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_main);
        Crashlytics.log(Log.INFO, TAG, "Created");

        Crashlytics.log(Log.INFO, TAG, "Initialising Parse & Facebook integration");
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

        // Save the current Installation to Parse.
        Crashlytics.log(Log.INFO, TAG, "Saving Installation data");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        Crashlytics.log(Log.INFO, TAG, "Setting up push notifications");
        PushService.setDefaultPushCallback(this, BeerListActivity.class, R.drawable.ic_stat_beerglass);
        ParseAnalytics.trackAppOpened(getIntent());

        launchBeerLoginActivity();
    }

    private void launchBeerLoginActivity() {
        Crashlytics.log(Log.INFO, TAG, "Launching BeerLoginActivity");
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
        if (id == R.id.action_logout) {
            Crashlytics.log(Log.INFO, TAG, "Logout selected from menu");
            Intent launchBeerLoginActivity = new Intent(this, BeerLoginActivity.class);
            String message = "logout";
            launchBeerLoginActivity.putExtra(AUTH_ACTION, message);
            startActivity(launchBeerLoginActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
