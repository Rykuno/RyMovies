package com.rykuno.rymovies.ui;

import android.content.Intent;
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

import com.rykuno.rymovies.BuildConfig;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.adapters.MovieTrailerAdapter;
import com.rykuno.rymovies.models.Movie;
import com.rykuno.rymovies.models.eventBusModels.TrailerEvent;
import com.rykuno.rymovies.services.ApiRequest;
import com.rykuno.rymovies.tasks.ManageFavoriteMovieTask;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailFragment extends Fragment {
    private Movie mCurrentMovie;
    private MovieTrailerAdapter mAdapter;
    private View rootView;
    private ArrayList<String> mTrailerArrayList;

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
        mTrailerArrayList = new ArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        setUIData();
        if (mCurrentMovie != null)
            fetchTrailerData();
        if (mCurrentMovie != null)
            rootView.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void fetchTrailerData() {
        String url = getString(R.string.trailers_url, String.valueOf(mCurrentMovie.getId()), BuildConfig.MY_MOVIE_DB_API_KEY);
        new ApiRequest(getActivity()).fetchData(url, getString(R.string.trailer));
    }

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
        } else if (getArguments() == null && getActivity().getIntent().getExtras() == null) {
            rootView.setVisibility(View.INVISIBLE);
        }

        //Set the poster and backdrop images depending on what data we have passed in(I.E, file name to retreive from or URL)
        if (mCurrentMovie != null) {
            //checks the favorited state of the movie
            new ManageFavoriteMovieTask(getActivity(), mCurrentMovie, false, mFavorite_button, mPoster_imageView, mBackdrop_imageView).execute();

            if (!mCurrentMovie.getPoster().contains(getString(R.string.imageDir)) && !mCurrentMovie.getBackdrop().contains(getString(R.string.imageDir))) {
                Picasso.with(getActivity()).load(getString(R.string.current_poster_w780_url, mCurrentMovie.getBackdrop())).into(mBackdrop_imageView);
                Picasso.with(getActivity()).load(getString(R.string.current_poster_w342_url, mCurrentMovie.getPoster())).into(mPoster_imageView);
            } else {
                mPoster_imageView.setImageBitmap(loadImageFromStorage(getString(R.string.poster_save_file, mCurrentMovie.getId())));
                mBackdrop_imageView.setImageBitmap(loadImageFromStorage(getString(R.string.backdrop_save_file, mCurrentMovie.getId())));
            }

            mTitle_textView.setText(mCurrentMovie.getTitle());
            mPlot_textView.setText(getString(R.string.get_tabbed_text, mCurrentMovie.getPlot()));
            mReleased_textView.setText(mCurrentMovie.getReleaseDate());
            mMovieRating_ratingBar.setRating((float) (mCurrentMovie.getRating() / 2));
            mAdapter = new MovieTrailerAdapter(getActivity(), mTrailerArrayList);
            mTrailer_gridView.setAdapter(mAdapter);

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

                    if (!mTrailerArrayList.isEmpty())
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_trailer, mCurrentMovie.getTitle(), mTrailerArrayList.get(0)));
                    else
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_movie, mCurrentMovie.getTitle()));

                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.send_to)));
                }
            });

            mTrailer_gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String keyPosition = mTrailerArrayList.get(position);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.view_trailer, keyPosition))));
                }
            });

            mFavorite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManageFavoriteMovieTask manageFavoriteMovieTask = new ManageFavoriteMovieTask(getActivity(), mCurrentMovie, true, mFavorite_button, mPoster_imageView, mBackdrop_imageView);
                    manageFavoriteMovieTask.execute();
                    EventBus.getDefault().post(mCurrentMovie);
                }
            });
        }
    }

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
