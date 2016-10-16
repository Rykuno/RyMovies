package com.rykuno.rymovies.Objects.EventObjects;

import com.rykuno.rymovies.Objects.Movie;

import java.util.ArrayList;

/**
 * Created by rykuno on 10/14/16.
 */

public class MovieEvent {
    private ArrayList<Movie> mMovieArrayList;

    public MovieEvent(ArrayList<Movie> movieArrayList) {
        mMovieArrayList = movieArrayList;
    }

    public ArrayList<Movie> getMovieArrayList() {
        return mMovieArrayList;
    }
}
