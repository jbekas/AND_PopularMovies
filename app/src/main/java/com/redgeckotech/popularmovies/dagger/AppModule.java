package com.redgeckotech.popularmovies.dagger;

import android.content.Context;

import com.redgeckotech.popularmovies.BuildConfig;
import com.redgeckotech.popularmovies.MoviesApplication;
import com.redgeckotech.popularmovies.net.MovieService;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module
public class AppModule {

    public static final String API_BASE_URL = "http://api.themoviedb.org/";

    private final MoviesApplication application;

    public AppModule(MoviesApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return this.application;
    }

    @Provides
    @Named("MovieDbHttpClient")
    OkHttpClient getMovieDbHttpClient() {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.LOG_RETROFIT_QUERIES) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        Interceptor apiAuthenticationInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl url = request.url().newBuilder().addQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY).build();
                request = request.newBuilder().url(url).build();
                return chain.proceed(request);
            }
        };

        return httpClientBuilder
                .addInterceptor(interceptor)
                .addInterceptor(apiAuthenticationInterceptor)
                .build();
    }

    @Provides
    MovieService getMovieService() {

        try {
            OkHttpClient httpClient = getMovieDbHttpClient();

            Retrofit.Builder builder =
                    new Retrofit.Builder()
                            .baseUrl(API_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.client(httpClient).build();
            return retrofit.create(MovieService.class);
        } catch (Exception e) {
            Timber.w(e, null);
        }

        return null;
    }

    @Provides
    Picasso getPicasso(Context context) {
        Picasso.Builder picassoBuilder = new Picasso.Builder(context);

        // Picasso.Builder creates the Picasso object to do the actual requests
        return picassoBuilder.build();
    }
}
