package com.redgeckotech.popularmovies.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMovieDB implements BaseColumns {

    private SQLiteDatabase database;

    public static final String TABLE_NAME = "favorites";

    public static final String COLUMN_MOVIE_ID = "movie_id";

    public static final String CREATE_SQL =
            "create table " + TABLE_NAME +
                    " (" + _ID + " integer primary key not null, " +
                    COLUMN_MOVIE_ID + " integer)";
    public static final String DROP_SQL =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL(DROP_SQL);
    }

    public static void removeAll(SQLiteDatabase db) {
        db.execSQL("DELETE from " + TABLE_NAME);
    }

    public FavoriteMovieDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addFavorite(long movieId) {

        //Timber.v("Saving movie: %s", movie);

        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVIE_ID, movieId);

        return database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(long movieId) {
        database.execSQL("DELETE from " + TABLE_NAME + " WHERE " + COLUMN_MOVIE_ID + "=?", new String[]{Long.toString(movieId)});
    }

    public List<Long> findAll() {
        Cursor cursor = null;
        List<Long> favorites = new ArrayList<>();

        try {
            cursor = database.query(TABLE_NAME, null, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    favorites.add(cursor.getLong(0));
                }
            }
        } finally {
            MovieDatabaseHelper.close(cursor);
        }

        return favorites;
    }

    public boolean isFavorite(long movieId) {
        Cursor cursor = null;

        String selection = COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[] { Long.toString(movieId) };

        try {
            cursor = database.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return true;
                }
            }
        } finally {
            MovieDatabaseHelper.close(cursor);
        }

        return false;
    }
}