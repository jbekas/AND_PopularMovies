package com.redgeckotech.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final long TEST_MOVIE_ID = 118340;

    // content://com.redgeckotech.popularmovies/movies"
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_MOVIE_ID = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The MOVIE WITH MOVIE ID URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MOVIE_ID), MovieProvider.MOVIE_WITH_ID);
    }
}
