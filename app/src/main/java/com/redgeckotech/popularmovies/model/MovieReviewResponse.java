package com.redgeckotech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieReviewResponse implements Parcelable {

    @SerializedName("page") protected int page;
    @SerializedName("total_pages") protected int totalPages;
    @SerializedName("total_results") protected int totalResults;
    @SerializedName("results") protected List<MovieReview> movieReviews;

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

    public List<MovieReview> getMovieReviews() {
        return movieReviews;
    }

    @Override
    public String toString() {
        return "MovieReviewResponse{" +
                "page=" + page +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                ", movieReviews=" + movieReviews +
                '}';
    }


    public MovieReviewResponse() {
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
        dest.writeTypedList(movieReviews);
    }

    protected MovieReviewResponse(Parcel in) {
        this.page = in.readInt();
        this.totalPages = in.readInt();
        this.totalResults = in.readInt();
        this.movieReviews = in.createTypedArrayList(MovieReview.CREATOR);
    }

    public static final Creator<MovieReviewResponse> CREATOR = new Creator<MovieReviewResponse>() {
        @Override
        public MovieReviewResponse createFromParcel(Parcel source) {
            return new MovieReviewResponse(source);
        }

        @Override
        public MovieReviewResponse[] newArray(int size) {
            return new MovieReviewResponse[size];
        }
    };
}
