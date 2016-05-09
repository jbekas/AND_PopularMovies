package com.redgeckotech.popularmovies.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.redgeckotech.popularmovies.data.MovieContract.MostPopularEntry;

public class MostPopularDB implements BaseColumns {

    private SQLiteDatabase database;

    public static final String CREATE_SQL =
            "create table " + MostPopularEntry.TABLE_NAME +
                    " (" + MostPopularEntry._ID + " integer primary key AUTOINCREMENT, " +
                    MostPopularEntry.COLUMN_POSITION + " integer not null, " +
                    MostPopularEntry.COLUMN_MOVIE_ID + " integer not null)";
    public static final String DROP_SQL =
            "DROP TABLE IF EXISTS " + MostPopularEntry.TABLE_NAME;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL(DROP_SQL);
    }

    public static void removeAll(SQLiteDatabase db) {
        db.execSQL("DELETE from " + MostPopularEntry.TABLE_NAME);
    }

    public MostPopularDB(SQLiteDatabase database) {
        this.database = database;
    }
}