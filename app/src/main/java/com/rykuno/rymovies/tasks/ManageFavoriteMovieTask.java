package com.rykuno.rymovies.tasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.rykuno.rymovies.R;
import com.rykuno.rymovies.data.MovieDbContract;
import com.rykuno.rymovies.objects.Movie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rykuno on 10/27/16.
 */

public class ManageFavoriteMovieTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private Movie mMovie;
    private ImageView mFavoriteButton;
    private ImageView mPoster;
    private ImageView mBackdrop;
    private boolean isFavorited;
    private boolean mCommitAction = false;
    Bitmap posterCache;
    Bitmap backdropCache;


    public ManageFavoriteMovieTask(Context context, Movie movie, boolean commitAction, ImageView favoriteButton, ImageView poster, ImageView backdrop) {
        mContext = context;
        mMovie = movie;
        mFavoriteButton = favoriteButton;
        mCommitAction = commitAction;
        mPoster = poster;
        mBackdrop = backdrop;
        mPoster.buildDrawingCache();
        mBackdrop.buildDrawingCache();
        posterCache = mPoster.getDrawingCache();
        backdropCache = mBackdrop.getDrawingCache();
    }


    @Override
    protected Void doInBackground(Void... params) {
        Uri uri = ContentUris.withAppendedId(MovieDbContract.FavoriteMovieEntry.CONTENT_URI, mMovie.getId());
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);

        if (mCommitAction) {
            if (cursor.getCount() > 0) {
                int rowsDeleted = mContext.getContentResolver().delete(uri, null, null);
                if (rowsDeleted > 0) {
                    deleteFavoritedImages(mMovie.getBackdrop() + "backdrop");
                    deleteFavoritedImages(mMovie.getPoster() + "poster");
                    isFavorited = false;
                }
            } else {
                String poster = saveToInternalStorage(posterCache, mContext.getString(R.string.poster));
                String backdrop = saveToInternalStorage(backdropCache, mContext.getString(R.string.backdrop));

                ContentValues values = new ContentValues();
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_TITLE, mMovie.getTitle());
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_PLOT, mMovie.getPlot());
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE, mMovie.getReleaseDate());
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_RATING, mMovie.getRating());
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID, mMovie.getId());
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_POSTER, poster);
                values.put(MovieDbContract.FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP, backdrop);
                Uri newUri = mContext.getContentResolver().insert(MovieDbContract.FavoriteMovieEntry.CONTENT_URI, values);
                isFavorited = true;
            }
        } else {
            if (cursor.getCount() > 0) {
                isFavorited = true;
            } else {
                isFavorited = false;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (isFavorited == true) {
            mFavoriteButton.setImageResource(android.R.drawable.star_big_on);
        } else {
            mFavoriteButton.setImageResource(android.R.drawable.star_big_off);
        }
    }

    /**
     * Saves the poster/backdrop images to internal storage for offline favorites viewing.
     *
     * @param bitmapImage : poster from mCurrentMovie.
     * @param identifier  : pass in "poster" or "backdrop" to identify  files from each other.
     * @return the String directory to where the file is stored.
     */
    private String saveToInternalStorage(Bitmap bitmapImage, String identifier) {
        ContextWrapper cw = new ContextWrapper(mContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(mContext.getString(R.string.imageDir), Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, String.valueOf(mMovie.getId()) + identifier + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void deleteFavoritedImages(String path) {
        File f = new File("/data/user/0/com.rykuno.rymovies/app_imageDir", path + ".jpg");
        if (f.exists())
            f.delete();
    }

    public boolean getFavoritedStatus(){
        return isFavorited;
    }
}
