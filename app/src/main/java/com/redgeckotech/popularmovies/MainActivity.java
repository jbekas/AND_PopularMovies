package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.redgeckotech.popularmovies.model.Movie;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements MovieListFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency injection
        ((MoviesApplication) getApplicationContext()).getApplicationComponent().inject(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FragmentManager fm = getSupportFragmentManager();
//        final MovieListFragment movieListFragment = (MovieListFragment) fm.findFragmentById(R.id.movie_list_fragment);

//        Spinner spinner = (Spinner) toolbar.findViewById(R.id.spinner);

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String value = prefs.getString(Constants.SELECTED_VIEW_PREF, Constants.VIEW_TYPE.MOST_POPULAR.toString());
//        Constants.VIEW_TYPE viewType = Constants.VIEW_TYPE.valueOf(value);
//
//        switch (viewType) {
//            case MOST_POPULAR:
//                spinner.setSelection(0);
//                break;
//            case HIGHEST_RATED:
//                spinner.setSelection(1);
//                break;
//            case FAVORITES:
//                spinner.setSelection(2);
//                break;
//        }
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Timber.d("on item selected");
//                switch (position) {
//                    case 0:
//                        movieListFragment.changeSelection(Constants.VIEW_TYPE.MOST_POPULAR);
//                        break;
//                    case 1:
//                        movieListFragment.changeSelection(Constants.VIEW_TYPE.HIGHEST_RATED);
//                        break;
//                    case 2:
//                        movieListFragment.changeSelection(Constants.VIEW_TYPE.FAVORITES);
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Movie movie) {
        Timber.d("onListFragmentInteraction: %s", movie);

        // for phones, start new activity
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(Constants.EXTRA_MOVIE, movie);
        startActivity(intent);
    }
}
