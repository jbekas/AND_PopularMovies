package com.redgeckotech.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.model.MovieResponse;
import com.redgeckotech.popularmovies.net.MovieService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MovieListFragment extends Fragment {

    // Dependency injection points
    @Inject MovieService mMovieService;
    @Inject Picasso mPicasso;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;

    private MyMovieListRecyclerViewAdapter mAdapter;
    private final List<Movie> mMovies = new ArrayList<Movie>();

    private String mQueryType;
    private int mPage;

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

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movielist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new MyMovieListRecyclerViewAdapter(mMovies, mListener, mPicasso);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


//        final Call<MovieResponse> call = movieService.getTopRated(1);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    MovieResponse movieResponse = call.execute().body();
//
//                    Timber.d(movieResponse.toString());
//                } catch (IOException e) {
//                    Timber.e(e, null);
//                    // handle errors
//                }
//
//            }
//        }).start();
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
            mPage = 1;
            updateMovieList();
        }
    }

    public void updateMovieList() {

        if (mQueryType == null) {
            mQueryType = getString(R.string.pref_sort_default);
        }

        final Call<MovieResponse> call;

        String highestRated = getString(R.string.highest_rated);

        if (highestRated.equals(mQueryType)) {
            call = mMovieService.getTopRated(1);
        } else {
            call = mMovieService.getPopular(1);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MovieResponse movieResponse = call.execute().body();

                    Timber.d(movieResponse.toString());

                    // If this is the first page, remove all items
                    if (mPage == 1) {
                        mMovies.clear();
                    }

                    // Append new items to list
                    mMovies.addAll(movieResponse.getMovies());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (IOException e) {
                    Timber.e(e, null);
                    // handle errors
                }

            }
        }).start();
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
