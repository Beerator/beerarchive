package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
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
        setContentView(R.layout.activity_ratings_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ratingsListBeerList = new ArrayList<Beer>();
        ratingsListAdapter = new RatingsListAdapter(RatingsListActivity.this,
                R.layout.ratings_list_item, ratingsListBeerList);
        ratingsListAdapter.notifyDataSetChanged();
        ListView recentRatingsListView = (ListView) findViewById(R.id.ratingsListListView);
        recentRatingsListView.setAdapter(ratingsListAdapter);
    }

    public void showRecentRatingsOnClick(View view) {
        showRecentRatings();
    }

    private void showRecentRatings() {
        ratingsListBeerList.clear();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beerRating");
        query.addDescendingOrder("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                processRatings(parseObjects, e, "date");
            }
        });
    }

    public void showTopRatingsOnClick(View view) {
        showTopRatings();
    }

    private void showTopRatings() {
        ratingsListBeerList.clear();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beerRating");
        query.addDescendingOrder("normRating");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                processRatings(parseObjects, e, "normRating");
            }
        });
    }


    private void processRatings(List<ParseObject> parseObjects, ParseException e, final String sortKey) {
        if (e == null) {
            for (final ParseObject ratingObj : parseObjects) {
                String beerObjectId = ratingObj.getString("beerObjectId");
                Boolean beerFound = false;
                //Try and find the beer in the local beerList first
                for (Beer b : g.getBeerList()) {
                    if (b.getObjectId().equals(beerObjectId)) {
                        //Beer found locally, clear it's ratings and add it to the ratingsListBeerList
                        Crashlytics.log(Log.INFO, TAG, "Beer found locally: " + b.toString());
                        beerFound = true;
                        b.clearRatings();
                        BeerRating beerRating = new BeerRating(ratingObj.getString("normRating"),
                                ratingObj.getCreatedAt());
                        beerRating.setUserName(ratingObj.getString("userDisplayName"));
                        b.addRating(beerRating);
                        ratingsListBeerList.add(b);
                        sortBeerRatings(sortKey);
                        ratingsListAdapter.notifyDataSetChanged();
                    }
                }
                if (!beerFound) {
                    //Beer was not found locally, try and download it
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("beer");
                    query.getInBackground(ratingObj.getString("beerObjectId"), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject beerObj, ParseException e) {
                            if (e == null) {
                                //Beer was found, add it to the list
                                Beer beer = new Beer(beerObj.getString("beerName"),
                                        beerObj.getString("brewery"), beerObj.getObjectId());
                                Crashlytics.log(Log.INFO, TAG, "Beer downloaded: " + beer.toString());
                                BeerRating beerRating = new BeerRating(ratingObj.getString("normRating"),
                                        ratingObj.getCreatedAt());
                                beerRating.setUserName(ratingObj.getString("userDisplayName"));
                                beer.addRating(beerRating);
                                ratingsListBeerList.add(beer);
                                sortBeerRatings(sortKey);
                                ratingsListAdapter.notifyDataSetChanged();
                            } else {
                                //Beer was not found, probably been deleted at some point
                                Crashlytics.log(Log.INFO, TAG, "Failed to find beer "
                                        + ratingObj.getString("beerObjectId")
                                        + " to match rating "
                                        + ratingObj.getObjectId());
                                Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                                        + ", Message: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        } else {
            Crashlytics.log(Log.INFO, TAG, "Failed to download ratings");
            Crashlytics.log(Log.INFO, TAG, "Code: " + e.getCode()
                    + ", Message: " + e.getMessage());
        }
    }

    public void sortBeerRatings(String sortKey) {
        if (sortKey.equals("date")) {
            Collections.sort(ratingsListBeerList, new Comparator<Beer>() {
                @Override
                public int compare(Beer beer1, Beer beer2) {
                    return beer2.getRatingsList().get(0).getDate()
                            .compareTo(beer1.getRatingsList().get(0).getDate());
                }
            });
        } else if (sortKey.equals("normRating")) {
            Collections.sort(ratingsListBeerList, new Comparator<Beer>() {
                @Override
                public int compare(Beer beer1, Beer beer2) {
                    return beer2.getRatingsList().get(0).getNormRating()
                            .compareTo(beer1.getRatingsList().get(0).getNormRating());
                }
            });
        }
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
