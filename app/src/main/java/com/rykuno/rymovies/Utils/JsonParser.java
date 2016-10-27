package com.rykuno.rymovies.utils;

import android.util.Log;

import com.rykuno.rymovies.objects.Comment;
import com.rykuno.rymovies.objects.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rykuno on 10/8/16.
 */

public class JsonParser {

    public JsonParser() {

    }

    public ArrayList parseJsonData(String code, String jsonData) throws JSONException {
        switch (code) {
            case "poster":
                return getPosterJsonDetails(jsonData);
            case "trailer":
                return getTrailerJsonDetails(jsonData);
            case "comments":
                return getCommentsJsonDetails(jsonData);
        }
        return null;
    }

    private ArrayList<Movie> getPosterJsonDetails(String jsonData) throws JSONException {
        ArrayList<Movie> moviesArrayList = new ArrayList<>();
        JSONObject movieDetails = new JSONObject(jsonData);
        JSONArray results = movieDetails.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject movieResult = results.getJSONObject(i);
            String poster = movieResult.getString("poster_path");
            String title = movieResult.getString("original_title");
            String plot = movieResult.getString("overview");
            double rating = movieResult.getDouble("vote_average");
            String releaseDate = movieResult.getString("release_date");
            String backdrop = movieResult.getString("backdrop_path");
            int id = movieResult.getInt("id");
            Log.v("Test", id + "");
            moviesArrayList.add(new Movie(title, plot, poster, rating, releaseDate, backdrop, id));

        }
        return moviesArrayList;
    }

    private ArrayList<String> getTrailerJsonDetails(String jsonData) throws JSONException {
        ArrayList<String> videoKeyArrayList = new ArrayList<>();
        JSONObject root = new JSONObject(jsonData);
        JSONArray results = root.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject keyResults = results.getJSONObject(i);
            String youtubeKey = keyResults.getString("key");
            videoKeyArrayList.add(youtubeKey);
        }
        return videoKeyArrayList;
    }

    private ArrayList<Comment> getCommentsJsonDetails(String jsonData) throws JSONException {
        ArrayList<Comment> commentsArrayList = new ArrayList<>();
        JSONObject movieComments = new JSONObject(jsonData);
        JSONArray results = movieComments.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject commentsResults = results.getJSONObject(i);
            String author = commentsResults.getString("author");
            String comment = commentsResults.getString("content");
            commentsArrayList.add(new Comment(author, comment));
        }
        return commentsArrayList;
    }

}