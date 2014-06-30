package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BeerDetailsActivity extends Activity {

    private String beerObjectId;

    private Beer beer;

    private ListView ratingsListView;

    private BeerRatingsAdapter beerRatingsAdapter;

    private ArrayList<BeerRating> beerRatings = new ArrayList<BeerRating>();

    private String ratingSystem = "1-5+";

    private static final String TAG = "BeerDetails";

    public final static String AUTH_ACTION = "com.rjmoseley.beerator.app.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_details);
        Crashlytics.log(Log.INFO, TAG, "Created");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Crashlytics.log(Log.INFO, TAG, "Resumed");

        ratingsListView = (ListView) findViewById(R.id.ratingsListView);
        View headerView = View.inflate(this, R.layout.activity_beer_details_header, null);
        ratingsListView.addHeaderView(headerView);

        ratingsListView.setAdapter(beerRatingsAdapter);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        findViewById(R.id.ratingsListView).setVisibility(View.VISIBLE);
        findViewById(R.id.loadAllRatings).setVisibility(View.GONE);
        findViewById(R.id.loadMyRatings).setVisibility(View.GONE);

        final TextView breweryName = (TextView) findViewById(R.id.breweryName);
        final TextView beerName = (TextView) findViewById(R.id.beerName);
        final TextView abv = (TextView) findViewById(R.id.abv);
        final TextView country = (TextView) findViewById(R.id.country);

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        beerObjectId = objectId;

        Crashlytics.log(Log.INFO, TAG, "Finding beer details for " + objectId);
        Globals g = Globals.getInstance();
        ArrayList<Beer> beerList = g.getBeerList();
        if (beerList.isEmpty()) {
            Crashlytics.log(Log.INFO, TAG, "beerList is empty");
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beer");
            try {
                ParseObject b = query.get(objectId);
                beer = new Beer(b.getString("beerName"), b.getString("brewery"), b.getObjectId());
                if (b.getString("abv") != null) {
                    beer.setABV(b.getString("abv"));
                }
                Crashlytics.log(Log.INFO, TAG, "Beer details downloaded for beer " + beer.toString());
            } catch (ParseException e) {
                Toast.makeText(this, "Failed to find beer details", Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.INFO, TAG, "Failed to download beer details, exiting BeerDetails");
                Crashlytics.log(Log.INFO, TAG, e.getMessage());
                Crashlytics.logException(e);
                e.printStackTrace();
                finish();
            }
        } else {
            for (Beer b : beerList) {
                if (b.getObjectId().equals(objectId)) {
                    beer = b;
                    Crashlytics.log(Log.INFO, TAG, "Beer details found from beerList for " + beer.toString());
                }
            }
        }

        //Setting TextView items to the beer values
        beerName.setText(beer.getName());
        breweryName.setText(beer.getBrewery());
        if (beer.getABV() != null) {
            abv.setText(beer.getABV() + " %");
        }
        if (beer.getCountry() != null) {
            country.setText(beer.getCountry().toString());
        }

        //Download beer ratings in background
        Crashlytics.log(Log.INFO, TAG, "Downloading beer ratings");
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beerRating");
        query.whereEqualTo("beerObjectId", objectId);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    beer.clearRatings();
                    for (ParseObject obj : objects) {
                        BeerRating br = new BeerRating(obj.getString("normRating"),
                                obj.getCreatedAt(),
                                obj.getObjectId(),
                                obj.getString("userObjectId"),
                                obj.getString("userDisplayName"),
                                obj.getParseGeoPoint("location"));
                        beer.addRating(br);
                        if (obj.getString("userObjectId").equals(ParseUser.getCurrentUser().getObjectId())) {
                            beer.addMyRating(br);
                        }
                    }
                    Crashlytics.log(Log.INFO, TAG, "Beer ratings downloaded: " + objects.size());

                    //Identify if there are ratings in the all ratings list
                    if (beer.getRatingsList().isEmpty()) {
                        Crashlytics.log(Log.INFO, TAG, "All ratings: none downloaded");
                    } else {
                        Crashlytics.log(Log.INFO, TAG, "All ratings: " + beer.getRatingsList().size() + " beers downloaded");
                        Button allRatingsButton = (Button) findViewById(R.id.loadAllRatings);
                        String allText = "All ratings (" + beer.getRatingsList().size() + ")";
                        allRatingsButton.setText(allText);
                        findViewById(R.id.loadAllRatings).setVisibility(View.VISIBLE);
                        //If there are ratings, are there some of my ratings?
                        if (beer.getMyRatingsList().isEmpty()) {
                            Crashlytics.log(Log.INFO, TAG, "My ratings: none downloaded");
                        } else {
                            Crashlytics.log(Log.INFO, TAG, "My ratings: " + beer.getMyRatingsList().size() + " beers downloaded");
                            Button myRatingsButton = (Button) findViewById(R.id.loadMyRatings);
                            String myText = "My ratings (" + beer.getMyRatingsList().size() + ")";
                            myRatingsButton.setText(myText);
                            findViewById(R.id.loadMyRatings).setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Toast.makeText(BeerDetailsActivity.this, "Failed to download beer ratings",
                            Toast.LENGTH_SHORT).show();
                    Crashlytics.log(Log.INFO, TAG, "Beer ratings download failed");
                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                            + ", Message: " + e.getMessage());
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        });

        //Setup the ratings pickers
        NumberPicker np1 = (NumberPicker) findViewById(R.id.numberPicker1);
        String[] np1Strings = {"5", "4", "3", "2", "1"};
        np1.setDisplayedValues(np1Strings);
        np1.setMaxValue(4);
        np1.setMinValue(0);
        np1.setValue(2);
        np1.setWrapSelectorWheel(false);

        NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        String[] np2Strings = {"+", " ", "-"};
        np2.setDisplayedValues(np2Strings);
        np2.setMinValue(0);
        np2.setMaxValue(2);
        np2.setValue(1);
        np2.setWrapSelectorWheel(false);
    }

    public void rateBeerOnClick(View view) {
        rateBeer();
    }

    public void rateBeer() {
        Crashlytics.log(Log.INFO, TAG, "Adding beer rating");
        final SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        findViewById(R.id.ratingLayout).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        NumberPicker np1 = (NumberPicker) findViewById(R.id.numberPicker1);
        String[] np1Strings = np1.getDisplayedValues();
        final String ratingElement1 = np1Strings[np1.getValue()];

        NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        String[] np2Strings = np2.getDisplayedValues();
        final String ratingElement2 = np2Strings[np2.getValue()];

        final BeerRating tempBR = new BeerRating(ratingElement1+ratingElement2, ratingSystem, new Date());

        final String normRating = tempBR.getNormRating();
        Crashlytics.log(Log.INFO, TAG, "Normalised rating = " + normRating);

        final ParseObject parseRating = new ParseObject("beerRating");

        parseRating.put("beerObjectId", beerObjectId);
        parseRating.put("normRating", normRating);
        parseRating.put("ratingSystem", ratingSystem);
        parseRating.put("userDisplayName",ParseUser.getCurrentUser().getString("displayName"));
        parseRating.put("userObjectId", ParseUser.getCurrentUser().getObjectId());


        //Sort out the ACL
        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setPublicReadAccess(sharedPrefs.getBoolean("public_ratings", true));
        Crashlytics.log(Log.INFO, TAG, "Public rating: " + sharedPrefs.getBoolean("public_ratings", true));
        parseRating.setACL(acl);

        if (sharedPrefs.getBoolean("geotag", true)) {
            Crashlytics.log(Log.INFO, TAG, "Attempting to GeoTag");
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setCostAllowed(false);
            ParseGeoPoint.getCurrentLocationInBackground(5000, criteria, new LocationCallback() {
                @Override
                public void done(ParseGeoPoint parseGeoPoint, ParseException e) {
                    if (e == null) {
                        parseRating.put("location", parseGeoPoint);
                        parseRating.saveInBackground();
                        Crashlytics.log(Log.INFO, TAG, "Saving geoPoint");
                    } else {
                        Crashlytics.log(Log.INFO, TAG, "Unable to get location");
                        Toast.makeText(BeerDetailsActivity.this, "Unable to get location",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        Crashlytics.log(Log.INFO, TAG, "Adding rating of " + normRating + " and rating system " + ratingSystem
                + " for beer with objectId " + beerObjectId);

        parseRating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Crashlytics.log(Log.INFO, TAG, "Beer rating saved to Parse successfully");
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    Date date = parseRating.getCreatedAt();
                    BeerRating beerRating = new BeerRating(normRating, date);
                    beerRating.setObjectId(parseRating.getObjectId());
                    try {
                        beerRating.setLocation(parseRating.getParseGeoPoint("location"));
                    } catch (NullPointerException e2) {
                        Crashlytics.log(Log.INFO, TAG, "No geoPoint to add to rating at present");
                        Crashlytics.log(Log.INFO, TAG, e2.getMessage());
                    }
                    beerRating.setUserObjectId(ParseUser.getCurrentUser().getObjectId());
                    beerRating.setUserName(ParseUser.getCurrentUser().getString("displayName"));
                    beer.addRating(beerRating);
                    beer.addMyRating(beerRating);
                    beer.sortRatings();
                    //Sort out the button text
                    Button allRatingsButton = (Button) findViewById(R.id.loadAllRatings);
                    Button myRatingsButton = (Button) findViewById(R.id.loadMyRatings);
                    String allText = "All ratings (" + beer.getRatingsList().size() + ")";
                    String myText = "My ratings (" + beer.getMyRatingsList().size() + ")";
                    allRatingsButton.setText(allText);
                    myRatingsButton.setText(myText);
                    if (beerRatingsAdapter != null) {
                        //beerRatings are already on display so add to them
                        beerRatingsAdapter.notifyDataSetChanged();
                    } else {
                        //beerRatings are not currently displayed so display the buttons
                        if (allRatingsButton.getVisibility() == View.GONE) {
                            allRatingsButton.setVisibility(View.VISIBLE);
                        }
                        if (myRatingsButton.getVisibility() == View.GONE) {
                            myRatingsButton.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Toast.makeText(BeerDetailsActivity.this, "Failed to save rating",
                            Toast.LENGTH_SHORT).show();
                    Crashlytics.log(Log.INFO, TAG, "Beer ratings save failed");
                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                            + ", Message: " + e.getMessage());
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }
        });

        //Do push notification if it's enabled
        if (sharedPrefs.getBoolean("push_send_enabled", true)) {
            Crashlytics.log(Log.INFO, TAG, "Push notification sending is enabled, getting friend list");
            Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
                @Override
                public void onCompleted(List<GraphUser> users, Response response) {
                    //Returned list of friends with Beerator
                    if (users != null) {
                        List<String> friendsList = new ArrayList<String>();
                        for (GraphUser user : users) {
                            friendsList.add(user.getId());
                        }
                        Crashlytics.log(Log.INFO, TAG, friendsList.size() + " Beerator friends found");

                        //New query of Installations to find those to send push to
                        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
                        query.whereContainedIn("fbId", friendsList);
                        query.whereNotEqualTo("pushEnabled", false);
                        ParsePush push = new ParsePush();
                        push.setQuery(query);
                        String message = ParseUser.getCurrentUser().getString("displayName") + " rated "
                                + beer.getBrewery() + " " + beer.getName() + " at "
                                + tempBR.getRating(ratingSystem) + "!";
                        JSONObject data = new JSONObject();
                        try {
                            data.put("alert", message);
                            data.put("beerObjectId", beer.getObjectId());
                            data.put("action", "com.rjmoseley.beerator.app.BEER_NOTIFICATION");
                        } catch (JSONException e) {
                            Crashlytics.log(Log.INFO, TAG, "JSON Exception");
                            Crashlytics.log(Log.INFO, TAG, e.getMessage());
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                        push.setData(data);
                        push.sendInBackground(new SendCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Crashlytics.log(Log.INFO, TAG, "Notification sent successfully");
                                } else {
                                    Crashlytics.log(Log.INFO, TAG, "Failed to send notification");
                                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                                            + ", Message: " + e.getMessage());
                                    Crashlytics.logException(e);
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else if (response.getError() != null) {
                        Crashlytics.log(Log.INFO, TAG, "Facebook error: " + response.getError().getErrorCode()
                                + ", " + response.getError().getErrorMessage());
                    }
                }
            }).executeAsync();
        } else {
            Crashlytics.log(Log.INFO, TAG, "Push notification sending is disabled, not sending notification");
        }
    }

    public void loadAllRatingsOnClick(View view) {
        loadAllRatings();
    }

    public void loadAllRatings() {
        Crashlytics.log(Log.INFO, TAG, "Displaying all ratings");

        beerRatings = beer.getRatingsList();

        beerRatingsAdapter = new BeerRatingsAdapter(this, R.layout.beer_rating_item, beerRatings);

        beerRatingsAdapter.notifyDataSetChanged();

        ratingsListView.setAdapter(beerRatingsAdapter);

        Crashlytics.log(Log.INFO, TAG, "All beerRatings size: " + beerRatings.size());

        findViewById(R.id.ratingsListView).setVisibility(View.VISIBLE);
        findViewById(R.id.loadAllRatings).setVisibility(View.GONE);
        findViewById(R.id.loadMyRatings).setVisibility(View.GONE);
    }

    public void loadMyRatingsOnClick(View view) {
        loadMyRatings();
    }

    public void loadMyRatings() {
        Crashlytics.log(Log.INFO, TAG, "Displaying my ratings");

        beerRatings = beer.getMyRatingsList();

        beerRatingsAdapter = new BeerRatingsAdapter(this, R.layout.beer_rating_item, beerRatings);

        beerRatingsAdapter.notifyDataSetChanged();

        ratingsListView.setAdapter(beerRatingsAdapter);

        Crashlytics.log(Log.INFO, TAG, "My beerRatings size: " + beerRatings.size());

        findViewById(R.id.ratingsListView).setVisibility(View.VISIBLE);
        findViewById(R.id.loadMyRatings).setVisibility(View.GONE);
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
            Crashlytics.log(Log.INFO, TAG, "Settings selected from menu");
            Intent i = new Intent(this, SettingActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_logout) {
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
