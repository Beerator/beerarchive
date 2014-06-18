package com.rjmoseley.beerarchive.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookRequestError;
import com.facebook.LoginActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.ParseException;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("MainActivity", "Initialising Parse & Facebook integration");
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

        // Check if there is a currently logged in user
        // and they are linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            Log.i("MainActivity", "User is already logged in");
            //Below line needed if more data needs to be added to ParseUser from GraphUser
            //getFacebookDetailsBackground();
            launchBeerList();
        } else {
            launchBeerLoginActivity();
        }
    }

    public void launchBeerLoginActivity() {
        Log.i("MainActivity", "Launching login");
        Intent launchBeerLoginActivity = new Intent(this, BeerLoginActivity.class);
        startActivity(launchBeerLoginActivity);
    }

    public void launchBeerList() {
        Log.i("MainActivity", "Launching beer list");
        Intent launchBeerList = new Intent(this, BeerListActivity.class);
        startActivity(launchBeerList);
    }

    private void getFacebookDetailsBackground() {
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    Log.i("Facebook integration", "Adding more user details");
                    ParseUser.getCurrentUser().put("fbId", user.getId());
                    ParseUser.getCurrentUser().put("name", user.getName());
                    ParseUser.getCurrentUser().saveInBackground();
                } else if (response.getError() != null) {
                    if ((response.getError().getCategory() ==
                            FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                            (response.getError().getCategory() ==
                                    FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                        Log.d("Facebook integration", "The facebook session was invalidated.");
                        logoutUser();
                    } else {
                        Log.d("Facebook integration", "Some other error: "
                                + response.getError().getErrorMessage());
                    }
                }
            }
        }).executeAsync();
    }

    private void logoutUser() {
        // Log the user out
        ParseUser.logOut();
        // Go to the login view
        Log.i("MainActivity", "Restarting login");
        Intent launchLoginActivity = new Intent(this, LoginActivity.class);
        startActivity(launchLoginActivity);
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
        else if (id ==R.id.action_search) {
            return true;
        }
        else if (id == R.id.action_add) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
