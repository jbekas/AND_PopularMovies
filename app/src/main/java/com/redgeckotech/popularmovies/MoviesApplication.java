package com.redgeckotech.popularmovies;

import android.app.Application;

import timber.log.Timber;

public class MoviesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
