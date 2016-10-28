package com.rykuno.rymovies.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.rykuno.rymovies.R;
import com.rykuno.rymovies.objects.eventBusObjects.CommentsEvent;
import com.rykuno.rymovies.objects.eventBusObjects.MovieEvent;
import com.rykuno.rymovies.objects.eventBusObjects.TrailerEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rykuno on 10/7/16.
 */

public class ApiRequest {
    private JsonParser mJsonParser;
    private String mCode;
    private Context mContext;
    private ArrayList parsedJsonArrayList;

    public ApiRequest(Context context) {
        mContext = context;
    }

    public void fetchData(String url, String code) {
        mCode = code;
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(ApiRequest.class.getSimpleName(), mContext.getString(R.string.data_fetch_failure));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String mJsonData = response.body().string();
                    mJsonParser = new JsonParser();
                    try {
                        switch (mCode) {
                            case "poster":
                                parsedJsonArrayList = mJsonParser.parseJsonData(mCode, mJsonData);
                                EventBus.getDefault().post(new MovieEvent(parsedJsonArrayList));
                                break;
                            case "trailer":
                                parsedJsonArrayList = mJsonParser.parseJsonData(mCode, mJsonData);
                                EventBus.getDefault().post(new TrailerEvent(parsedJsonArrayList));
                                break;
                            case "comments":
                                parsedJsonArrayList = mJsonParser.parseJsonData(mCode, mJsonData);
                                EventBus.getDefault().post(new CommentsEvent(parsedJsonArrayList));
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(mContext, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
