package com.cinestream.tvplayer.ui.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;

public class CardPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_card, parent, false);
        return new MovieCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof MediaItems) {
            MovieCardViewHolder holder = (MovieCardViewHolder) viewHolder;
            MediaItems mediaItems = (MediaItems) item;

            // Set title
            holder.titleView.setText(mediaItems.getTitle());

            // Load image with placeholder and error handling
            Glide.with(holder.imageView.getContext())
                    .load(mediaItems.getPosterUrl())
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Clean up resources if needed
    }

    static class MovieCardViewHolder extends ViewHolder {
        ImageView imageView;
        TextView titleView;

        public MovieCardViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.imageView);
            titleView = view.findViewById(R.id.titleView);
        }
    }
}