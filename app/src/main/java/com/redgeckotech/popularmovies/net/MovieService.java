package com.redgeckotech.popularmovies.net;


import com.redgeckotech.popularmovies.model.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieService {
    @GET("/3/movie/popular") Call<MovieResponse> getPopular(@Query("api_key") String apiKey);

    @GET("/3/movie/top_rated") Call<MovieResponse> getTopRated(@Query("api_key") String apiKey);
}



