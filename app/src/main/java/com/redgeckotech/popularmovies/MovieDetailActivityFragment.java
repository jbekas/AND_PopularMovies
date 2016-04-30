package com.redgeckotech.popularmovies;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.redgeckotech.popularmovies.db.FavoriteMovieDB;
import com.redgeckotech.popularmovies.db.MovieDatabaseHelper;
import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.model.MovieReview;
import com.redgeckotech.popularmovies.model.MovieReviewResponse;
import com.redgeckotech.popularmovies.net.MovieService;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    @Inject MovieService mMovieService;
    @Inject Picasso mPicasso;

    private Movie mMovie;
    private final String mPosterSize = "w185";

    @Bind(R.id.movie_detail_scrollview) ScrollView mScrollView;
    @Bind(R.id.movie_poster) ImageView mMoviePoster;
    @Bind(R.id.movie_title) TextView mMovieTitle;
    @Bind(R.id.overview) TextView mOverview;
    @Bind(R.id.year) TextView mYear;
    @Bind(R.id.vote_average) TextView mVoteAverage;
    @Bind(R.id.favorite_button) FloatingActionButton mFavoriteButton;
    @Bind(R.id.review_layout) ViewGroup mReviewLayout;
    @Bind(R.id.review_list) ViewGroup mReviewList;

    private MovieDatabaseHelper mDbHelper;

    //private MovieReviewAdapter mMovieReviewAdapter;
    //private List<MovieReview> mMovieReviews = new ArrayList<>();

    private boolean mFavorite;

    @ColorInt private int activeColor;
    @ColorInt private int inactiveColor;

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

        mDbHelper = MovieDatabaseHelper.getInstance(getActivity());

        SQLiteDatabase db = null;
        try {
            db = mDbHelper.getReadableDatabase();
            FavoriteMovieDB favoriteMovieDB = new FavoriteMovieDB(db);

            mFavorite = favoriteMovieDB.isFavorite(mMovie.getId());

        } finally {
            MovieDatabaseHelper.close(db);
        }

        // Get color resources once
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activeColor = getResources().getColor(R.color.yellow500);
            inactiveColor = getResources().getColor(R.color.grey500);
        } else {
            activeColor = getResources().getColor(R.color.yellow500, getActivity().getTheme());
            inactiveColor = getResources().getColor(R.color.grey500, getActivity().getTheme());
        }

        Timber.d("movie: %s", mMovie);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ButterKnife.bind(this, rootView);

        updateUI();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Constants.EXTRA_MOVIE, mMovie);
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveReviews(1);
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

                        mFavoriteButton.setColorFilter(mFavorite ? activeColor : inactiveColor, PorterDuff.Mode.SRC_ATOP);
                    }
                }
            });
        }
    }

    @OnClick(R.id.favorite_button)
    public void toggleFavorite(View v) {
        Timber.d("favorite clicked");

        SQLiteDatabase db = null;
        try {
            db = mDbHelper.getWritableDatabase();
            FavoriteMovieDB favoriteMovieDB = new FavoriteMovieDB(db);

            if (mFavorite) {
                favoriteMovieDB.removeFavorite(mMovie.getId());
                mFavorite = false;
            } else {
                favoriteMovieDB.addFavorite(mMovie.getId());
                mFavorite = true;
            }

        } finally {
            MovieDatabaseHelper.close(db);
        }

        updateUI();
    }

    public void retrieveReviews(final int pageNumber) {

        Timber.d("retrieveReviews");

        mMovieService.getReviews(mMovie.getId(), pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<MovieReviewResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, null);
                    }

                    @Override
                    public void onNext(final MovieReviewResponse movieReviewResponse) {
                        try {


                            Timber.d("Received API MovieReviewResponse");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mReviewLayout.setVisibility(movieReviewResponse.getMovieReviews().size() > 0 ? View.VISIBLE : View.GONE);

                                    mReviewList.removeAllViews();

                                    for (MovieReview review : movieReviewResponse.getMovieReviews()) {
                                        String formattedReview = String.format("Author: %s\n\n%s\n\n", review.getAuthor(), review.getContent());

                                        TextView textView = new TextView(getActivity());
                                        textView.setText(formattedReview);
                                        mReviewList.addView(textView);
                                    }
                                }
                            });

                            Timber.d("FetchMovieReviewsTask complete. %d reviews retrieved.", movieReviewResponse.getMovieReviews().size());

                        } catch (Exception e) {
                            Timber.e(e, null);
                            // handle errors
                        }
                    }
                });
    }
}
