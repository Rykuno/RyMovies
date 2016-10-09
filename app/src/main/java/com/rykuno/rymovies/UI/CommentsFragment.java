package com.rykuno.rymovies.UI;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rykuno.rymovies.Adapters.MovieCommentsAdapter;
import com.rykuno.rymovies.Objects.Comment;
import com.rykuno.rymovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {
    private static final String COMMENTS_KEY = "COMMENTS_KEY";
    private ArrayList<Comment> mCommentArrayList = new ArrayList<>();
    private MovieCommentsAdapter mAdapter;
    private int mMovieIdKey;

    @BindView(R.id.comments_listView)
    ListView mListView;
    @BindView(R.id.comments_emptyView)
    TextView mEmptyView;


    public CommentsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, rootView);
        Bundle bundle = this.getArguments();
        mMovieIdKey = bundle.getInt("MOVIE_ID");
        mListView.setEmptyView(mEmptyView);
        mAdapter = new MovieCommentsAdapter(getActivity(), mCommentArrayList);
        mListView.setAdapter(mAdapter);

        if (savedInstanceState == null) {
            String baseUrl = getString(R.string.movie_base_url) + String.valueOf(mMovieIdKey) + "/reviews?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc&language=en-US";
            fetchData(baseUrl);
        } else if (savedInstanceState != null) {
            mCommentArrayList = savedInstanceState.getParcelableArrayList(COMMENTS_KEY);
        }

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
                        mCommentArrayList.addAll(getCommentsJsonDetails(mJsonData));
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

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private ArrayList<Comment> getCommentsJsonDetails(String jsonData) throws JSONException {
        ArrayList<Comment> commentsArrayList = new ArrayList<>();
        JSONObject movieComments = new JSONObject(jsonData);
        JSONArray results = movieComments.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject commentsResults = results.getJSONObject(i);
            String author = commentsResults.getString("author");
            String comment = commentsResults.getString("content");
            commentsArrayList.add(new Comment(author, comment));
        }
        return commentsArrayList;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(COMMENTS_KEY, mCommentArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

}
