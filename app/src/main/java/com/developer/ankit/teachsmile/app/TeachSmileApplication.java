package com.developer.ankit.teachsmile.app;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by ankit on 4/27/17.
 */

public class TeachSmileApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (com.developer.ankit.teachsmile.BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }
}
