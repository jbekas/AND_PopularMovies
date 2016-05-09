package com.redgeckotech.popularmovies.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

public class MovieDatabaseHelper extends SQLiteOpenHelper {

    private static MovieDatabaseHelper sInstance;

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    private MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MovieDatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MovieDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when no database exists in disk and the helper class needs
    // to create a new one.
    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("onCreate called.");
        MovieDB.createTable(db);
        MostPopularDB.createTable(db);
        HighestRatedDB.createTable(db);
        FavoriteMovieDB.createTable(db);
    }

    // Called when there is a database version mismatch meaning that
    // the version of the database on disk needs to be upgraded to
    // the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log the version upgrade.
        Timber.i("Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        // Upgrade the existing database to conform to the new
        // version. Multiple previous versions can be handled by
        // comparing oldVersion and newVersion values.
        // The simplest case is to drop the old table and create a new one.
        MovieDB.dropTable(db);
        MovieDB.createTable(db);

        MostPopularDB.dropTable(db);
        MostPopularDB.createTable(db);

        HighestRatedDB.dropTable(db);
        HighestRatedDB.createTable(db);

        // If necessary, upgrade favorites table so that favorites are not lost.
    }

    public static void deleteDatabase(Context context) {
        boolean result = context.deleteDatabase(DATABASE_NAME);
        Timber.d(DATABASE_NAME + " deleted: " + result);
    }

    public static void close(SQLiteDatabase database) {
        if (database != null) {
            database.close();
        }
    }

    public static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void clearDatabase(Context context) {
        MovieDatabaseHelper dbHelper = new MovieDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        MovieDB.removeAll(db);
        MostPopularDB.removeAll(db);
        HighestRatedDB.removeAll(db);
        FavoriteMovieDB.removeAll(db);

        db.execSQL("VACUUM");
    }
}
