package com.rykuno.rymovies.models.eventBusModels;

import java.util.ArrayList;


public class TrailerEvent {
    private ArrayList<String> mTrailerArrayList ;

    public TrailerEvent(ArrayList<String> trailerArrayList) {
        mTrailerArrayList = trailerArrayList;
    }

    public ArrayList<String> getTrailerArrayList() {
        return mTrailerArrayList;
    }
}
