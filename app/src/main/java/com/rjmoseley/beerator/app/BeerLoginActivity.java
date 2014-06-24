package com.rjmoseley.beerator.app;

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
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class BeerLoginActivity extends Activity {

    private Button loginButton;
    private Button logoutButton;
    private Button launchAppButton;
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

        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutButtonClicked();
            }
        });

        launchAppButton = (Button) findViewById(R.id.launchApp);
        launchAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBeerList();
            }
        });

        updateCurrentStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentStatus();
    }

    private void updateCurrentStatus() {
        TextView currentStatus = (TextView) findViewById(R.id.currentStatus);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentStatus.setText(R.string.logged_in);
            findViewById(R.id.loginButton).setVisibility(View.GONE);
            findViewById(R.id.logoutButtonLayout).setVisibility(View.VISIBLE);
        } else {
            currentStatus.setText(R.string.logged_out);
            findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
            findViewById(R.id.logoutButtonLayout).setVisibility(View.GONE);
        }
    }

    private void onLoginButtonClicked() {

        BeerLoginActivity.this.progressDialog = ProgressDialog.show(
                BeerLoginActivity.this, "", "Logging in...", true);

        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                BeerLoginActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Log.d("Facebook login", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("Facebook login", "User signed up and logged in through Facebook!");
                    getDetailsBackground();
                    launchBeerList();
                } else {
                    Log.d("Facebook login", "User logged in through Facebook!");
                    getDetailsBackground();
                    launchBeerList();
                }
            }
        });
    }


    private void getDetailsBackground() {
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    ParseUser.getCurrentUser().put("fbId", user.getId());
                    ParseUser.getCurrentUser().put("name", user.getName());
                    String displayName = user.getFirstName() + " " + user.getLastName().charAt(0);
                    ParseUser.getCurrentUser().put("displayName", displayName);
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

    private void onLogoutButtonClicked() {
        BeerLoginActivity.this.progressDialog = ProgressDialog.show(
                BeerLoginActivity.this, "", "Logging out...", true);
        logoutUser();
    }

    private void logoutUser() {
        // Log the user out
        ParseUser.logOut();
        // Go to the login view
        Log.i("LoginActivity", "Restarting login");
        Intent launchMainActivity = new Intent(this, MainActivity.class);
        startActivity(launchMainActivity);
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
