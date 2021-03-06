package com.rykuno.rymovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieDbContract {
    public static final String CONTENT_AUTHORITY = "com.rykuno.rymovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public MovieDbContract() {
    }

    public static final class FavoriteMovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITES);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static final String TABLE_NAME = "favorites";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FAVORITES_TITLE = "title";
        public static final String COLUMN_FAVORITES_PLOT = "plot";
        public static final String COLUMN_FAVORITES_POSTER = "poster";
        public static final String COLUMN_FAVORITES_RATING = "rating";
        public static final String COLUMN_FAVORITES_RELEASE_DATE = "release_date";
        public static final String COLUMN_FAVORITES_BACKDROP = "backdrop";
        public static final String COLUMN_FAVORITES_MOVIE_ID = "movie_id";
    }

}

