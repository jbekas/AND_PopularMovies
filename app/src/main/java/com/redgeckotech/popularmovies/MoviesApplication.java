package com.redgeckotech.popularmovies;

import android.app.Application;

import com.redgeckotech.popularmovies.dagger.AppComponent;
import com.redgeckotech.popularmovies.dagger.AppModule;
import com.redgeckotech.popularmovies.dagger.DaggerAppComponent;

import timber.log.Timber;

public class MoviesApplication extends Application {

    private AppComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeInjector();
        getApplicationComponent().inject(this);

        Timber.plant(new Timber.DebugTree());
    }

    private void initializeInjector() {
        this.applicationComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getApplicationComponent() {
        return this.applicationComponent;
    }
}
