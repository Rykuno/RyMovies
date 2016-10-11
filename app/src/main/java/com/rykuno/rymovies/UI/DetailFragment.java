package com.rykuno.rymovies.UI;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rykuno.rymovies.Adapters.MovieTrailerAdapter;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {
    private static final String TRAILER_KEY = "TRAILER_KEY";
    private Movie mCurrentMovie;
    private ApiRequest mApiRequest;
    private MovieTrailerAdapter mAdapter;
    private Uri mCurrentMovieUri;
    private ArrayList<String> mTrailerArrayList = new ArrayList();

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
    @BindView(R.id.rating_textView)
    TextView mRating_textView;
    @BindView(R.id.trailer_gridview)
    GridView mTrailer_gridView;
    @BindView(R.id.reviews_button)
    Button review_button;
    @BindView(R.id.favorite_button)
    Button favorite_button;


    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        if (getArguments() != null)
            mCurrentMovie = getArguments().getParcelable("ARGUMENTS");

        setUIData();

        if (savedInstanceState == null && mCurrentMovie != null) {
            fetchTrailerData();
        } else if (savedInstanceState != null) {
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
    public void onEvent(ArrayList<String> event) throws JSONException {
        if (!event.isEmpty() && event.get(0) instanceof String) {
            mTrailerArrayList.clear();
            mTrailerArrayList.addAll(event);
            mAdapter.notifyDataSetChanged();
        }
    }


    private void setUIData() {
        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Intent intent = getActivity().getIntent();
            mCurrentMovie = intent.getParcelableExtra(getString(R.string.movie_key));
        }
        if (mCurrentMovie != null) {

            if (mCurrentMovie.getPoster() != null && mCurrentMovie.getBackdrop() != null) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780/" + mCurrentMovie.getBackdrop()).into(mBackdrop_imageView);
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + mCurrentMovie.getPoster()).into(mPoster_imageView);
            } else {
                Bitmap posterBitmap = BitmapFactory.decodeByteArray(mCurrentMovie.getPosterByte(), 0, mCurrentMovie.getPosterByte().length);
                Bitmap backdropBitmap = BitmapFactory.decodeByteArray(mCurrentMovie.getBackdropByte(), 0, mCurrentMovie.getBackdropByte().length);
                mPoster_imageView.setImageBitmap(posterBitmap);
                mBackdrop_imageView.setImageBitmap(backdropBitmap);
            }
            mTitle_textView.setText(mCurrentMovie.getTitle());
            mPlot_textView.setText(mCurrentMovie.getPlot());
            mReleased_textView.setText(mCurrentMovie.getReleaseDate());
            DecimalFormat df = new DecimalFormat("#.#");
            mRating_textView.setText(String.valueOf(df.format(mCurrentMovie.getRating())));

            mAdapter = new MovieTrailerAdapter(getActivity(), mTrailerArrayList);
            mTrailer_gridView.setAdapter(mAdapter);

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
                    byte[] poster = getBitmapAsByteArray(mPoster_imageView.getDrawingCache());
                    byte[] backdrop = getBitmapAsByteArray(mBackdrop_imageView.getDrawingCache());


                    ContentValues values = new ContentValues();
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE, title);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT, plot);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE, releaseDate);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_RATING, rating);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID, id);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER, poster);
                    values.put(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP, backdrop);

                    Uri newUri = getActivity().getContentResolver().insert(FavoriteMovieEntry.CONTENT_URI, values);
                    if (newUri == null) {
                        Toast.makeText(getActivity(), "Already Favorited", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Favorited", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(TRAILER_KEY, mTrailerArrayList);
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
}
