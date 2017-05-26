package com.udacity.stockhawk.ui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.stockhawk.R;

/**
 * Created by nestorkokoafantchao on 5/10/17.
 */

public class SettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_generals);
            SharedPreferences sharedPreferences =getPreferenceScreen().getSharedPreferences();
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            int count = preferenceScreen.getPreferenceCount();

            for(int i=0; i<count;i++){
                Preference p = preferenceScreen.getPreference(i);
                if(!(p instanceof CheckBoxPreference)){
                    String stringValue = sharedPreferences.getString(p.getKey(), "");
                    setPreferencsSummary(p,stringValue);
                }
            }

        }

        @Override
        public void onStart() {
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            super.onStart();
        }

        @Override
        public void onStop() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onStop();
        }

        public void setPreferencsSummary(Preference preference ,String value){

            if (preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int indexOfValue = listPreference.findIndexOfValue(value);
                if(indexOfValue>=0){
                    listPreference.setSummary(listPreference.getEntries()[indexOfValue]);
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference( key);
            if(preference != null){
                if(!(preference instanceof CheckBoxPreference)){
                    String stringValue = sharedPreferences.getString(preference.getKey(),"");
                    setPreferencsSummary(preference,stringValue);
                }
            }





        }
    }
}
