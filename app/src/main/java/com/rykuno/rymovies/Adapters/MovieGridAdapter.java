package com.rykuno.rymovies.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rykuno.rymovies.Objects.Movie;
import com.rykuno.rymovies.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by rykuno on 10/6/16.
 */

public class MovieGridAdapter extends ArrayAdapter<Movie> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public MovieGridAdapter(Context context, List<Movie> object) {
        super(context, 0, object);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItemView = convertView;
        Movie currentMovie = getItem(position);
        MyViewHolder holder = null;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
            holder = new MyViewHolder(gridItemView);
            gridItemView.setTag(holder);
        } else {
            holder = (MyViewHolder) gridItemView.getTag();
        }

        if (currentMovie.getPoster() !=null)
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w500/" + currentMovie.getPoster()).into(holder.posterImage);
        else{
            Bitmap bmp = BitmapFactory.decodeByteArray(currentMovie.getPosterByte(), 0, currentMovie.getPosterByte().length);
            holder.posterImage.setImageBitmap(bmp);
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


    static class MyViewHolder {
        ImageView posterImage;
        TriangleLabelView posterRating;

        public MyViewHolder(View v) {
            posterImage = (ImageView) v.findViewById(R.id.poster_imageView);
            posterRating = (TriangleLabelView) v.findViewById(R.id.posterRating);
        }
    }
}
