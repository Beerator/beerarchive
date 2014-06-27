package com.rjmoseley.beerator.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseInstallation;

public class SettingActivity extends PreferenceActivity  implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Crashlytics.log(Log.INFO, TAG, "Created");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        Crashlytics.log(Log.INFO, TAG, "Resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        Crashlytics.log(Log.INFO, TAG, "Paused");
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if(key.equals("push_receive_enabled")) {
            Boolean pushEnabled = prefs.getBoolean("push_receive_enabled", true);
            Crashlytics.log(Log.INFO, TAG, "PushEnabled set to " + pushEnabled.toString());
            ParseInstallation.getCurrentInstallation().put("pushEnabled", pushEnabled);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
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
