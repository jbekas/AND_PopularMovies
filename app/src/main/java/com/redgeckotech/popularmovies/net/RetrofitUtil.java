package com.redgeckotech.popularmovies.net;

import com.redgeckotech.popularmovies.BuildConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    private static Interceptor apiAuthenticationInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            HttpUrl url = request.url().newBuilder().addQueryParameter("api_key",BuildConfig.THE_MOVIE_DB_API_KEY).build();
            request = request.newBuilder().url(url).build();
            return chain.proceed(request);
        }
    };

    public static <S> S createService(Class<S> serviceClass) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.LOG_RETROFIT_QUERIES) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient client = httpClient
                .addInterceptor(interceptor)
                .addInterceptor(apiAuthenticationInterceptor)
                .build();

        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}
