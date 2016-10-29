package com.rykuno.rymovies.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rykuno.rymovies.models.Movie;
import com.rykuno.rymovies.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.List;

import jp.shts.android.library.TriangleLabelView;


public class MovieGridAdapter extends ArrayAdapter<Movie> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private Context mContext;

    public MovieGridAdapter(Context context, List<Movie> object) {
        super(context, 0, object);
        mContext = context;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View gridItemView = convertView;
        Movie currentMovie = getItem(position);
        MyViewHolder holder;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
            holder = new MyViewHolder(gridItemView);
            gridItemView.setTag(holder);
        } else {
            holder = (MyViewHolder) gridItemView.getTag();
        }

        assert currentMovie != null;
        if (!currentMovie.getPoster().contains(mContext.getString(R.string.imageDir)))
            Picasso.with(getContext()).load(mContext.getString(R.string.current_poster_w500_url, currentMovie.getPoster())).into(holder.posterImage);
        else {
            holder.posterImage.setImageBitmap(loadImageFromStorage(String.valueOf(currentMovie.getId()) + mContext.getString(R.string.poster)));
        }


        if (currentMovie.getRating() > 8) {
            holder.posterRating.setVisibility(View.VISIBLE);
            String rating = DECIMAL_FORMAT.format(currentMovie.getRating());
            holder.posterRating.setSecondaryText(rating);
        } else {
            holder.posterRating.setVisibility(View.INVISIBLE);
        }

        return gridItemView;
    }

    private Bitmap loadImageFromStorage(String identifier) {

        try {
            File f = new File("/data/user/0/com.rykuno.rymovies/app_imageDir", identifier + ".jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }


    private static class MyViewHolder {
        ImageView posterImage;
        TriangleLabelView posterRating;

        MyViewHolder(View v) {
            posterImage = (ImageView) v.findViewById(R.id.poster_imageView);
            posterRating = (TriangleLabelView) v.findViewById(R.id.posterRating);
        }
    }
}
