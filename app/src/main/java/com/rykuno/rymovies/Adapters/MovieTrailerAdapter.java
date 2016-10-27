package com.rykuno.rymovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rykuno.rymovies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rykuno on 10/7/16.
 */

public class MovieTrailerAdapter extends ArrayAdapter<String> {
    public MovieTrailerAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String currentTrailer = getItem(position);
        MovieTrailerAdapter.MyViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.trailer_item, parent, false);
            holder = new MovieTrailerAdapter.MyViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (MovieTrailerAdapter.MyViewHolder) view.getTag();
        }

        holder.trailer_textView.setText("Trailer " + (position + 1));
        Picasso.with(getContext()).load("http://img.youtube.com/vi/" + currentTrailer + "/0.jpg").into(holder.trailer_imageView);

        return view;
    }


    static class MyViewHolder {
        TextView trailer_textView;
        ImageView trailer_imageView;

        public MyViewHolder(View v) {
            trailer_textView = (TextView) v.findViewById(R.id.trailer_textView);
            trailer_imageView = (ImageView) v.findViewById(R.id.trailer_imageView);
        }
    }
}
