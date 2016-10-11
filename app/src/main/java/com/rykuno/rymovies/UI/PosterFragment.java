package com.rykuno.rymovies.UI;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Toast;

import com.rykuno.rymovies.Adapters.MovieGridAdapter;
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


public class PosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String MOVIE_KEY = "MOVIE_KEY";
    private static final int CODE_PREFERENCES = 100;
    private MovieGridAdapter mAdapter;
    private ArrayList<Movie> mMovieList = new ArrayList<>();
    private ArrayList<Movie> mFavoriteMovies = new ArrayList<>();
    private ApiRequest apiRequest;
    private SharedPreferences prefs;
    private Uri mCurrentUri;
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
        mCurrentUri = FavoriteMovieEntry.CONTENT_URI;
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        setUIData();

        if (savedInstanceState == null) {
            fetchPosterData();
        }

        return rootView;
    }

    private void loadFavorites() {
        mMovieList.clear();
        Cursor cursor = getActivity().getContentResolver().query(FavoriteMovieEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            int idColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry._ID);
            int titleColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE);
            int plotColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT);
            int releaseDateColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RATING);
            int movieIdColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID);
            int posterColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER);
            int backdropColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP);

            int id = cursor.getInt(idColumnIndex);
            int movieId = cursor.getInt(movieIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String plot = cursor.getString(plotColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            double rating = cursor.getDouble(ratingColumnIndex);
            byte[] poster = cursor.getBlob(posterColumnIndex);
            byte[] backdrop = cursor.getBlob(backdropColumnIndex);


            Movie movie = new Movie(title, plot, poster, rating, releaseDate, backdrop, movieId);
            mMovieList.add(movie);
        }
        mAdapter.notifyDataSetChanged();
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

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Movie currentMovie = (Movie) parent.getItemAtPosition(position);
                if (mMovieList.contains(currentMovie)) {
                    mMovieList.remove(currentMovie);
                    Uri uri = ContentUris.withAppendedId(FavoriteMovieEntry.CONTENT_URI, currentMovie.getId());
                    int rowsDeleted = getActivity().getContentResolver().delete(uri, null, null);
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
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
            if (prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)).equals("favorites")) {
                loadFavorites();
            } else {
                String baseUrl = "http://api.themoviedb.org/3/movie/" + prefs.getString(getString(R.string.sortOptions), getString(R.string.popular)) + "?api_key=0379de6cabbe4ba56fb0e6d68aa6bbdc";
                apiRequest.fetchData(baseUrl, "poster");
            }
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                FavoriteMovieEntry._ID,
                FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID,
                FavoriteMovieEntry.COLUMN_FAVORITES_TITLE,
                FavoriteMovieEntry.COLUMN_FAVORITES_PLOT,
                FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE,
                FavoriteMovieEntry.COLUMN_FAVORITES_RATING,
                FavoriteMovieEntry.COLUMN_FAVORITES_POSTER,
                FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP,

        };
        return new CursorLoader(getActivity(), mCurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = getActivity().getContentResolver().query(FavoriteMovieEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            int idColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry._ID);
            int titleColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_TITLE);
            int plotColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_PLOT);
            int releaseDateColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_RATING);
            int movieIdColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_MOVIE_ID);
            int posterColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_POSTER);
            int backdropColumnIndex = cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_FAVORITES_BACKDROP);

            int id = cursor.getInt(idColumnIndex);
            int movieId = cursor.getInt(movieIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String plot = cursor.getString(plotColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            double rating = cursor.getDouble(ratingColumnIndex);
            byte[] poster = cursor.getBlob(posterColumnIndex);
            byte[] backdrop = cursor.getBlob(backdropColumnIndex);

            Movie movie = new Movie(title, plot, poster, rating, releaseDate, backdrop, movieId);
            mFavoriteMovies.add(movie);
        }
        Toast.makeText(getActivity(), mFavoriteMovies.size() + "", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
