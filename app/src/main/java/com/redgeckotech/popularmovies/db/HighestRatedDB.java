package com.redgeckotech.popularmovies.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.redgeckotech.popularmovies.data.MovieContract.HighestRatedEntry;

public class HighestRatedDB implements BaseColumns {

    private SQLiteDatabase database;

    public static final String CREATE_SQL =
            "create table " + HighestRatedEntry.TABLE_NAME +
                    " (" + HighestRatedEntry._ID + " integer primary key AUTOINCREMENT, " +
                    HighestRatedEntry.COLUMN_POSITION + " integer not null, " +
                    HighestRatedEntry.COLUMN_MOVIE_ID + " integer not null)";
    public static final String DROP_SQL =
            "DROP TABLE IF EXISTS " + HighestRatedEntry.TABLE_NAME;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL(DROP_SQL);
    }

    public static void removeAll(SQLiteDatabase db) {
        db.execSQL("DELETE from " + HighestRatedEntry.TABLE_NAME);
    }

    public HighestRatedDB(SQLiteDatabase database) {
        this.database = database;
    }
}