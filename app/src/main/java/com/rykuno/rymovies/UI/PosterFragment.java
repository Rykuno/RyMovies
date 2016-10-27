package com.rykuno.rymovies.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.rykuno.rymovies.BuildConfig;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.adapters.MovieGridAdapter;
import com.rykuno.rymovies.objects.Movie;
import com.rykuno.rymovies.objects.eventBusObjects.MovieEvent;
import com.rykuno.rymovies.tasks.FetchFavoriteMoviesTask;
import com.rykuno.rymovies.utils.ApiRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PosterFragment extends Fragment {
    private static final String MOVIE_KEY = "MOVIE_KEY";
    private static final int CODE_PREFERENCES = 100;
    private static final String GRID_POSITION_KEY = "SCROLL KEY";
    private MovieGridAdapter mAdapter;
    private ArrayList<Movie> mMovieList;
    private ApiRequest mApiRequest;
    private SharedPreferences mPrefs;
    private boolean mTablet;
    private ProgressDialog mDialog;

    @BindView(R.id.moviesPoster_gridview)
    GridView mGridView;
    @BindView(R.id.gridView_emptyView)
    View mEmptyView;


    public PosterFragment() {
        mMovieList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poster, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
            mGridView.setVerticalScrollbarPosition(savedInstanceState.getInt(GRID_POSITION_KEY));
        }

        setUIData();
        return rootView;
    }


    private void setUIData() {
        mAdapter = new MovieGridAdapter(getActivity(), mMovieList);
        mGridView.setAdapter(mAdapter);
        mGridView.setEmptyView(mEmptyView);
        mDialog = new ProgressDialog(getActivity());

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTablet = ((MainActivity) getActivity()).isTablet();
                if (!mTablet) {
                    Movie currentMovie = (Movie) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(getString(R.string.movie_key), currentMovie);
                    startActivity(intent);
                } else {
                    Bundle args = new Bundle();
                    Movie currentMovie = (Movie) parent.getItemAtPosition(position);
                    args.putParcelable(getString(R.string.arguments), currentMovie);
                    DetailFragment detailFragment = new DetailFragment();
                    detailFragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
                }
            }
        });
    }

    public void removeFavoriteMovie() {
        if (mPrefs.getString(getString(R.string.sortOptions), getString(R.string.popular)).equals(getString(R.string.favorites))) {
            FetchFavoriteMoviesTask fetchFavoriteMoviesTask = new FetchFavoriteMoviesTask(getActivity(), mAdapter);
            fetchFavoriteMoviesTask.execute();

            if (mTablet) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, new DetailFragment()).commit();
            }
        }
    }


    private void fetchPosterData() {
        if (mPrefs.getString(getString(R.string.sortOptions), getString(R.string.popular)).equals(getString(R.string.favorites))) {
            FetchFavoriteMoviesTask fetchFavoriteMoviesTask = new FetchFavoriteMoviesTask(getActivity(), mAdapter);
            fetchFavoriteMoviesTask.execute();
            if (mTablet)
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, new DetailFragment()).commit();
        } else {
            mDialog.setMessage(getString(R.string.loading));
            mDialog.setCancelable(true);
            mDialog.show();

            String baseUrl = getString(R.string.movie_base_url) + mPrefs.getString(getString(R.string.sortOptions), getString(R.string.popular)) + "?api_key=" + BuildConfig.MY_MOVIE_DB_API_KEY;
            mApiRequest = new ApiRequest(getActivity());
            mApiRequest.fetchData(baseUrl, getString(R.string.poster));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, mMovieList);
        outState.putInt(GRID_POSITION_KEY, mGridView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(getActivity(), SettingsActivity.class), CODE_PREFERENCES);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (savedInstanceState == null) {
            fetchPosterData();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_PREFERENCES) {
            fetchPosterData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MovieEvent event) throws JSONException {
        if (mDialog.isShowing())
            mDialog.hide();

        mMovieList.clear();
        mMovieList.addAll(event.getMovieArrayList());
        mAdapter.notifyDataSetChanged();

        mTablet = ((MainActivity) getActivity()).isTablet();
        if (mTablet && event.getMovieArrayList().size() > 0) {
            Bundle args = new Bundle();
            Movie currentMovie = event.getMovieArrayList().get(0);
            args.putParcelable(getString(R.string.arguments), currentMovie);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Movie movie) {
        removeFavoriteMovie();
    }

    @Override
    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
