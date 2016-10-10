package com.rykuno.rymovies.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.rykuno.rymovies.Adapters.MovieGridAdapter;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;

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
    private MovieGridAdapter mAdapter;
    private ArrayList<Movie> mMovieList = new ArrayList<>();
    private ApiRequest apiRequest;
    private SharedPreferences prefs;
    private Boolean mTablet;

    @BindView(R.id.moviesPoster_gridview)
    GridView mGridView;


    public PosterFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poster, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        setUIData();

        if (savedInstanceState == null) {
            fetchPosterData();
        }

        return rootView;
    }

    private void setUIData() {
        mAdapter = new MovieGridAdapter(getActivity(), mMovieList);
        mGridView.setAdapter(mAdapter);

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
                    args.putParcelable("ARGUMENTS", currentMovie);
                    Log.v("CLICKING", currentMovie.getTitle());
                    DetailFragment detailFragment = new DetailFragment();
                    detailFragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
                }
            }
        });

    }

    private void fetchPosterData() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String baseUrl = "http://api.themoviedb.org/3/movie/" + prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)) + "?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc";
        apiRequest = new ApiRequest(getActivity());
        apiRequest.fetchData(baseUrl, "poster");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ArrayList<Movie> event) throws JSONException {
        if (!event.isEmpty() && event.get(0) instanceof Movie) {
            mMovieList.clear();
            mMovieList.addAll(event);
            mAdapter.notifyDataSetChanged();

            mTablet = ((MainActivity) getActivity()).isTablet();
            if (mTablet) {
                Bundle args = new Bundle();
                Movie currentMovie = event.get(0);
                args.putParcelable("ARGUMENTS", currentMovie);
                Log.v("CLICKING", currentMovie.getTitle());
                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null)
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
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
            String baseUrl = "http://api.themoviedb.org/3/movie/" + prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)) + "?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc";
            apiRequest.fetchData(baseUrl, "poster");
        }
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
