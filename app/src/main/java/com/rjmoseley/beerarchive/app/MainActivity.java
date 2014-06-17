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

    private Button loginButton;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.initialize(this, "7TlbR0Q2rGmZDaHsmDh6YwVBwkREhlQObLY6kvvo", "2h6aF1mhOnShpJ77Ky1PgWENL14WDC39ZWk4gBjL");
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

        // Check if there is a currently logged in user
        // and they are linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            // Go to the user info activity
            Log.i("Facebook login", "User is already logged in");
            launchBeerList();
        }

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

    }
    private void onLoginButtonClicked() {
        MainActivity.this.progressDialog = ProgressDialog.show(
                MainActivity.this, "", "Logging in...", true);

        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                MainActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Log.d("Facebook login", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("Facebook login", "User signed up and logged in through Facebook!");
                    getFacebookIdInBackground();
                    launchBeerList();
                } else {
                    Log.d("Facebook login", "User logged in through Facebook!");
                    getFacebookIdInBackground();
                    launchBeerList();
                }
            }
        });
    }
    public void launchBeerList() {
        Log.i("MainActivity", "Launching beer list");
        Intent launchBeerList = new Intent(this, BeerListActivity.class);
        startActivity(launchBeerList);
    }

    private void getFacebookIdInBackground() {
        Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    ParseUser.getCurrentUser().put("fbId", user.getId());
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
        });
    }

    private void logoutUser() {
        // Log the user out
        ParseUser.logOut();
        // Go to the login view

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
}
