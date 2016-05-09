package com.redgeckotech.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.redgeckotech.popularmovies.data.MovieContract.FavoritesEntry;
import com.redgeckotech.popularmovies.data.MovieContract.HighestRatedEntry;
import com.redgeckotech.popularmovies.data.MovieContract.MostPopularEntry;
import com.redgeckotech.popularmovies.data.MovieContract.MovieEntry;
import com.redgeckotech.popularmovies.db.MovieDatabaseHelper;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDatabaseHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int MOST_POPULAR = 102;
    static final int HIGHEST_RATED = 103;
    static final int FAVORITES = 104;

    private static final SQLiteQueryBuilder sMoviesQueryBuilder;
    private static final SQLiteQueryBuilder sMostPopularQueryBuilder;
    private static final SQLiteQueryBuilder sHighestRatedQueryBuilder;
    private static final SQLiteQueryBuilder sFavoritesQueryBuilder;

    static{
        sMoviesQueryBuilder = new SQLiteQueryBuilder();
        sMoviesQueryBuilder.setTables(MovieEntry.TABLE_NAME);

        sMostPopularQueryBuilder = new SQLiteQueryBuilder();
        sMostPopularQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MostPopularEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID +
                        " = " + MostPopularEntry.TABLE_NAME +
                        "." + MostPopularEntry.COLUMN_MOVIE_ID);

        sHighestRatedQueryBuilder = new SQLiteQueryBuilder();
        sHighestRatedQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        HighestRatedEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID +
                        " = " + HighestRatedEntry.TABLE_NAME +
                        "." + HighestRatedEntry.COLUMN_MOVIE_ID);

        sFavoritesQueryBuilder = new SQLiteQueryBuilder();
        sFavoritesQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        FavoritesEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry._ID +
                        " = " + FavoritesEntry.TABLE_NAME +
                        "." + FavoritesEntry.COLUMN_MOVIE_ID);
    }

    //movies._id = ?
    private static final String sMovieSelection =
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = ? ";

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        long movieId = MovieEntry.getMovieIdFromUri(uri);

        String selection = sMovieSelection;
        String[] selectionArgs = new String[]{Long.toString(movieId)};

        return sMoviesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCursor(@NonNull SQLiteQueryBuilder builder,
                             String[] projection,
                             String selection,
                             String[] selectionArgs,
                             String sortOrder) {

        return builder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_MOST_POPULAR, MOST_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_HIGHEST_RATED, HIGHEST_RATED);
        matcher.addURI(authority, MovieContract.PATH_FAVORITES, FAVORITES);

        return matcher;
    }

    /*
        Create a new MovieDatabaseHelper for later use
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = MovieDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITES:
                return FavoritesEntry.FAVORITES_TYPE;
            case HIGHEST_RATED:
                return HighestRatedEntry.HIGHEST_RATED_TYPE;
            case MOST_POPULAR:
                return MostPopularEntry.MOST_POPULAR_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "favorites"
            case FAVORITES: {
                retCursor = getCursor(sFavoritesQueryBuilder, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "highest_rated"
            case HIGHEST_RATED: {
                retCursor = getCursor(sHighestRatedQueryBuilder, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "most_popular"
            case MOST_POPULAR: {
                retCursor = getCursor(sMostPopularQueryBuilder, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "movie/#"
            case MOVIE_WITH_ID: {
                retCursor = getMovieByMovieId(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIE: {
                retCursor = getCursor(sMoviesQueryBuilder, projection, selection, selectionArgs, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVORITES: {
                long _id = db.insertWithOnConflict(FavoritesEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_NONE);
                if ( _id > 0 )
                    returnUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case FAVORITES:
                rowsDeleted = db.delete(
                        FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HIGHEST_RATED:
                rowsDeleted = db.delete(
                        HighestRatedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOST_POPULAR:
                rowsDeleted = db.delete(
                        MostPopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;

        switch (match) {
            case HIGHEST_RATED:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(HighestRatedEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MOST_POPULAR:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MostPopularEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}