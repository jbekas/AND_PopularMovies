package com.redgeckotech.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.redgeckotech.popularmovies.data.MovieContract.MovieEntry;
import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.model.MovieResponse;
import com.redgeckotech.popularmovies.net.MovieService;
import com.redgeckotech.popularmovies.util.EndlessRecyclerOnScrollListener;
import com.squareup.picasso.Picasso;

import java.util.Vector;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A fragment representing a list of Movies.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 * <p/>
 */
public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LIST_LOADER = 0;

    public static final String QUERY_TYPE = "QUERY_TYPE";
    public static final String PAGE_NUMBER = "PAGE_NUMBER";

    // Dependency injection points
    @Inject MovieService mMovieService;
    @Inject Picasso mPicasso;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;

    private MyMovieListRecyclerViewAdapter mAdapter;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private EndlessRecyclerOnScrollListener mEndlessScrollListener;
    private int mPosition = ListView.INVALID_POSITION;

    private String mQueryType;

    public static String mHighestRated;
    public static String mMostPopular;

    private MovieContentObserver mMovieContentObserver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MovieListFragment newInstance(int columnCount) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency injection
        ((MoviesApplication) getActivity().getApplicationContext()).getApplicationComponent().inject(this);

        mMostPopular = getString(R.string.most_popular);
        mHighestRated = getString(R.string.highest_rated);

        int pageNumber;

        if (savedInstanceState != null) {
            mQueryType = savedInstanceState.getString(QUERY_TYPE);
            pageNumber = savedInstanceState.getInt(PAGE_NUMBER);
        } else {
            if (getArguments() != null) {
                mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            }
            pageNumber = 1;
        }

        mEndlessScrollListener = new EndlessRecyclerOnScrollListener(pageNumber) {
            @Override
            public void onLoadMore(int currentPage) {
                Timber.d("onLoadMore: %d", currentPage);
                updateMovieList(currentPage);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movielist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            mLayoutManager = new GridLayoutManager(view.getContext(), mColumnCount);

            mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new MyMovieListRecyclerViewAdapter(getActivity(), null, mListener, mPicasso);
            mRecyclerView.setAdapter(mAdapter);

            // Add endless scroller
            mEndlessScrollListener.setLinearLayoutManager(mLayoutManager);
            mRecyclerView.addOnScrollListener(mEndlessScrollListener);

            mRecyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("onClick %s", v);
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();
        if (context == null) {
            return;
        }

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String queryType = sharedPrefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        if (!queryType.equals(mQueryType)) {
            mQueryType = queryType;
            updateMovieList(1);
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (mHighestRated.equals(mQueryType)) {
                actionBar.setTitle(R.string.highest_rated_label);
            } else {
                actionBar.setTitle(R.string.most_popular_label);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(QUERY_TYPE, mQueryType);
        outState.putInt(PAGE_NUMBER, mEndlessScrollListener.getCurrentPage());
    }

    public void updateMovieList(final int pageNumber) {

        if (mQueryType == null) {
            mQueryType = getString(R.string.pref_sort_default);
        }

        final Observable<MovieResponse> call;

        if (mHighestRated.equals(mQueryType)) {
            call = mMovieService.getTopRated(pageNumber);
        } else {
            call = mMovieService.getPopular(pageNumber);
        }

        call.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<MovieResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, null);
                    }

                    @Override
                    public void onNext(MovieResponse movieResponse) {
                        try {

                            Timber.d("Received API MovieResponse");

                            // If this is the first page, remove all items
                            if (pageNumber == 1) {
                                getActivity().getContentResolver().delete(
                                        MovieEntry.CONTENT_URI,
                                        null,
                                        null
                                );
                            }

                            Vector<ContentValues> cVVector = new Vector<>(movieResponse.getMovies().size());

                            for (Movie movie : movieResponse.getMovies()) {

                                ContentValues movieValues = new ContentValues();

                                movieValues.put(MovieEntry._ID, movie.getId());
                                movieValues.put(MovieEntry.COLUMN_ADULT, movie.isAdult() ? 1 : 0);
                                movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                                movieValues.put(MovieEntry.COLUMN_GENRE_IDS, movie.getGenreIdsAsString());
                                movieValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
                                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                                movieValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                                movieValues.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
                                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                                movieValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
                                movieValues.put(MovieEntry.COLUMN_VIDEO, movie.isVideo() ? 1 : 0);
                                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                                movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());

                                cVVector.add(movieValues);
                            }

                            int inserted = 0;

                            // add to database
                            if (cVVector.size() > 0) {
                                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                                cVVector.toArray(cvArray);
                                inserted = getActivity().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
                            }

                            Timber.d("FetchMovieTask complete. %d movies inserted.", inserted);

                        } catch (Exception e) {
                            Timber.e(e, null);
                            // handle errors
                        }
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        getActivity().getContentResolver().unregisterContentObserver(mMovieContentObserver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(MOVIE_LIST_LOADER, null, this);

        mMovieContentObserver = getMovieContentObserver();

        getActivity().getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, mMovieContentObserver);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Movie item);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // LoaderManager.LoaderCallbacks<Cursor> implementation
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Timber.d("onCreateLoader %s", mQueryType);

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:  Descending my popularity or vote average
        String sortOrder;

        if (mHighestRated.equals(mQueryType)) {
            sortOrder = MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        } else {
            sortOrder = MovieEntry.COLUMN_POPULARITY + " DESC";
        }

        Uri movieListUri = MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                movieListUri,
                null, //FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // MovieContentObserver
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static class MovieContentObserver extends ContentObserver {
        final HandlerThread mHT;

        static MovieContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new MovieContentObserver(ht);
        }

        private MovieContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }
    }

    static MovieContentObserver getMovieContentObserver() {
        return MovieContentObserver.getTestContentObserver();
    }

}
