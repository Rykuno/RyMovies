package com.rykuno.rymovies.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rykuno.rymovies.objects.Comment;
import com.rykuno.rymovies.R;

import java.util.List;

/**
 * Created by rykuno on 10/8/16.
 */


public class MovieCommentsAdapter extends ArrayAdapter<Comment> {
    public MovieCommentsAdapter(Context context, List<Comment> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Comment currentComment = getItem(position);
        MovieCommentsAdapter.MyViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comments_item, parent, false);
            holder = new MovieCommentsAdapter.MyViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (MovieCommentsAdapter.MyViewHolder) view.getTag();
        }

        holder.author_textView.setText(currentComment.getAuthor());
        holder.comment_textView.setText(currentComment.getComment());
        return view;
    }


    static class MyViewHolder {
        TextView author_textView;
        TextView comment_textView;

        public MyViewHolder(View v) {
            author_textView = (TextView) v.findViewById(R.id.textview_to_pop_list_author);
            comment_textView = (TextView) v.findViewById(R.id.textview_to_pop_list_comments);
        }
    }
}