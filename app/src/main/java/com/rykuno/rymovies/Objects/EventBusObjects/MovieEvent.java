package com.rykuno.rymovies.objects.eventBusObjects;

import com.rykuno.rymovies.objects.Movie;

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
