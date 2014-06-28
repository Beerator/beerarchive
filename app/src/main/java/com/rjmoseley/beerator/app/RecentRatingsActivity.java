package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecentRatingsActivity extends Activity {

    private RecentRatingsAdapter recentRatingsAdapter;
    private ArrayList<Beer> recentRatingsBeerList;
    private static final String TAG = "RecentRatings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_ratings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recentRatingsBeerList = new ArrayList<Beer>();
        recentRatingsAdapter = new RecentRatingsAdapter(RecentRatingsActivity.this,
                R.layout.recent_ratings_item, recentRatingsBeerList);
        recentRatingsAdapter.notifyDataSetChanged();
        ListView recentRatingsListView = (ListView)findViewById(R.id.recentRatingsListView);
        recentRatingsListView.setAdapter(recentRatingsAdapter);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beerRating");
        query.addDescendingOrder("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (final ParseObject ratingObj : parseObjects) {
                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beer");
                        query.getInBackground(ratingObj.getString("beerObjectId"), new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject beerObj, ParseException e) {
                                if (e == null) {
                                    Beer beer = new Beer(beerObj.getString("beerName"),
                                            beerObj.getString("brewery"), beerObj.getObjectId());
                                    BeerRating beerRating = new BeerRating(ratingObj.getString("normRating"),
                                            ratingObj.getCreatedAt());
                                    beerRating.setUserName(ratingObj.getString("userDisplayName"));
                                    beer.addRating(beerRating);
                                    recentRatingsBeerList.add(beer);
                                    sortBeerRatingsByDate();
                                    recentRatingsAdapter.notifyDataSetChanged();
                                } else {
                                    Crashlytics.log(Log.INFO, TAG, "Failed to find beer to match recent rating");
                                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                                            + ", Message: " + e.getMessage());
                                }
                            }
                        });
                    }
                } else {
                    Crashlytics.log(Log.INFO, TAG, "Failed to download recent ratings");
                    Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                            + ", Message: " + e.getMessage());
                }
            }
        });
    }

    public void sortBeerRatingsByDate() {
        Collections.sort(recentRatingsBeerList, new Comparator<Beer>() {
            @Override
            public int compare(Beer beer1, Beer beer2) {
                return beer2.getRatingsList().get(0).getDate()
                        .compareTo(beer1.getRatingsList().get(0).getDate());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recent_ratings, menu);
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
