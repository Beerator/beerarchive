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
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.Parse;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BeerDetailsActivity extends Activity {

    private ArrayList<Beer> beerList = new ArrayList<Beer>();

    private String beerObjectId;

    private Beer beer;

    private ListView ratingsListView;

    private BeerRatingsAdapter beerRatingsAdapter;

    private ArrayList<BeerRating> beerRatings = new ArrayList<BeerRating>();

    private String ratingSystem = "1-5+";

    private LocationManager locationManager;
    private Criteria criteria;
    private String locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_details);

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

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");
        beerObjectId = objectId;
        Log.i("Beer details", "objectId: " + objectId);

        Globals g = Globals.getInstance();
        beerList = g.getBeerList();
        for (Beer b : beerList) {
            if (b.getObjectId().equals(objectId)) {
                Log.i("Beer details", "Beer details found " + b.toString());
                beer = b;
                beerName.setText(beer.getName());
                breweryName.setText(beer.getBrewery());
                if (beer.getABV() != null) {
                    abv.setText(beer.getABV() + " %");
                }

                //Download beer ratings in background
                final ParseQuery query = new ParseQuery("beerRating");
                query.whereEqualTo("beerObjectId", objectId);
                query.orderByDescending("createdAt").findInBackground(new FindCallback<ParseObject>() {
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
                            Log.i("Beer details", "Beer ratings downloaded: " + objects.size());

                            //Identify if there are ratings in the all ratings list
                            if (beer.getRatingsList().isEmpty()) {
                                Log.i("Beer details", "All ratings: none downloaded");
                            } else {
                                Log.i("Beer details", "All ratings: " + beer.getRatingsList().size() + " beers downloaded");
                                findViewById(R.id.loadAllRatings).setVisibility(View.VISIBLE);
                                //If there are ratings, are there some of my ratings?
                                if (beer.getMyRatingsList().isEmpty()) {
                                    Log.i("Beer details", "My ratings: none downloaded");
                                } else {
                                    Log.i("Beer details", "My ratings: " + beer.getMyRatingsList().size() + " beers downloaded");
                                    findViewById(R.id.loadMyRatings).setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            Log.i("Beer details", "Beer ratings download failed");
                        }
                        //Don't display ratings automatically
                        //loadRatings();

                    }
                });
            }
        }


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

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        findViewById(R.id.ratingLayout).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        NumberPicker np1 = (NumberPicker) findViewById(R.id.numberPicker1);
        String[] np1Strings = np1.getDisplayedValues();
        final String ratingElement1 = np1Strings[np1.getValue()];

        NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        String[] np2Strings = np2.getDisplayedValues();
        final String ratingElement2 = np2Strings[np2.getValue()];

        final ParseGeoPoint geoPoint = new ParseGeoPoint();

        if (sharedPrefs.getBoolean("geotag", true)) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setCostAllowed(false);

            locationProvider = locationManager.getBestProvider(criteria, true);
            Log.i("Location", "Location provider chosen: "+ locationProvider);

            if (locationProvider != null) {
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) {
                    Log.i("Location", "Location: " + location.toString());
                    geoPoint.setLatitude(location.getLatitude());
                    geoPoint.setLongitude(location.getLongitude());
                } else {
                    Toast.makeText(this, "Unable to get location.  Check your device settings",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Unable to get location provider.  Check your device settings",
                        Toast.LENGTH_LONG).show();
            }
        }

        final BeerRating tempBR = new BeerRating(ratingElement1+ratingElement2, ratingSystem, new Date());

        final String normRating = tempBR.getNormRating();

        final ParseObject parseRating = new ParseObject("beerRating");

        parseRating.put("beerObjectId", beerObjectId);
        parseRating.put("normRating", normRating);
        parseRating.put("ratingSystem", ratingSystem);
        parseRating.put("userDisplayName",ParseUser.getCurrentUser().getString("displayName"));
        parseRating.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
        parseRating.put("location", geoPoint);

        //Sort out the ACL
        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setPublicReadAccess(sharedPrefs.getBoolean("public_ratings", true));
        parseRating.setACL(acl);

        Log.i("Beer rating", "Adding rating of " + normRating + " and rating system " + ratingSystem
                + " for beer with objectId " + beerObjectId);

        parseRating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                Date date = parseRating.getCreatedAt();
                BeerRating beerRating = new BeerRating(normRating, date);
                beerRating.setObjectId(parseRating.getObjectId());
                beerRating.setLocation(geoPoint);
                beerRating.setUserObjectId(ParseUser.getCurrentUser().getObjectId());
                beerRating.setUserName(ParseUser.getCurrentUser().getString("displayName"));
                beer.addRating(beerRating);
                beer.addMyRating(beerRating);
                beer.sortRatings();
                if (beerRatingsAdapter != null) {
                    beerRatingsAdapter.notifyDataSetChanged();
                }
            }
        });

        //Do push notification
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                if (users != null) {
                    List<String> friendsList = new ArrayList<String>();
                    for (GraphUser user : users) {
                        friendsList.add(user.getId());
                    }
                    Log.i("BeerRatingAdd", friendsList.size() + " friends found to send push notification to");

                    // Construct a ParseUser query that will find friends whose
                    // facebook IDs are contained in the current user's friend list.
                    ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
                    query.whereContainedIn("fbId", friendsList);

                    ParsePush push = new ParsePush();
                    push.setQuery(query);
                    String message = ParseUser.getCurrentUser().getString("displayName") + " rated "
                            + beer.getBrewery() + " " + beer.getName() + " at "
                            + tempBR.getRating(ratingSystem) + "!";
                    push.setMessage(message);
                    push.sendInBackground();
                }
            }
        }).executeAsync();

    }

    public void loadAllRatingsOnClick(View view) {
        loadAllRatings();
    }

    public void loadAllRatings() {
        Log.i("Beer details", "Displaying all ratings");

        beerRatings = beer.getRatingsList();

        beerRatingsAdapter = new BeerRatingsAdapter(this, R.layout.beer_rating_item, beerRatings);

        beerRatingsAdapter.notifyDataSetChanged();

        ratingsListView.setAdapter(beerRatingsAdapter);

        Log.i("Beer details", "All beerRatings size: " + beerRatings.size());

        findViewById(R.id.ratingsListView).setVisibility(View.VISIBLE);
        findViewById(R.id.loadAllRatings).setVisibility(View.GONE);
        findViewById(R.id.loadMyRatings).setVisibility(View.GONE);
    }

    public void loadMyRatingsOnClick(View view) {
        loadMyRatings();
    }

    public void loadMyRatings() {
        Log.i("Beer details", "Displaying my ratings");

        beerRatings = beer.getMyRatingsList();

        beerRatingsAdapter = new BeerRatingsAdapter(this, R.layout.beer_rating_item, beerRatings);

        beerRatingsAdapter.notifyDataSetChanged();

        ratingsListView.setAdapter(beerRatingsAdapter);

        Log.i("Beer details", "My beerRatings size: " + beerRatings.size());

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
