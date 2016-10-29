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


public class MovieTrailerAdapter extends ArrayAdapter<String> {

    private Context mContext;

    public MovieTrailerAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        String currentTrailer = getItem(position);
        MovieTrailerAdapter.MyViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.trailer_item, parent, false);
            holder = new MovieTrailerAdapter.MyViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (MovieTrailerAdapter.MyViewHolder) view.getTag();
        }

        holder.trailer_textView.setText(mContext.getString(R.string.trailer_text, (position + 1)));
        Picasso.with(getContext()).load(mContext.getString(R.string.current_trailer_url, currentTrailer)).into(holder.trailer_imageView);
        return view;
    }

    private static class MyViewHolder {
        TextView trailer_textView;
        ImageView trailer_imageView;

        MyViewHolder(View v) {
            trailer_textView = (TextView) v.findViewById(R.id.trailer_textView);
            trailer_imageView = (ImageView) v.findViewById(R.id.trailer_imageView);
        }
    }
}
