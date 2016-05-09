package com.redgeckotech.popularmovies.net;


import com.redgeckotech.popularmovies.model.MovieResponse;
import com.redgeckotech.popularmovies.model.MovieReviewResponse;
import com.redgeckotech.popularmovies.model.RelatedVideosResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface MovieService {
    @GET("/3/movie/popular") Observable<MovieResponse> getPopular(@Query("page") int page);

    @GET("/3/movie/top_rated") Observable<MovieResponse> getTopRated(@Query("page") int page);

    @GET("/3/movie/{id}/reviews") Observable<MovieReviewResponse> getReviews(@Path("id") long movieId, @Query("page") int page);

    @GET("/3/movie/{id}/videos") Observable<RelatedVideosResponse> getRelatedVideos(@Path("id") long movieId, @Query("page") int page);
}



