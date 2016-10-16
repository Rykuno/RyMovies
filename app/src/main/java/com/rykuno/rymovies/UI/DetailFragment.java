package com.rykuno.rymovies.UI;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.rykuno.rymovies.Adapters.MovieTrailerAdapter;
import com.rykuno.rymovies.Objects.EventObjects.TrailerEvent;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;
import com.rykuno.rymovies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {
    private static final String TRAILER_KEY = "TRAILER_KEY";
    private static final String MOVIE_KEY = "MOVIE_KEY";
    private Movie mCurrentMovie;
    private ApiRequest mApiRequest;
    private MovieTrailerAdapter mAdapter;
    private ArrayList<String> mTrailerArrayList = new ArrayList();
    private boolean movieFavorited;

    @BindView(R.id.backdrop_imageView)
    ImageView mBackdrop_imageView;
    @BindView(R.id.poster_imageView)
    ImageView mPoster_imageView;
    @BindView(R.id.title_textView)
    TextView mTitle_textView;
    @BindView(R.id.plot_textView)
    TextView mPlot_textView;
    @BindView(R.id.released_textView)
    TextView mReleased_textView;
    @BindView(R.id.trailer_gridview)
    GridView mTrailer_gridView;
    @BindView(R.id.reviews_button)
    Button review_button;
    @BindView(R.id.favorite_button)
    Button favorite_button;
    @BindView(R.id.movieRating)
    RatingBar movieRating_ratingBar;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        if (getArguments() != null)
            mCurrentMovie = getArguments().getParcelable("ARGUMENTS");
        else if (getArguments() == null && getActivity().getIntent().getExtras() == null)
            rootView.setVisibility(View.INVISIBLE);

        if (savedInstanceState != null) {
            mCurrentMovie = savedInstanceState.getParcelable(MOVIE_KEY);
            rootView.setVisibility(View.VISIBLE);
        }

        setUIData();

        if (savedInstanceState == null && mCurrentMovie != null)
            fetchTrailerData();
        else if (savedInstanceState != null) {
            mTrailerArrayList = savedInstanceState.getStringArrayList(TRAILER_KEY);
        }

        return rootView;
    }


    private void fetchTrailerData() {
        String baseUrl = getString(R.string.movie_base_url) + String.valueOf(mCurrentMovie.getId()) + "/videos?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc&language=en-US";
        mApiRequest = new ApiRequest(getActivity());
        mApiRequest.fetchData(baseUrl, "trailer");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TrailerEvent event) throws JSONException {
        mTrailerArrayList.clear();
        mTrailerArrayList.addAll(event.getTrailerArrayList());
        mAdapter.notifyDataSetChanged();
    }

    private void setUIData() {

        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Intent intent = getActivity().getIntent();
            mCurrentMovie = intent.getParcelableExtra(getString(R.string.movie_key));
        }

        if (mCurrentMovie != null) {
            if (!mCurrentMovie.getPoster().contains("imageDir") && !mCurrentMovie.getBackdrop().contains("imageDir")) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780/" + mCurrentMovie.getBackdrop()).into(mBackdrop_imageView);
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + mCurrentMovie.getPoster()).into(mPoster_imageView);
            } else {
                mPoster_imageView.setImageBitmap(loadImageFromStorage(String.valueOf(mCurrentMovie.getId()) + "poster"));
                mBackdrop_imageView.setImageBitmap(loadImageFromStorage(String.valueOf(mCurrentMovie.getId()) + "backdrop"));
            }

            DecimalFormat df = new DecimalFormat("#.#");

            mTitle_textView.setText(mCurrentMovie.getTitle());
            mPlot_textView.setText(mCurrentMovie.getPlot());
            mReleased_textView.setText(mCurrentMovie.getReleaseDate());
            mAdapter = new MovieTrailerAdapter(getActivity(), mTrailerArrayList);
            mTrailer_gridView.setAdapter(mAdapter);
            movieRating_ratingBar.setRating(Float.valueOf(df.format(mCurrentMovie.getRating() / 2)));

            review_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle arguments = new Bundle();
                    arguments.putInt("MOVIE_ID", mCurrentMovie.getId());
                    CommentsFragment commentsFragment = new CommentsFragment();
                    commentsFragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, commentsFragment).addToBackStack("detail").commit();
                }
            });

            mTrailer_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String keyPosition = mTrailerArrayList.get(position).toString();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + keyPosition)));
                }
            });

            configureFavoritesButton();

        }
    }

    private void isMovieFavorited() {
        MovieDbHelper dbHelper = new MovieDbHelper(getActivity());
        SQLiteDatabase sqldb = dbHelper.getReadableDatabase();
        String Query = "Select * from " + FavoriteMovieEntry.TABLE_NAME + " where " + FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID + " = " + mCurrentMovie.getId();
        Cursor cursor = sqldb.rawQuery(Query, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            dbHelper.close();
            sqldb.close();
            movieFavorited = true;
        } else {
            cursor.close();
            dbHelper.close();
            sqldb.close();
            movieFavorited = false;
        }
        configureFavoritesButton();

    }

    private void configureFavoritesButton() {
        if (movieFavorited == false) {
            favorite_button.setText("Favorite");
            favorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = mCurrentMovie.getTitle();
                    String plot = mCurrentMovie.getPlot();
                    String releaseDate = mCurrentMovie.getReleaseDate();
                    double rating = mCurrentMovie.getRating();
                    int id = mCurrentMovie.getId();
                    mPoster_imageView.buildDrawingCache();
                    mBackdrop_imageView.buildDrawingCache();
                    String poster = saveToInternalStorage(mPoster_imageView.getDrawingCache(), "poster");
                    String backdrop = saveToInternalStorage(mBackdrop_imageView.getDrawingCache(), "backdrop");

                    ContentValues values = new ContentValues();
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE, title);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT, plot);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE, releaseDate);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RATING, rating);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID, id);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER, poster);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP, backdrop);

                    Uri newUri = getActivity().getContentResolver().insert(FavoriteMovieEntry.CONTENT_URI, values);
                    favorite_button.setText("Unfavorite");
                    isMovieFavorited();
                }
            });
        }

        if (movieFavorited == true) {
            favorite_button.setText("Unfavorite");
            favorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(mCurrentMovie);
                    favorite_button.setText("Favorite");
                    isMovieFavorited();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(TRAILER_KEY, mTrailerArrayList);
        outState.putParcelable(MOVIE_KEY, mCurrentMovie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null)
            mTrailerArrayList = savedInstanceState.getStringArrayList(TRAILER_KEY);
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String identifier) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, String.valueOf(mCurrentMovie.getId()) + identifier + ".jpg");
        Log.e(mypath.toString(), " PATH TO FILE");
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

    private Bitmap loadImageFromStorage(String identifier) {

        try {
            File f = new File("/data/user/0/com.rykuno.rymovies/app_imageDir", identifier + ".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }


}
