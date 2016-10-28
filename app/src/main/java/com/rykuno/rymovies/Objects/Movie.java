package com.rykuno.rymovies.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rykuno on 10/6/16.
 */

public class Movie implements Parcelable {
    private String mTitle;
    private String mPlot;
    private String mPoster;
    private double mRating;
    private String mReleaseDate;
    private String mBackdrop;
    private int mId;

    public Movie(String title, String plot, String poster, double rating, String releaseDate, String backdrop, int id) {
        mTitle = title;
        mPlot = plot;
        mPoster = poster;
        mRating = rating;
        mReleaseDate = releaseDate;
        mBackdrop = backdrop;
        mId = id;
    }

    protected Movie(Parcel in) {
        mTitle = in.readString();
        mPlot = in.readString();
        mPoster = in.readString();
        mRating = in.readDouble();
        mReleaseDate = in.readString();
        mBackdrop = in.readString();
        mId = in.readInt();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getTitle() {
        return mTitle;
    }

    public String getPlot() {
        return mPlot;
    }

    public String getPoster() {
        return mPoster;
    }

    public double getRating() {
        return mRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getBackdrop() {
        return mBackdrop;
    }

    public int getId() {
        return mId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mPlot);
        dest.writeString(mPoster);
        dest.writeDouble(mRating);
        dest.writeString(mReleaseDate);
        dest.writeString(mBackdrop);
        dest.writeInt(mId);
    }
}
