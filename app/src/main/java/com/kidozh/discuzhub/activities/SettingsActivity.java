package com.kidozh.discuzhub.activities;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.kidozh.discuzhub.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            bindPreferenceListener();
        }

        void bindPreferenceListener(){
            // night mode preference
            findPreference(getString(R.string.preference_key_display_mode)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    switch (stringValue){
                        case "MODE_NIGHT_NO":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            return true;
                        case "MODE_NIGHT_YES":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            return true;
                        case "MODE_NIGHT_FOLLOW_SYSTEM":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            return true;
                        case "MODE_NIGHT_AUTO_BATTERY":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                            return true;
                    }
                    return false;
                }
            });

            findPreference(getString(R.string.preference_key_use_browser_client)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    switch (stringValue){
                        case "NONE":{
                            preference.setSummary(R.string.preference_summary_use_browser_client_NONE);
                            return true;
                        }
                        case "ANDROID":{
                            String useragent = new WebView(getContext()).getSettings().getUserAgentString();
                            preference.setSummary(getString(R.string.preference_summary_use_browser_client_ANDROID)
                                    +"\n"+String.format("\"%s\"",useragent));
                            return true;
                        }
                        default:
                            preference.setSummary("%s");
                            return true;
                    }
                }
            });

        }


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}