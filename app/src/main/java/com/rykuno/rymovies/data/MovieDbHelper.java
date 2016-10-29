package com.rykuno.rymovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;

public class MovieDbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " ("
                + FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_TITLE + " TEXT NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_PLOT + " TEXT NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_RATING + " TEXT NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE + " REAL NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_POSTER + " Text NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP + " Text NOT NULL, "
                + FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID + " INTEGER UNIQUE NOT NULL);";

        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
