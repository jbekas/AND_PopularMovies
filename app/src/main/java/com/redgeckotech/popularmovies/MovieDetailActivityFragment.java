package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.redgeckotech.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    @Inject Picasso mPicasso;

    private Movie mMovie;
    private final String mPosterSize = "w185";

    public MovieDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency injection
        ((MoviesApplication) getActivity().getApplicationContext()).getApplicationComponent().inject(this);

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

        ImageView moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
        TextView overview = (TextView) rootView.findViewById(R.id.overview);
        TextView year = (TextView) rootView.findViewById(R.id.year);
        TextView voteAverage = (TextView) rootView.findViewById(R.id.vote_average);

        mPicasso.load(String.format("http://image.tmdb.org/t/p/%s/%s", mPosterSize, mMovie.getPosterPath())).into(moviePoster);

        movieTitle.setText(mMovie.getTitle());
        movieTitle.setSelected(true);
        overview.setText(mMovie.getOverview());

        String releaseYear = mMovie.getReleaseYear();
        year.setText(releaseYear);
        year.setVisibility(releaseYear == null ? View.GONE : View.VISIBLE);

        // Limit decimals to 2, but strip if trailing decimals are 0
        BigDecimal bd = new BigDecimal(mMovie.getVoteAverage()).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
        voteAverage.setText(getString(R.string.vote_average, bd.toString()));

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Constants.EXTRA_MOVIE, mMovie);
    }
}
