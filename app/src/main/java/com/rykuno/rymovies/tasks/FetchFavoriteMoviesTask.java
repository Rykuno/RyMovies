package com.rykuno.rymovies.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.rykuno.rymovies.adapters.MovieGridAdapter;
import com.rykuno.rymovies.data.MovieDbContract;
import com.rykuno.rymovies.models.Movie;

import java.util.ArrayList;

public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

    private Context mContext;
    private MovieGridAdapter mAdapter;

    public FetchFavoriteMoviesTask(Context context, MovieGridAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    private ArrayList<Movie> fetchFavoritesFromCursor(Cursor cursor) {
        ArrayList<Movie> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            int titleColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_TITLE);
            int plotColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_PLOT);
            int releaseDateColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_RATING);
            int movieIdColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID);
            int posterColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_POSTER);
            int backdropColumnIndex = cursor.getColumnIndex(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP);

            int movieId = cursor.getInt(movieIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String plot = cursor.getString(plotColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            double rating = cursor.getDouble(ratingColumnIndex);
            String poster = cursor.getString(posterColumnIndex);
            String backdrop = cursor.getString(backdropColumnIndex);
            results.add(new Movie(title, plot, poster, rating, releaseDate, backdrop, movieId));
        }
        cursor.close();
        return results;
    }

    @Override
    protected ArrayList<Movie> doInBackground(Void... params) {
        Cursor cursor = mContext.getContentResolver().query(MovieDbContract.FavoriteMovieEntry.CONTENT_URI, null, null, null, null);
        return fetchFavoritesFromCursor(cursor);
    }

    @Override
    protected void onPostExecute(ArrayList<Movie> movies) {
        mAdapter.clear();
        mAdapter.addAll(movies);
        mAdapter.notifyDataSetChanged();
    }
}
