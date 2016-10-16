package com.rykuno.rymovies.UI;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.rykuno.rymovies.BuildConfig;
import com.rykuno.rymovies.Objects.EventBusObjects.TrailerEvent;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.rykuno.rymovies.data.MovieDbContract;
import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;
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
    private View rootView;
    private ArrayList<String> mTrailerArrayList = new ArrayList();
    private boolean mMovieFavorited;

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
    @BindView(R.id.share_button)
    Button mShare_button;
    @BindView(R.id.reviews_button)
    Button mReview_button;
    @BindView(R.id.favorite_button)
    ImageView mFavorite_button;
    @BindView(R.id.movieRating)
    RatingBar mMovieRating_ratingBar;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

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
        String baseUrl = getString(R.string.movie_base_url) + String.valueOf(mCurrentMovie.getId()) + "/videos?api_key=" + BuildConfig.MY_MOVIE_DB_API_KEY;
        mApiRequest = new ApiRequest(getActivity());
        mApiRequest.fetchData(baseUrl, getString(R.string.trailer));
    }

    /**
     * Sets UI fields and onClickListeners
     */
    private void setUIData() {
        //If the movie is passed through an Intent(I.E not tablet mode) set the current movie to the intent extra
        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Intent intent = getActivity().getIntent();
            mCurrentMovie = intent.getParcelableExtra(getString(R.string.movie_key));
        }

        //If the movie is passed through an Argument(I.E. tablet mode) set the current movie to the arguemtents value.
        if (getArguments() != null) {
            mCurrentMovie = getArguments().getParcelable(getString(R.string.arguments));
            rootView.setVisibility(View.VISIBLE);
        } else if (getArguments() == null && getActivity().getIntent().getExtras() == null)
            rootView.setVisibility(View.INVISIBLE);


        //Set the poster and backdrop images depending on what data we have passed in(I.E, file name to retreive from or URL)
        if (mCurrentMovie != null) {
            if (!mCurrentMovie.getPoster().contains(getString(R.string.imageDir)) && !mCurrentMovie.getBackdrop().contains(getString(R.string.imageDir))) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780/" + mCurrentMovie.getBackdrop()).into(mBackdrop_imageView);
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + mCurrentMovie.getPoster()).into(mPoster_imageView);
            } else {
                mPoster_imageView.setImageBitmap(loadImageFromStorage(String.valueOf(mCurrentMovie.getId()) + getString(R.string.poster)));
                mBackdrop_imageView.setImageBitmap(loadImageFromStorage(String.valueOf(mCurrentMovie.getId()) + getString(R.string.backdrop)));
            }

            mTitle_textView.setText(mCurrentMovie.getTitle());
            mPlot_textView.setText(mCurrentMovie.getPlot());
            mReleased_textView.setText(mCurrentMovie.getReleaseDate());
            mAdapter = new MovieTrailerAdapter(getActivity(), mTrailerArrayList);
            mTrailer_gridView.setAdapter(mAdapter);
            mMovieRating_ratingBar.setRating((float) (mCurrentMovie.getRating() / 2));

            mReview_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(getString(R.string.movie_id), mCurrentMovie.getId());
                    CommentsFragment commentsFragment = new CommentsFragment();
                    commentsFragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, commentsFragment).addToBackStack(null).commit();
                }
            });

            mShare_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this Movie! : " + mCurrentMovie.getTitle() + "\n https://www.youtube.com/watch?v=" + mTrailerArrayList.get(0).toString());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.send_to)));
                }
            });

            mTrailer_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String keyPosition = mTrailerArrayList.get(position).toString();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + keyPosition)));
                }
            });

            isMovieFavorited();
            configureFavoritesButton();

        }
    }

    /**
     * Determiens if the current movie is favorited
     * through querying the database for a match of the movie ID.
     */
    private void isMovieFavorited() {
        Uri uri = ContentUris.withAppendedId(MovieDbContract.FavoriteMovieEntry.CONTENT_URI, mCurrentMovie.getId());
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() > 0)
            mMovieFavorited = true;
        else
            mMovieFavorited = false;

        cursor.close();
        configureFavoritesButton();
    }

    /**
     * Decides which action the favorites button will
     * take depending on the favorited state of the movie.
     */
    private void configureFavoritesButton() {
        if (mMovieFavorited == false) {
            mFavorite_button.setImageResource(android.R.drawable.star_big_off);
            mFavorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = mCurrentMovie.getTitle();
                    String plot = mCurrentMovie.getPlot();
                    String releaseDate = mCurrentMovie.getReleaseDate();
                    double rating = mCurrentMovie.getRating();
                    int id = mCurrentMovie.getId();
                    mPoster_imageView.buildDrawingCache();
                    mBackdrop_imageView.buildDrawingCache();
                    String poster = saveToInternalStorage(mPoster_imageView.getDrawingCache(), getString(R.string.poster));
                    String backdrop = saveToInternalStorage(mBackdrop_imageView.getDrawingCache(), getString(R.string.backdrop));

                    ContentValues values = new ContentValues();
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE, title);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT, plot);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE, releaseDate);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RATING, rating);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID, id);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER, poster);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP, backdrop);

                    Uri newUri = getActivity().getContentResolver().insert(FavoriteMovieEntry.CONTENT_URI, values);
                    mFavorite_button.setImageResource(android.R.drawable.star_big_on);
                    isMovieFavorited();
                }
            });
        }

        if (mMovieFavorited == true) {
            mFavorite_button.setImageResource(android.R.drawable.star_big_on);
            mFavorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(mCurrentMovie);
                    mFavorite_button.setImageResource(android.R.drawable.star_big_off);
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


    /**
     * EventBus callback for populating the trailer's gridview.
     * @param event : ArrayList passed from ApiRequest class after retrieving data.
     * @throws JSONException
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TrailerEvent event) throws JSONException {
        mTrailerArrayList.clear();
        mTrailerArrayList.addAll(event.getTrailerArrayList());
        mAdapter.notifyDataSetChanged();
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

    /**
     * Saves the poster/backdrop images to internal storage for offline favorites viewing.
     *
     * @param bitmapImage : poster from mCurrentMovie.
     * @param identifier  : pass in "poster" or "backdrop" to identify  files from each other.
     * @return the String directory to where the file is stored.
     */
    private String saveToInternalStorage(Bitmap bitmapImage, String identifier) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(getString(R.string.imageDir), Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, String.valueOf(mCurrentMovie.getId()) + identifier + ".jpg");
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

    /**
     * Loads the bitmap of the image of whos file path is passed in.
     *
     * @param path : pass the path of the image to retrieve.
     * @return Bitmap of the image to restore.
     */
    private Bitmap loadImageFromStorage(String path) {

        try {
            File f = new File("/data/user/0/com.rykuno.rymovies/app_imageDir", path + ".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }


}
