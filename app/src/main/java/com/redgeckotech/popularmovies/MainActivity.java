package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.redgeckotech.popularmovies.model.Movie;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements MovieListFragment.OnListFragmentInteractionListener {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency injection
        ((MoviesApplication) getApplicationContext()).getApplicationComponent().inject(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onListFragmentInteraction(Movie movie) {
        Timber.d("onListFragmentInteraction: %s", movie);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (movie != null) {
                Bundle arguments = new Bundle();
                arguments.putParcelable(Constants.EXTRA_MOVIE, movie);

                MovieDetailFragment fragment = new MovieDetailFragment();
                fragment.setArguments(arguments);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            } else {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commit();
                }
            }
        } else {
            // for phones, start new activity

            if (movie != null) {
                Intent intent = new Intent(this, MovieDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MOVIE, movie);
                startActivity(intent);
            } else {
                Timber.w("movie is null.");
            }
        }
    }
}
