package com.rykuno.rymovies.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import com.rykuno.rymovies.services.ApiRequest;
import com.rykuno.rymovies.tasks.FetchFavoriteMoviesTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PosterFragment extends Fragment {
    private static final int CODE_PREFERENCES = 100;
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
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setHasOptionsMenu(true);
        setUIData();
        fetchPosterData();
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
            new FetchFavoriteMoviesTask(getActivity(), mAdapter).execute();
            if (mTablet)
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, new DialogFragment()).commit();
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

            String url = getString(R.string.movies_url, (mPrefs.getString(getString(R.string.sortOptions), getString(R.string.popular))), BuildConfig.MY_MOVIE_DB_API_KEY);
            mApiRequest = new ApiRequest(getActivity());
            mApiRequest.fetchData(url, getString(R.string.poster));
        }
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
