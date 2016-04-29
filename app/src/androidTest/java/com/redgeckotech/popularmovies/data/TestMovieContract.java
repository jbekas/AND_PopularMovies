package com.redgeckotech.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestMovieContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final long TEST_MOVIE_ID = 118340;

    public void testBuildMovie() {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMovieUri in " +
                        "MovieContract.",
                movieUri);
        assertEquals("Error: Movie id not properly appended to the end of the Uri",
                Long.toString(TEST_MOVIE_ID), movieUri.getLastPathSegment());
    }
}

