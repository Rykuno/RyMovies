package com.rykuno.rymovies.Objects.EventObjects;

import java.util.ArrayList;

/**
 * Created by rykuno on 10/14/16.
 */

public class TrailerEvent {
    private ArrayList<String> mTrailerArrayList ;

    public TrailerEvent(ArrayList<String> trailerArrayList) {
        mTrailerArrayList = trailerArrayList;
    }

    public ArrayList<String> getTrailerArrayList() {
        return mTrailerArrayList;
    }
}
