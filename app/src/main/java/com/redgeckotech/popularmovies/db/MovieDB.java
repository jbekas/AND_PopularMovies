package com.redgeckotech.popularmovies.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.redgeckotech.popularmovies.model.Movie;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MovieDB {
    private SQLiteDatabase database;

    public final static String TABLE = "movies";

    public static final String ID = "_id";
    public static final String ADULT = "adult";
    public static final String BACKDROP_PATH = "backdrop_path";
    public static final String GENRE_IDS = "genre_ids";
    public static final String ORIGINAL_LANGUAGE = "original_language";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String OVERVIEW = "overview";
    public static final String POPULARITY = "popularity";
    public static final String POSTER_PATH = "poster_path";
    public static final String RELEASE_DATE = "release_date";
    public static final String TITLE = "title";
    public static final String VIDEO = "video";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String VOTE_COUNT = "vote_count";

    public static final String CREATE_SQL =
            "create table " + TABLE +
                    " (" + ID + " integer primary key not null, " +
                    ADULT + " integer, " +
                    BACKDROP_PATH + " text, " +
                    GENRE_IDS + " text, " +
                    ORIGINAL_LANGUAGE + " text, " +
                    ORIGINAL_TITLE + " text, " +
                    OVERVIEW + " text, " +
                    POPULARITY + " real, " +
                    POSTER_PATH + " text, " +
                    RELEASE_DATE + " text, " +
                    TITLE + " text, " +
                    VIDEO + " integer, " +
                    VOTE_AVERAGE + " real, " +
                    VOTE_COUNT + " integer)";
    public static final String DROP_SQL =
            "DROP TABLE IF EXISTS " + TABLE;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL(DROP_SQL);
    }

    public static void removeAll(SQLiteDatabase db) {
        db.execSQL("DELETE from " + TABLE);
    }

    public MovieDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long save(Movie movie) {

        Timber.v("Saving movie: %s", movie);

        ContentValues values = new ContentValues();
        values.put(ID, movie.getId());
        values.put(ADULT, movie.isAdult() ? 1 : 0);
        values.put(BACKDROP_PATH, movie.getBackdropPath());
        values.put(GENRE_IDS, movie.getGenreIdsAsString());
        values.put(ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
        values.put(ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(OVERVIEW, movie.getOverview());
        values.put(POPULARITY, movie.getPopularity());
        values.put(POSTER_PATH, movie.getPosterPath());
        values.put(RELEASE_DATE, movie.getReleaseDate());
        values.put(TITLE, movie.getTitle());
        values.put(VIDEO, movie.isVideo() ? 1 : 0);
        values.put(VOTE_AVERAGE, movie.getVoteAverage());
        values.put(VOTE_COUNT, movie.getVoteCount());

        return database.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void remove(String movieId) {
        database.execSQL("DELETE from " + TABLE + " WHERE " + ID + "=?", new String[]{movieId});
    }

    public Movie findMovie(int movieId) {
        Cursor cursor = null;
        Movie movie = null;

        try {
            String selection = ID + "=?";
            String[] selectionArgs = new String[]{Integer.toString(movieId)};
            cursor = database.query(TABLE, null, selection, selectionArgs, null, null, null, null);
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
            cursor = database.query(TABLE, null, null, null, null, null, null, null);
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

    public static Movie createMovie(Cursor cursor) {

        Movie movie = new Movie();
        movie.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        movie.setAdult(cursor.getInt(cursor.getColumnIndex(ADULT)) == 1);
        movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(BACKDROP_PATH)));
        movie.setGenreIdsFromString(cursor.getString(cursor.getColumnIndex(GENRE_IDS)));
        movie.setOriginalLanguage(cursor.getString(cursor.getColumnIndex(ORIGINAL_LANGUAGE)));
        movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(ORIGINAL_TITLE)));
        movie.setOverview(cursor.getString(cursor.getColumnIndex(OVERVIEW)));
        movie.setPopularity(cursor.getFloat(cursor.getColumnIndex(POPULARITY)));
        movie.setPosterPath(cursor.getString(cursor.getColumnIndex(POSTER_PATH)));
        movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(RELEASE_DATE)));
        movie.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        movie.setVideo(cursor.getInt(cursor.getColumnIndex(VIDEO)) == 1);
        movie.setVoteAverage(cursor.getFloat(cursor.getColumnIndex(VOTE_AVERAGE)));
        movie.setVoteCount(cursor.getInt(cursor.getColumnIndex(VOTE_COUNT)));

        return movie;
    }
}

