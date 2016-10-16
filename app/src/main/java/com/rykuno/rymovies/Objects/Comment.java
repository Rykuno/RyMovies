package com.rykuno.rymovies.Objects;

/**
 * Created by rykuno on 10/8/16.
 */


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rykuno on 3/16/16.
 */
public class Comment implements Parcelable {
    private String mAuthor;
    private String mComment;

    public Comment(String author, String comment) {
        mAuthor = author;
        mComment = comment;
    }

    public Comment(Parcel in) {
        mAuthor = in.readString();
        mComment = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getAuthor() {
        return mAuthor;
    }


    public String getComment() {
        return mComment;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthor);
        dest.writeString(mComment);
    }
}