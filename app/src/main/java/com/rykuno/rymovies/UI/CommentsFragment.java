package com.rykuno.rymovies.UI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.rykuno.rymovies.Adapters.MovieCommentsAdapter;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.rykuno.rymovies.Objects.Comment;
import com.rykuno.rymovies.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {
    private static final String COMMENTS_KEY = "COMMENTS_KEY";
    private ArrayList<Comment> mCommentArrayList = new ArrayList<>();
    private MovieCommentsAdapter mAdapter;
    private ApiRequest mApiRequest;
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
            fetchCommentsData();
        } else if (savedInstanceState != null) {
            mCommentArrayList = savedInstanceState.getParcelableArrayList(COMMENTS_KEY);
        }

        return rootView;
    }

    private void fetchCommentsData(){
        String baseUrl = getString(R.string.movie_base_url) + String.valueOf(mMovieIdKey) + "/reviews?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc&language=en-US";
        mApiRequest = new ApiRequest(getActivity(), "comments");
        mApiRequest.fetchData(baseUrl);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ArrayList<Comment> event) throws JSONException {
        mCommentArrayList.clear();
        mCommentArrayList.addAll(event);
        mAdapter.notifyDataSetChanged();
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
