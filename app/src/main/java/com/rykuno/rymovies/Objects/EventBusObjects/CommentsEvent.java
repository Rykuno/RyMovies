package com.rykuno.rymovies.Objects.EventBusObjects;

import com.rykuno.rymovies.Objects.Comment;

import java.util.ArrayList;

/**
 * Created by rykuno on 10/14/16.
 */

public class CommentsEvent {
    private ArrayList<Comment> mCommentArrayList;

    public CommentsEvent(ArrayList<Comment> commentArrayList) {
        mCommentArrayList = commentArrayList;
    }

    public ArrayList<Comment> getCommentArrayList() {
        return mCommentArrayList;
    }
}
