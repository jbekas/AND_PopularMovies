package com.redgeckotech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RelatedVideosResponse implements Parcelable {

    @SerializedName("page") protected int page;
    @SerializedName("results") protected List<RelatedVideo> relatedVideos;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<RelatedVideo> getRelatedVideos() {
        return relatedVideos;
    }

    public void setRelatedVideos(List<RelatedVideo> relatedVideos) {
        this.relatedVideos = relatedVideos;
    }

    @Override
    public String toString() {
        return "RelatedVideosResponse{" +
                "page=" + page +
                ", relatedVideos=" + relatedVideos +
                '}';
    }


    public RelatedVideosResponse() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeTypedList(relatedVideos);
    }

    protected RelatedVideosResponse(Parcel in) {
        this.page = in.readInt();
        this.relatedVideos = in.createTypedArrayList(RelatedVideo.CREATOR);
    }

    public static final Creator<RelatedVideosResponse> CREATOR = new Creator<RelatedVideosResponse>() {
        @Override
        public RelatedVideosResponse createFromParcel(Parcel source) {
            return new RelatedVideosResponse(source);
        }

        @Override
        public RelatedVideosResponse[] newArray(int size) {
            return new RelatedVideosResponse[size];
        }
    };
}
