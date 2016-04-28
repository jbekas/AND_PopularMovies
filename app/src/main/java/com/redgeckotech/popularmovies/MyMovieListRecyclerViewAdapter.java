package com.redgeckotech.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.redgeckotech.popularmovies.MovieListFragment.OnListFragmentInteractionListener;
import com.redgeckotech.popularmovies.db.MovieDB;
import com.redgeckotech.popularmovies.model.Movie;
import com.redgeckotech.popularmovies.widget.CursorRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Movie} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyMovieListRecyclerViewAdapter extends CursorRecyclerViewAdapter<MyMovieListRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private final Picasso mPicasso;

    private final String mPosterSize = "w185";

    public MyMovieListRecyclerViewAdapter(Context context,
                                          Cursor cursor,
                                          OnListFragmentInteractionListener listener,
                                          Picasso picasso) {

        super(context, cursor);

        mListener = listener;
        mPicasso = picasso;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_movielist, parent, false);
        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(final ViewHolder holder, int position) {
//        holder.mItem = mValues.get(position);
//        //holder.mIdView.setText("" + mValues.get(position).hashCode());
//
//        //Timber.d("%d %s", mValues.get(position).hashCode(), mValues.get(position).getTitle());
//
//        //holder.mContentView.setText(mValues.get(position).content);
//        mPicasso.load(String.format("http://image.tmdb.org/t/p/%s/%s", mPosterSize, mValues.get(position).getPosterPath())).into(holder.mContentView);
//
//        //http://image.tmdb.org/t/p/dlIPGXPxXQTp9kFrRzn0RsfUelx.jpg?api_key=07bb317f20ae1d58939907399b77c710
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
//    }

//    @Override
//    public int getItemCount() {
//        return super.getItemCount()
//    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {
        Movie movie = MovieDB.createMovie(cursor);
        //viewHolder.mItem = mValues.get(position);
        //holder.mIdView.setText("" + mValues.get(position).hashCode());

        //Timber.d("%d %s", mValues.get(position).hashCode(), mValues.get(position).getTitle());

        //holder.mContentView.setText(mValues.get(position).content);
        mPicasso.load(String.format("http://image.tmdb.org/t/p/%s/%s", mPosterSize, movie.getPosterPath())).into(viewHolder.mContentView);

        //http://image.tmdb.org/t/p/dlIPGXPxXQTp9kFrRzn0RsfUelx.jpg?api_key=07bb317f20ae1d58939907399b77c710
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(viewHolder.mItem);
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        //public final TextView mIdView;
        public final ImageView mContentView;
        public Movie mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            //mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (ImageView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.toString() + "'";
        }
    }
}
