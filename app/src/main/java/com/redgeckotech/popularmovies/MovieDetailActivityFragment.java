package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.redgeckotech.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    @Inject Picasso mPicasso;

    private Movie mMovie;

    public MovieDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(Constants.EXTRA_MOVIE);
        } else {
            Intent intent = getActivity().getIntent();
            mMovie = intent.getParcelableExtra(Constants.EXTRA_MOVIE);
        }

        Timber.d("movie: %s", mMovie);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);

        movieTitle.setText(mMovie.getTitle());

        return rootView;
    }
}
