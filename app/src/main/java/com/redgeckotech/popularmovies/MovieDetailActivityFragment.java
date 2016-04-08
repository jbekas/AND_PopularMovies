package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
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

    private ScrollView mScrollView;
    private ImageView mMoviePoster;
    private TextView mMovieTitle;
    private TextView mOverview;
    private TextView mYear;
    private TextView mVoteAverage;

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

        mScrollView = (ScrollView) rootView.findViewById(R.id.movie_detail_scrollview);
        mMoviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        mMovieTitle = (TextView) rootView.findViewById(R.id.movie_title);
        mOverview = (TextView) rootView.findViewById(R.id.overview);
        mYear = (TextView) rootView.findViewById(R.id.year);
        mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average);

        updateUI();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Constants.EXTRA_MOVIE, mMovie);
    }

    public void updateUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMovie == null) {
                        mScrollView.setVisibility(View.INVISIBLE);
                    } else {
                        mScrollView.setVisibility(View.VISIBLE);
                        mPicasso.load(String.format("http://image.tmdb.org/t/p/%s/%s", mPosterSize, mMovie.getPosterPath())).into(mMoviePoster);

                        mMovieTitle.setText(mMovie.getTitle());
                        mOverview.setText(mMovie.getOverview());

                        String releaseYear = mMovie.getReleaseYear();
                        mYear.setText(releaseYear);
                        mYear.setVisibility(releaseYear == null ? View.GONE : View.VISIBLE);

                        // Limit decimals to 2, but strip if trailing decimals are 0
                        BigDecimal bd = new BigDecimal(mMovie.getVoteAverage()).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                        mVoteAverage.setText(getString(R.string.vote_average, bd.toString()));

                        mMovieTitle.setSelected(true);
                    }
                }
            });
        }
    }
}
