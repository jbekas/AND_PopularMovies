package com.redgeckotech.popularmovies.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.redgeckotech.popularmovies.data.MovieContract.MovieEntry;
import com.redgeckotech.popularmovies.model.Movie;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MovieDB {
    private SQLiteDatabase database;

    public enum SORT_ORDER { MOST_POPULAR, HIGHEST_RATED }

    public static final String CREATE_SQL =
            "create table " + MovieEntry.TABLE_NAME +
                    " (" + MovieEntry._ID + " integer primary key not null, " +
                    MovieEntry.COLUMN_ADULT + " integer, " +
                    MovieEntry.COLUMN_BACKDROP_PATH + " text, " +
                    MovieEntry.COLUMN_GENRE_IDS + " text, " +
                    MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " text, " +
                    MovieEntry.COLUMN_ORIGINAL_TITLE + " text, " +
                    MovieEntry.COLUMN_OVERVIEW + " text, " +
                    MovieEntry.COLUMN_POPULARITY + " real, " +
                    MovieEntry.COLUMN_POSTER_PATH + " text, " +
                    MovieEntry.COLUMN_RELEASE_DATE + " text, " +
                    MovieEntry.COLUMN_TITLE + " text, " +
                    MovieEntry.COLUMN_VIDEO + " integer, " +
                    MovieEntry.COLUMN_VOTE_AVERAGE + " real, " +
                    MovieEntry.COLUMN_VOTE_COUNT + " integer)";
    public static final String DROP_SQL =
            "DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL(DROP_SQL);
    }

    public static void removeAll(SQLiteDatabase db) {
        db.execSQL("DELETE from " + MovieEntry.TABLE_NAME);
    }

    public MovieDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long save(Movie movie) {

        //Timber.v("Saving movie: %s", movie);

        ContentValues values = new ContentValues();
        values.put(MovieEntry._ID, movie.getId());
        values.put(MovieEntry.COLUMN_ADULT, movie.isAdult() ? 1 : 0);
        values.put(MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        values.put(MovieEntry.COLUMN_GENRE_IDS, movie.getGenreIdsAsString());
        values.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
        values.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
        values.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MovieEntry.COLUMN_VIDEO, movie.isVideo() ? 1 : 0);
        values.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());

        return database.insertWithOnConflict(MovieEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void remove(String movieId) {
        database.execSQL("DELETE from " + MovieEntry.TABLE_NAME + " WHERE " + MovieEntry._ID + "=?", new String[]{movieId});
    }

    public Movie findMovie(int movieId) {
        Cursor cursor = null;
        Movie movie = null;

        try {
            String selection = MovieEntry._ID + "=?";
            String[] selectionArgs = new String[]{Integer.toString(movieId)};
            cursor = database.query(MovieEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                movie = createMovie(cursor);
            }
        } finally {
            MovieDatabaseHelper.close(cursor);
        }

        return movie;
    }

    public List<Movie> findAll() {
        Cursor cursor = null;
        List<Movie> movies = new ArrayList<>();

        try {
            cursor = database.query(MovieEntry.TABLE_NAME, null, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Movie movie = createMovie(cursor);
                    movies.add(movie);
                }
            }
        } finally {
            MovieDatabaseHelper.close(cursor);
        }

        return movies;
    }

    public Cursor findCursor(SORT_ORDER sortOrder) {

        String sort = null;
        switch (sortOrder) {
            case MOST_POPULAR:
                sort = MovieEntry.COLUMN_POPULARITY + " DESC";
                break;
            case HIGHEST_RATED:
                sort = MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
                break;
            default:
                Timber.e("No sort order specified.");
        }

        return database.query(MovieEntry.TABLE_NAME, null, null, null, null, null, sort, null);
    }

    public static Movie createMovie(Cursor cursor) {

        Movie movie = new Movie();
        movie.setId(cursor.getInt(cursor.getColumnIndex(MovieEntry._ID)));
        movie.setAdult(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) == 1);
        movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH)));
        movie.setGenreIdsFromString(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_GENRE_IDS)));
        movie.setOriginalLanguage(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE)));
        movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
        movie.setPopularity(cursor.getFloat(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY)));
        movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)));
        movie.setVideo(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VIDEO)) == 1);
        movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE)));
        movie.setVoteCount(cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT)));

        return movie;
    }
}