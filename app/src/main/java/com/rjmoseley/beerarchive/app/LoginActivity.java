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
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.rjmoseley.beerarchive.app.R;

public class LoginActivity extends Activity {

    private Button loginButton;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });
    }

    private void onLoginButtonClicked() {
        LoginActivity.this.progressDialog = ProgressDialog.show(
                LoginActivity.this, "", "Logging in...", true);

        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                LoginActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Log.d("Facebook login", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("Facebook login", "User signed up and logged in through Facebook!");
                    getFacebookDetailsBackground();
                    launchBeerList();
                } else {
                    Log.d("Facebook login", "User logged in through Facebook!");
                    getFacebookDetailsBackground();
                    launchBeerList();
                }
            }
        });
    }

    private void getFacebookDetailsBackground() {
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    ParseUser.getCurrentUser().put("fbId", user.getId());
                    ParseUser.getCurrentUser().put("name", user.getName());
                    ParseUser.getCurrentUser().put("username", user.getUsername());
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

    public void launchBeerList() {
        Log.i("LoginActivity", "Launching beer list");
        Intent launchBeerList = new Intent(this, BeerListActivity.class);
        startActivity(launchBeerList);
    }

    private void logoutUser() {
        // Log the user out
        ParseUser.logOut();
        // Go to the login view
        Log.i("LoginActivity", "Restarting login");
        Intent launchLoginActivity = new Intent(this, LoginActivity.class);
        startActivity(launchLoginActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
