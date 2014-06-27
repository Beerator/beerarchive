package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class BeerLoginActivity extends Activity {

    private Dialog progressDialog;

    private static final String TAG = "BeerLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Crashlytics.log(Log.INFO, TAG, "Created");

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutButtonClicked();
            }
        });

        Button launchAppButton = (Button) findViewById(R.id.launchApp);
        launchAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBeerList();
            }
        });

        updateCurrentStatus();

        Intent intent = getIntent();
        String message = intent.getStringExtra(BeerListActivity.AUTH_ACTION);
        if (message != null) {
            if (message.equals("logout")) {
                Crashlytics.log(Log.INFO, TAG, "Logout requested via Intent");
                onLogoutButtonClicked();
            }
        }

        // Check if there is a currently logged in user
        // and they are linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            Crashlytics.log(Log.INFO, TAG, "User is already logged in");
            //Below line needed if more data needs to be added to ParseUser from GraphUser
            getDetailsBackground();
            launchBeerList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.INFO, TAG, "Resumed");
        updateCurrentStatus();
    }

    private void updateCurrentStatus() {
        TextView currentStatus = (TextView) findViewById(R.id.currentStatus);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Crashlytics.log(Log.INFO, TAG, "User is currently logged in");
            currentStatus.setText(R.string.logged_in);
            findViewById(R.id.loginButton).setVisibility(View.GONE);
            findViewById(R.id.logoutButtonLayout).setVisibility(View.VISIBLE);
        } else {
            Crashlytics.log(Log.INFO, TAG, "User is currently logged out");
            currentStatus.setText(R.string.logged_out);
            findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
            findViewById(R.id.logoutButtonLayout).setVisibility(View.GONE);
        }
    }

    private void onLoginButtonClicked() {
        Crashlytics.log(Log.INFO, TAG, "Login button pressed");
        BeerLoginActivity.this.progressDialog = ProgressDialog.show(
                BeerLoginActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("public_profile", "user_friends");

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                BeerLoginActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Crashlytics.log(Log.INFO, TAG, "The user cancelled the Facebook login");
                } else if (user.isNew()) {
                    Crashlytics.log(Log.INFO, TAG, "User signed up and logged in through Facebook!");
                    getDetailsBackground();
                    launchBeerList();
                } else {
                    Crashlytics.log(Log.INFO, TAG, "User logged in through Facebook!");
                    getDetailsBackground();
                    launchBeerList();
                }
                if (e != null) {
                    Crashlytics.log(Log.INFO, TAG, "Exception was raised during login");
                    Crashlytics.log(Log.INFO, TAG, e.getMessage());
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void getDetailsBackground() {
        Crashlytics.log(Log.INFO, TAG, "Attempting to set up Installation, User and Crashlytics details");
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    Crashlytics.log(Log.INFO, TAG, "Facebook query successful");
                    //Check push notification preference
                    SharedPreferences sharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(BeerLoginActivity.this);
                    Boolean pushEnabled = sharedPrefs.getBoolean("push_receive_enabled", true);

                    //Setting up User details
                    ParseUser.getCurrentUser().put("fbId", user.getId());
                    ParseUser.getCurrentUser().put("name", user.getName());
                    String displayName = user.getFirstName() + " " + user.getLastName().charAt(0);
                    ParseUser.getCurrentUser().put("displayName", displayName);
                    ParseUser.getCurrentUser().saveInBackground();

                    //Setting up installation details
                    ParseInstallation.getCurrentInstallation().put("fbId", user.getId());
                    ParseInstallation.getCurrentInstallation().put("name", user.getName());
                    ParseInstallation.getCurrentInstallation().put("userObjectId",
                            ParseUser.getCurrentUser().getObjectId());
                    ParseInstallation.getCurrentInstallation().put("pushEnabled", pushEnabled);
                    ParseInstallation.getCurrentInstallation().saveInBackground();

                    //Setting up crashlytics details
                    Crashlytics.log(Log.INFO, TAG, "Adding identifiers for Crashlytics");
                    Crashlytics.setUserIdentifier(ParseInstallation.getCurrentInstallation()
                            .getObjectId());
                    Crashlytics.setUserName(user.getName());

                } else if (response.getError() != null) {
                    Crashlytics.log(Log.INFO, TAG, "Facebook query failed, " +
                            "unable to setup User, Installation and Crashlytics details");
                    if ((response.getError().getCategory() ==
                            FacebookRequestError.Category.AUTHENTICATION_RETRY) ||
                            (response.getError().getCategory() ==
                                    FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                        Crashlytics.log(Log.INFO, TAG, "The facebook session was invalidated.");
                        logoutUser();
                    } else {
                        Crashlytics.log(Log.INFO, TAG, "Some other error: "
                                + response.getError().getErrorMessage());
                    }
                }
            }
        }).executeAsync();
    }

    public void launchBeerList() {
        Crashlytics.log(Log.INFO, TAG, "Launching beer list");
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
        Crashlytics.log(Log.INFO, TAG, "Logging user out");
        ParseFacebookUtils.getSession().closeAndClearTokenInformation();
        ParseUser.logOut();
        // Go to the login view
        Crashlytics.log(Log.INFO, TAG, "Returning to MainActivity");
        Intent launchMainActivity = new Intent(this, MainActivity.class);
        startActivity(launchMainActivity);
    }

    @Override
    public void onBackPressed(){
        Crashlytics.log(Log.INFO, TAG, "Back button pressed, launching home screen");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
