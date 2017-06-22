package com.developer.ankit.teachsmile.app.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.developer.ankit.teachsmile.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_pref);
    }
}
