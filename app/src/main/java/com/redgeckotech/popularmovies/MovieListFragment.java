package com.redgeckotech.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redgeckotech.popularmovies.db.MovieDB;
import com.redgeckotech.popularmovies.db.MovieDatabaseHelper;
import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.model.MovieResponse;
import com.redgeckotech.popularmovies.net.MovieService;
import com.redgeckotech.popularmovies.util.EndlessRecyclerOnScrollListener;
import com.squareup.picasso.Picasso;

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
public class MovieListFragment extends Fragment {

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

    private String mQueryType;

    public static String mHighestRated;
    public static String mMostPopular;

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
        final MovieDB.SORT_ORDER sortOrder;

        if (mHighestRated.equals(mQueryType)) {
            call = mMovieService.getTopRated(pageNumber);
            sortOrder = MovieDB.SORT_ORDER.HIGHEST_RATED;
        } else {
            call = mMovieService.getPopular(pageNumber);
            sortOrder = MovieDB.SORT_ORDER.MOST_POPULAR;
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

                        MovieDatabaseHelper dbHelper = MovieDatabaseHelper.getInstance(getActivity());
                        SQLiteDatabase db = null;
                        try {
                            db = dbHelper.getWritableDatabase();
                            final MovieDB movieDB = new MovieDB(db);

                            // If this is the first page, remove all items
                            if (pageNumber == 1) {
                                MovieDB.removeAll(db);
                            }

                            for (Movie movie : movieResponse.getMovies()) {
                                movieDB.save(movie);
                            }

                            // TODO move this to a ContentResolver and use the ContentResolver Observer pattern
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Cursor c = movieDB.findCursor(sortOrder);
                                    Timber.d("swapCursor");
                                    mAdapter.swapCursor(c);
                                }
                            });

                        } finally {
                            //MovieDatabaseHelper.close(db);
                        }

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
}
