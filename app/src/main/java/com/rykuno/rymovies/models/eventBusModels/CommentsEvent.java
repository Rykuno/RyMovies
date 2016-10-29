package com.rykuno.rymovies.models.eventBusModels;

import com.rykuno.rymovies.models.Comment;

import java.util.ArrayList;

public class CommentsEvent {
    private ArrayList<Comment> mCommentArrayList;

    public CommentsEvent(ArrayList<Comment> commentArrayList) {
        mCommentArrayList = commentArrayList;
    }

    public ArrayList<Comment> getCommentArrayList() {
        return mCommentArrayList;
    }
}
