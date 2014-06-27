package com.rjmoseley.beerator.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class BeerNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "BeerNotificationReceiver";

    public BeerNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            String action = intent.getAction();
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.d(TAG, "got action " + action + " with:");
            Iterator itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }
            String objectId = json.getString("beerObjectId");
            Crashlytics.log(Log.INFO, TAG, "Beer in notification is " + objectId);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}
