package com.rykuno.rymovies.UI;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.rykuno.rymovies.R;

public class MainActivity extends AppCompatActivity {

    private boolean mTabletMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.detail_container)!= null){
            mTabletMode = true;
            DetailFragment detailFragment = new DetailFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, detailFragment).commit();
        }
    }

    public boolean isTablet() {
        return mTabletMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
