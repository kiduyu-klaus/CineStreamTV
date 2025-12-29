package com.cinestream.tvplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.Episode;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private List<Episode> episodes;
    private OnEpisodeClickListener listener;
    private int selectedPosition = 0;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode);
    }

    public EpisodeAdapter(List<Episode> episodes) {
        this.episodes = episodes;
    }

    public void setOnEpisodeClickListener(OnEpisodeClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedEpisode(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.bind(episode, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    class EpisodeViewHolder extends RecyclerView.ViewHolder {
        ImageView episodeThumbnail;
        ImageView playIcon;
        TextView episodeTitle;
        TextView episodeDate;
        TextView episodeDescription;
        View container;

        EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            episodeThumbnail = itemView.findViewById(R.id.episodeThumbnail);
            playIcon = itemView.findViewById(R.id.playIcon);
            episodeTitle = itemView.findViewById(R.id.episodeTitle);
            episodeDate = itemView.findViewById(R.id.episodeDate);
            episodeDescription = itemView.findViewById(R.id.episodeDescription);
            container = itemView.findViewById(R.id.episodeContainer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEpisodeClick(episodes.get(position));
                }
            });

            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
        }

        void bind(Episode episode, boolean isSelected) {
            episodeTitle.setText(episode.getEpisodeTitle());
            episodeDate.setText(episode.getAirDate());
            episodeDescription.setText(episode.getOverview());

            // Load thumbnail
            if (episode.getStillPath() != null && !episode.getStillPath().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(episode.getStillPath())
                        .centerCrop()
                        .into(episodeThumbnail);
            }

            // Show/hide play icon based on selection
            playIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Highlight selected episode
            if (isSelected) {
                container.setBackgroundResource(R.drawable.episode_selected_background);
            } else {
                container.setBackgroundResource(R.drawable.episode_normal_background);
            }
        }
    }
}