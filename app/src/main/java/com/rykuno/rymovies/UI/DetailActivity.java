package com.rykuno.rymovies.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rykuno.rymovies.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

            DetailFragment detailFragment = new DetailFragment();
        Log.v("DETAIL FRAG", "CALLED");
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
    }
}
