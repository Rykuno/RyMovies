package com.rykuno.rymovies.UI;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Toast;

import com.rykuno.rymovies.Adapters.MovieGridAdapter;
import com.rykuno.rymovies.Objects.EventObjects.MovieEvent;
import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.rykuno.rymovies.Utils.ApiRequest;
import com.rykuno.rymovies.data.MovieDbContract.FavoriteMovieEntry;

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
    private static final String GRID_POSITION_KEY = "SCROLL KEY" ;
    private MovieGridAdapter mAdapter;
    private ArrayList<Movie> mMovieList = new ArrayList<>();
    private ArrayList<Movie> mFavoritesArrayList = new ArrayList<>();
    private ApiRequest apiRequest;
    private SharedPreferences prefs;
    private boolean mTablet;

    @BindView(R.id.moviesPoster_gridview)
    GridView mGridView;
    @BindView(R.id.gridView_emptyView)
    View mEmptyView;


    public PosterFragment() {

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

    public void loadFavorites() {
        mFavoritesArrayList.clear();
        Cursor cursor = getActivity().getContentResolver().query(FavoriteMovieEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            int titleColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE);
            int plotColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT);
            int releaseDateColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RATING);
            int movieIdColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID);
            int posterColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER);
            int backdropColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP);

            int movieId = cursor.getInt(movieIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String plot = cursor.getString(plotColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            double rating = cursor.getDouble(ratingColumnIndex);
            String poster = cursor.getString(posterColumnIndex);
            String backdrop = cursor.getString(backdropColumnIndex);

            mFavoritesArrayList.add(new Movie(title, plot, poster, rating, releaseDate, backdrop, movieId));
        }
        mAdapter.clear();
        mAdapter.addAll(mFavoritesArrayList);
        cursor.close();
    }

    private void setUIData() {
        mAdapter = new MovieGridAdapter(getActivity(), mMovieList);
        mGridView.setAdapter(mAdapter);
        mGridView.setEmptyView(mEmptyView);

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
                    DetailFragment detailFragment = new DetailFragment();
                    detailFragment.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
                }
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Movie currentMovie = (Movie) parent.getItemAtPosition(position);
                if (mMovieList.contains(currentMovie)) {
                    removeMovie(currentMovie);
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    public void removeMovie(Movie movie) {
        mFavoritesArrayList.remove(movie);
        Uri uri = ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, movie.getId());
        int rowsDeleted = getActivity().getContentResolver().delete(uri, null, null);
        if (prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)).equals("favorites"))
            loadFavorites();
    }

    private void fetchPosterData() {
        if (prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)).equals("favorites")) {
            Toast.makeText(getActivity(), "WORKING", Toast.LENGTH_SHORT).show();
            loadFavorites();
        } else {
            String baseUrl = "http://api.themoviedb.org/3/movie/" + prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)) + "?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc";
            apiRequest = new ApiRequest(getActivity());
            apiRequest.fetchData(baseUrl, "poster");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MovieEvent event) throws JSONException {
        mMovieList.clear();
        mMovieList.addAll(event.getMovieArrayList());
        mAdapter.notifyDataSetChanged();

        mTablet = ((MainActivity) getActivity()).isTablet();
        if (mTablet) {
            Bundle args = new Bundle();
            Movie currentMovie = event.getMovieArrayList().get(0);
            args.putParcelable("ARGUMENTS", currentMovie);
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Movie event) {
        removeMovie(event);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, mMovieList);
        outState.putInt(GRID_POSITION_KEY, mGridView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
