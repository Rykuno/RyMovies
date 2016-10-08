package com.rykuno.rymovies.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rykuno.rymovies.Adapters.MovieTrailerAdapter;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

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
    MovieTrailerAdapter mAdapter;
    @BindView(R.id.reviews_button)
    Button review_button;

    private ArrayList<String> mTrailerArrayList = new ArrayList();

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        mAdapter = new MovieTrailerAdapter(getActivity(), mTrailerArrayList);
        mTrailer_gridView.setAdapter(mAdapter);
        setUIData();



        if (savedInstanceState == null) {
            fetchDetailData();
        }

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

        return rootView;
    }

    private void fetchDetailData(){
        String baseUrl = getString(R.string.movie_base_url)+ String.valueOf(mCurrentMovie.getId()) + "/videos?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc&language=en-US";
        ApiRequest apiRequest = new ApiRequest(getActivity(), "trailer");
        apiRequest.fetchData(baseUrl);
    }
    private void setUIData() {
        Intent intent = getActivity().getIntent();
        mCurrentMovie = intent.getParcelableExtra(getString(R.string.movie_key));
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780/" + mCurrentMovie.getBackdrop()).into(mBackdrop_imageView);
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + mCurrentMovie.getPoster()).into(mPoster_imageView);
        mTitle_textView.setText(mCurrentMovie.getTitle());
        mPlot_textView.setText(mCurrentMovie.getPlot());
        mReleased_textView.setText(mCurrentMovie.getReleaseDate());
        DecimalFormat df = new DecimalFormat("#.#");
        mRating_textView.setText(String.valueOf(df.format(mCurrentMovie.getRating())));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ArrayList<String> event) throws JSONException {
        mTrailerArrayList.clear();
        mTrailerArrayList.addAll(event);
        mAdapter.notifyDataSetChanged();
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
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }
}
