package com.redgeckotech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieResponse implements Parcelable {

    @SerializedName("page") protected int page;
    @SerializedName("total_pages") protected int totalPages;
    @SerializedName("total_results") protected int totalResults;
    @SerializedName("results") protected List<Movie> movies;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    @Override
    public String toString() {
        return "MovieResponse{" +
                "page=" + page +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                ", movies=" + movies +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeInt(this.totalPages);
        dest.writeInt(this.totalResults);
        dest.writeList(this.movies);
    }

    public MovieResponse() {
    }

    protected MovieResponse(Parcel in) {
        this.page = in.readInt();
        this.totalPages = in.readInt();
        this.totalResults = in.readInt();
        this.movies = new ArrayList<Movie>();
        in.readList(this.movies, Movie.class.getClassLoader());
    }

    public static final Parcelable.Creator<MovieResponse> CREATOR = new Parcelable.Creator<MovieResponse>() {
        @Override
        public MovieResponse createFromParcel(Parcel source) {
            return new MovieResponse(source);
        }

        @Override
        public MovieResponse[] newArray(int size) {
            return new MovieResponse[size];
        }
    };
}
