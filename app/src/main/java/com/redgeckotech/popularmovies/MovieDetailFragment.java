package com.redgeckotech.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
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
import android.widget.Toast;

import com.redgeckotech.popularmovies.db.FavoriteMovieDB;
import com.redgeckotech.popularmovies.db.MovieDatabaseHelper;
import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.model.MovieReview;
import com.redgeckotech.popularmovies.model.MovieReviewResponse;
import com.redgeckotech.popularmovies.model.RelatedVideo;
import com.redgeckotech.popularmovies.model.RelatedVideosResponse;
import com.redgeckotech.popularmovies.net.MovieService;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MovieDetailFragment extends Fragment {

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
    @Bind(R.id.review_group) ViewGroup mReviewGroup;
    @Bind(R.id.related_videos_layout) ViewGroup mRelatedVideosLayout;
    @Bind(R.id.related_video_group) ViewGroup mRelatedVideoGroup;

    private MovieDatabaseHelper mDbHelper;

    private List<MovieReview> mReviews = new ArrayList<>();
    private List<RelatedVideo> mRelatedVideos = new ArrayList<>();

    private boolean mFavorite;

    @ColorInt private int activeColor;
    @ColorInt private int inactiveColor;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency injection
        ((MoviesApplication) getActivity().getApplicationContext()).getApplicationComponent().inject(this);

        if (savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(Constants.EXTRA_MOVIE);
        } else {
            if (getArguments() != null) {
                mMovie = getArguments().getParcelable(Constants.EXTRA_MOVIE);
            }
        }

        if (mMovie == null) {
            Timber.w("mMovie is null.");
            return;
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
        retrieveRelatedVideos(1);
    }

    public void updateUI() {
        final Activity activity = getActivity();

        if (activity == null) {
            Timber.w("Activity no longer exists, returning.");
            return;
        } else {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mMovie == null) {
                            mScrollView.setVisibility(View.INVISIBLE);
                        } else {
                            mScrollView.setVisibility(View.VISIBLE);
                            mPicasso.load(String.format("http://image.tmdb.org/t/p/%s/%s", mPosterSize, mMovie.getPosterPath())).into(mMoviePoster);

                            mMovieTitle.setText(mMovie.getTitle());
                            mOverview.setText(mMovie.getOverview());

                            String releaseYear = mMovie.getReleaseYear();
                            mYear.setText(getString(R.string.release_year, releaseYear));
                            mYear.setVisibility(releaseYear == null ? View.GONE : View.VISIBLE);

                            // Limit decimals to 2, but strip if trailing decimals are 0
                            BigDecimal bd = new BigDecimal(mMovie.getVoteAverage()).setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                            mVoteAverage.setText(getString(R.string.vote_average, bd.toString()));

                            mMovieTitle.setSelected(true);

                            mFavoriteButton.setColorFilter(mFavorite ? activeColor : inactiveColor, PorterDuff.Mode.SRC_ATOP);

                            LayoutInflater inflater = activity.getLayoutInflater();

                            // Movie Reviews
                            mReviewLayout.setVisibility(mReviews.size() > 0 ? View.VISIBLE : View.GONE);

                            mReviewGroup.removeAllViews();

                            for (MovieReview review : mReviews) {
                                View view = inflater.inflate(R.layout.template_review, mReviewGroup, false);

                                TextView author = (TextView) view.findViewById(R.id.review_author);
                                author.setText(review.getAuthor());

                                TextView content = (TextView) view.findViewById(R.id.review_content);
                                content.setText(review.getContent());

                                mReviewGroup.addView(view);
                            }

                            // Related Videos
                            mRelatedVideosLayout.setVisibility(mRelatedVideos.size() > 0 ? View.VISIBLE : View.GONE);

                            mRelatedVideoGroup.removeAllViews();

                            for (RelatedVideo relatedVideo : mRelatedVideos) {
                                if (Constants.TEXT_YOUTUBE_SITE.equals(relatedVideo.getSite()) && Constants.TEXT_TRAILER.equals(relatedVideo.getType())) {
                                    View view = inflater.inflate(R.layout.template_related_video, mRelatedVideoGroup, false);

                                    final String videoPath = String.format(Constants.TEXT_YOUTUBE_URI, relatedVideo.getKey());

                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));

                                            PackageManager manager = activity.getPackageManager();
                                            List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
                                            if (infos.size() <= 0) {
                                                Toast.makeText(activity, R.string.error_no_video_player, Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            startActivity(intent);
                                        }
                                    });

                                    TextView name = (TextView) view.findViewById(R.id.trailer_name);
                                    name.setText(relatedVideo.getName());

                                    mRelatedVideoGroup.addView(view);
                                }
                            }
                        }
                    } catch (IllegalStateException e) {
                        Timber.e(e, null);
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

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), mFavorite ? R.string.favorite_saved : R.string.favorite_removed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } finally {
            MovieDatabaseHelper.close(db);
        }

        updateUI();
    }

    public void retrieveReviews(final int pageNumber) {

        if (mMovie == null) {
            Timber.w("mMovie is null.");
            return;
        }

        mMovieService.getReviews(mMovie.getId(), pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<MovieReviewResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, null);
                    }

                    @Override
                    public void onNext(final MovieReviewResponse movieReviewResponse) {
                        try {

                            Timber.d("Received API MovieReviewResponse");

                            mReviews = movieReviewResponse.getMovieReviews();

                            Timber.d("FetchMovieReviewsTask complete. %d reviews retrieved.", movieReviewResponse.getMovieReviews().size());

                            updateUI();

                        } catch (Exception e) {
                            Timber.e(e, null);
                            // handle errors
                        }
                    }
                });
    }

    public void retrieveRelatedVideos(final int pageNumber) {

        if (mMovie == null) {
            Timber.w("mMovie is null.");
            return;
        }

        mMovieService.getRelatedVideos(mMovie.getId(), pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<RelatedVideosResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, null);
                    }

                    @Override
                    public void onNext(final RelatedVideosResponse relatedVideosResponse) {
                        try {

                            Timber.d("Received API RelatedVideosResponse");

                            mRelatedVideos = relatedVideosResponse.getRelatedVideos();

                            updateUI();

                            Timber.d("FetchRelatedVideosTask complete. %d related videos retrieved.", relatedVideosResponse.getRelatedVideos().size());

                        } catch (Exception e) {
                            Timber.e(e, null);
                            // handle errors
                        }
                    }
                });
    }

}
