package com.rykuno.rymovies.models.eventBusModels;

import com.rykuno.rymovies.models.Movie;

import java.util.ArrayList;


public class MovieEvent {
    private ArrayList<Movie> mMovieArrayList;

    public MovieEvent(ArrayList<Movie> movieArrayList) {
        mMovieArrayList = movieArrayList;
    }

    public ArrayList<Movie> getMovieArrayList() {
        return mMovieArrayList;
    }
}
