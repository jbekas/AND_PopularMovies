package com.redgeckotech.popularmovies.net;

import com.redgeckotech.popularmovies.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    public static final String API_BASE_URL = "http://api.themoviedb.org/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    // TODO Use dagger and dependency injection

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.LOG_RETROFIT_QUERIES) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        Retrofit retrofit = builder.client(httpClient.addInterceptor(interceptor).build()).build();
        return retrofit.create(serviceClass);
    }
}
