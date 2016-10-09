package com.rykuno.rymovies.UI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {
    private static final String TRAILER_KEY = "TRAILER_KEY";
   // private Movie mCurrentMovie = new Movie("Forrest Gump", "A man with a low IQ has accomplished great things in his life and been present during significant historic events - in each case, far exceeding what anyone imagined he could do. Yet, despite all the things he has attained, his one true love eludes him. 'Forrest Gump' is the story of a man who rose above his challenges, and who proved that determination, courage, and love are more important than ability.", "/z4ROnCrL77ZMzT0MsNXY5j25wS2.jpg", 8, "1994-07-06", "/ctOEhQiFIHWkiaYp7b0ibSTe5IL.jpg", 13);
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
    @BindView(R.id.reviews_button)
    Button review_button;


    private MovieTrailerAdapter mAdapter;
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

        if (getArguments() != null){
            mCurrentMovie = getArguments().getParcelable("ARGUMENTS");
            Log.v("ARGUMENTS", mCurrentMovie.getReleaseDate());
        }

        setUIData();

        if (savedInstanceState == null && mCurrentMovie != null) {
            String baseUrl = getString(R.string.movie_base_url) + String.valueOf(mCurrentMovie.getId()) + "/videos?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc&language=en-US";
            fetchData(baseUrl);
        }else if (savedInstanceState != null){
            mTrailerArrayList = savedInstanceState.getStringArrayList(TRAILER_KEY);
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

    public void fetchData(String url) {
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(getActivity(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String mJsonData = response.body().string();
                    try {
                        mTrailerArrayList.addAll(getTrailerJsonDetails(mJsonData));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Network Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getTrailerJsonDetails(String jsonData) throws JSONException {
        ArrayList<String> videoKeyArrayList = new ArrayList<>();
        JSONObject root = new JSONObject(jsonData);
        JSONArray results = root.getJSONArray("results");
        for (int i = 0; i<results.length(); i++) {
            JSONObject keyResults = results.getJSONObject(i);
            String youtubeKey = keyResults.getString("key");
            videoKeyArrayList.add(youtubeKey);
        }
        return videoKeyArrayList;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void setUIData() {
        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            Intent intent = getActivity().getIntent();
            mCurrentMovie = intent.getParcelableExtra(getString(R.string.movie_key));
        }
        if (mCurrentMovie != null) {
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w780/" + mCurrentMovie.getBackdrop()).into(mBackdrop_imageView);
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + mCurrentMovie.getPoster()).into(mPoster_imageView);
            mTitle_textView.setText(mCurrentMovie.getTitle());
            mPlot_textView.setText(mCurrentMovie.getPlot());
            mReleased_textView.setText(mCurrentMovie.getReleaseDate());
            DecimalFormat df = new DecimalFormat("#.#");
            mRating_textView.setText(String.valueOf(df.format(mCurrentMovie.getRating())));
        }
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
}
