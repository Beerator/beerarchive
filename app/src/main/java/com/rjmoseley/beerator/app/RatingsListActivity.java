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

public class RatingsListActivity extends Activity {

    private RatingsListAdapter ratingsListAdapter;
    private ArrayList<Beer> ratingsListBeerList;
    private static final String TAG = "RatingsList";
    final Globals g = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_ratings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ratingsListBeerList = new ArrayList<Beer>();
        ratingsListAdapter = new RatingsListAdapter(RatingsListActivity.this,
                R.layout.ratings_list_item, ratingsListBeerList);
        ratingsListAdapter.notifyDataSetChanged();
        ListView recentRatingsListView = (ListView)findViewById(R.id.ratingsListListView);
        recentRatingsListView.setAdapter(ratingsListAdapter);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beerRating");
        query.addDescendingOrder("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (final ParseObject ratingObj : parseObjects) {
                        String beerObjectId = ratingObj.getString("beerObjectId");
                        Boolean beerFound = false;
                        for (Beer b : g.getBeerList()) {
                            if (b.getObjectId().equals(beerObjectId)) {
                                Crashlytics.log(Log.INFO, TAG, "Beer found locally: " + b.toString());
                                beerFound = true;
                                b.clearRatings();
                                BeerRating beerRating = new BeerRating(ratingObj.getString("normRating"),
                                        ratingObj.getCreatedAt());
                                beerRating.setUserName(ratingObj.getString("userDisplayName"));
                                b.addRating(beerRating);
                                ratingsListBeerList.add(b);
                                sortBeerRatingsByDate();
                                ratingsListAdapter.notifyDataSetChanged();
                            }
                        }
                        if (!beerFound) {
                            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beer");
                            query.getInBackground(ratingObj.getString("beerObjectId"), new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject beerObj, ParseException e) {
                                    if (e == null) {
                                        Beer beer = new Beer(beerObj.getString("beerName"),
                                                beerObj.getString("brewery"), beerObj.getObjectId());
                                        Crashlytics.log(Log.INFO, TAG, "Beer downloaded: " + beer.toString());
                                        BeerRating beerRating = new BeerRating(ratingObj.getString("normRating"),
                                                ratingObj.getCreatedAt());
                                        beerRating.setUserName(ratingObj.getString("userDisplayName"));
                                        beer.addRating(beerRating);
                                        ratingsListBeerList.add(beer);
                                        sortBeerRatingsByDate();
                                        ratingsListAdapter.notifyDataSetChanged();
                                    } else {
                                        Crashlytics.log(Log.INFO, TAG, "Failed to find beer to match recent rating");
                                        Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                                                + ", Message: " + e.getMessage());
                                    }
                                }
                            });
                        }
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
        Collections.sort(ratingsListBeerList, new Comparator<Beer>() {
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
        getMenuInflater().inflate(R.menu.ratings_list, menu);
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
