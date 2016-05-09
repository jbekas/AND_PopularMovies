package com.redgeckotech.popularmovies.dagger;

import android.content.Context;

import com.redgeckotech.popularmovies.MainActivity;
import com.redgeckotech.popularmovies.MovieDetailActivity;
import com.redgeckotech.popularmovies.MovieDetailFragment;
import com.redgeckotech.popularmovies.MovieListFragment;
import com.redgeckotech.popularmovies.MoviesApplication;

import javax.inject.Singleton;

import dagger.Component;

@Singleton // Constraints this component to one-per-application or unscoped bindings.
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MoviesApplication application);

    void inject(MainActivity mainActivity);
    void inject(MovieDetailActivity mainActivity);

    void inject(MovieListFragment fragment);
    void inject(MovieDetailFragment fragment);

    //Exposed to sub-graphs.
    Context context();
}