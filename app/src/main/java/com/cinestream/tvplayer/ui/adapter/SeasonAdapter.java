package com.cinestream.tvplayer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.Season;

import java.util.List;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {

    private List<Season> seasons;
    private OnSeasonClickListener listener;
    private int selectedPosition = 0;

    public interface OnSeasonClickListener {
        void onSeasonClick(Season season);
    }

    public SeasonAdapter(List<Season> seasons) {
        this.seasons = seasons;
    }

    public void setOnSeasonClickListener(OnSeasonClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedSeason(int seasonNumber) {
        for (int i = 0; i < seasons.size(); i++) {
            if (seasons.get(i).getSeasonNumber() == seasonNumber) {
                int previousPosition = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_season, parent, false);
        return new SeasonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        Season season = seasons.get(position);
        holder.bind(season, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return seasons.size();
    }

    class SeasonViewHolder extends RecyclerView.ViewHolder {
        TextView seasonName;
        TextView episodeCount;
        View container;

        SeasonViewHolder(@NonNull View itemView) {
            super(itemView);
            seasonName = itemView.findViewById(R.id.seasonName);
            episodeCount = itemView.findViewById(R.id.episodeCount);
            container = itemView.findViewById(R.id.seasonContainer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSeasonClick(seasons.get(position));
                }
            });

            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
        }

        void bind(Season season, boolean isSelected) {
            seasonName.setText(season.getName());
            episodeCount.setText(season.getEpisodeCount() + " episodes");

            // Highlight selected season
            if (isSelected) {
                container.setBackgroundResource(R.drawable.season_selected_background);
                seasonName.setTextColor(itemView.getContext().getColor(android.R.color.white));
            } else {
                container.setBackgroundResource(R.drawable.season_normal_background);
                seasonName.setTextColor(itemView.getContext().getColor(R.color.gray_light));
            }
        }
    }
}