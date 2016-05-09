package com.redgeckotech.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RelatedVideo implements Parcelable {

    @SerializedName("id") protected String id;
    @SerializedName("iso_3166_1") protected String iso_3166_1;
    @SerializedName("iso_639_1") protected String iso_639_1;
    @SerializedName("key") protected String key;
    @SerializedName("name") protected String name;
    @SerializedName("site") protected String site;
    @SerializedName("size") protected int size;
    @SerializedName("type") protected String type;

    public RelatedVideo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public void setIso_3166_1(String iso_3166_1) {
        this.iso_3166_1 = iso_3166_1;
    }

    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String iso_639_1) {
        this.iso_639_1 = iso_639_1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "RelatedVideo{" +
                "id='" + id + '\'' +
                ", iso_3166_1='" + iso_3166_1 + '\'' +
                ", iso_639_1='" + iso_639_1 + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", site='" + site + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.iso_3166_1);
        dest.writeString(this.iso_639_1);
        dest.writeString(this.key);
        dest.writeString(this.name);
        dest.writeString(this.site);
        dest.writeInt(this.size);
        dest.writeString(this.type);
    }

    protected RelatedVideo(Parcel in) {
        this.id = in.readString();
        this.iso_3166_1 = in.readString();
        this.iso_639_1 = in.readString();
        this.key = in.readString();
        this.name = in.readString();
        this.site = in.readString();
        this.size = in.readInt();
        this.type = in.readString();
    }

    public static final Creator<RelatedVideo> CREATOR = new Creator<RelatedVideo>() {
        @Override
        public RelatedVideo createFromParcel(Parcel source) {
            return new RelatedVideo(source);
        }

        @Override
        public RelatedVideo[] newArray(int size) {
            return new RelatedVideo[size];
        }
    };
}
