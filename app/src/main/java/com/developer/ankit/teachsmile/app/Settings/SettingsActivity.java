package com.developer.ankit.teachsmile.app.Settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    public interface UpdateEmotionPreferenceCallback {
        void emotionUpdated();
    }

    private UpdateEmotionPreferenceCallback callback;

    public static Intent startSettingsActivity(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }


}
