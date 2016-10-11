package com.rykuno.rymovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;

/**
 * Created by rykuno on 10/11/16.
 */

public class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private MovieDbHelper mDbHelper;
    private static final int MOVIE = 0;
    private static final int MOVIE_ID = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sUriMatcher.addURI(MovieDbContract.CONTENT_AUTHORITY, MovieDbContract.PATH_FAVORITES, MOVIE);
        sUriMatcher.addURI(MovieDbContract.CONTENT_AUTHORITY, MovieDbContract.PATH_FAVORITES + "/#", MOVIE_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case MOVIE :
                cursor = database.query(
                        FavoriteMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_ID :
                selection = FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        FavoriteMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default :
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MOVIE :
                return insertMovie(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertMovie(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(FavoriteMovieEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case MOVIE:
                rowsDeleted = database.delete(FavoriteMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                selection = FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(FavoriteMovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
