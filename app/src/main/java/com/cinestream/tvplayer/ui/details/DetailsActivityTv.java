package com.cinestream.tvplayer.ui.details;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.Episode;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.model.Season;
import com.cinestream.tvplayer.data.repository.MediaRepository;
import com.cinestream.tvplayer.ui.adapter.EpisodeAdapter;
import com.cinestream.tvplayer.ui.adapter.SeasonAdapter;
import com.cinestream.tvplayer.ui.player.PlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivityTv extends AppCompatActivity {
    private static final String TAG = "DetailsActivityTv";

    // UI Components
    private ImageView backgroundImage;
    private TextView titleText;
    private TextView yearText;
    private TextView seasonsCountText;
    private TextView genresText;
    private TextView descriptionText;
    private RecyclerView seasonsRecyclerView;
    private RecyclerView episodesRecyclerView;
    private TextView selectedSeasonTitle;
    private TextView selectedEpisodeTitle;
    private TextView selectedEpisodeDate;
    private TextView selectedEpisodeDescription;
    private ImageView selectedEpisodeThumbnail;
    private ProgressBar loadingProgress;
    private Button playButton;

    // Data
    private MediaItems tvShow;
    private List<Season> seasons = new ArrayList<>();
    private List<Episode> currentEpisodes = new ArrayList<>();
    private SeasonAdapter seasonAdapter;
    private EpisodeAdapter episodeAdapter;
    private MediaRepository mediaRepository;
    private int selectedSeasonNumber = 1;
    private Episode selectedEpisode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_tv);

        mediaRepository = new MediaRepository();

        // Get TV show from intent
        tvShow = getIntent().getParcelableExtra("media_item");
        if (tvShow == null) {
            finish();
            return;
        }

        initializeViews();
        setupRecyclerViews();
        loadTvShowDetails();
    }

    private void initializeViews() {
        backgroundImage = findViewById(R.id.backgroundImage);
        titleText = findViewById(R.id.titleText);
        yearText = findViewById(R.id.yearText);
        seasonsCountText = findViewById(R.id.seasonsCountText);
        genresText = findViewById(R.id.genresText);
        descriptionText = findViewById(R.id.descriptionText);
        seasonsRecyclerView = findViewById(R.id.seasonsRecyclerView);
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView);
        selectedSeasonTitle = findViewById(R.id.selectedSeasonTitle);
        selectedEpisodeTitle = findViewById(R.id.selectedEpisodeTitle);
        selectedEpisodeDate = findViewById(R.id.selectedEpisodeDate);
        selectedEpisodeDescription = findViewById(R.id.selectedEpisodeDescription);
        selectedEpisodeThumbnail = findViewById(R.id.selectedEpisodeThumbnail);
        loadingProgress = findViewById(R.id.loadingProgress);
        playButton = findViewById(R.id.playButton);

        // Set basic info
        titleText.setText(tvShow.getTitle());
        yearText.setText(String.valueOf(tvShow.getYear()));
        descriptionText.setText(tvShow.getDescription());

        // Load background
        String imageUrl = tvShow.getPrimaryImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(backgroundImage);
        }

        // Play button
        playButton.setOnClickListener(v -> {
            if (selectedEpisode != null) {
                playEpisode(selectedEpisode);
            } else {
                Toast.makeText(this, "Please select an episode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        // Seasons RecyclerView
        LinearLayoutManager seasonsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        seasonsRecyclerView.setLayoutManager(seasonsLayoutManager);

        seasonAdapter = new SeasonAdapter(seasons);
        seasonsRecyclerView.setAdapter(seasonAdapter);

        seasonAdapter.setOnSeasonClickListener(season -> {
            selectedSeasonNumber = season.getSeasonNumber();
            seasonAdapter.setSelectedSeason(selectedSeasonNumber);
            loadEpisodes(selectedSeasonNumber);
        });

        // Episodes RecyclerView
        LinearLayoutManager episodesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        episodesRecyclerView.setLayoutManager(episodesLayoutManager);

        episodeAdapter = new EpisodeAdapter(currentEpisodes);
        episodesRecyclerView.setAdapter(episodeAdapter);

        episodeAdapter.setOnEpisodeClickListener(episode -> {
            selectedEpisode = episode;
            updateSelectedEpisodeInfo(episode);
        });
    }

    private void loadTvShowDetails() {
        loadingProgress.setVisibility(View.VISIBLE);

        mediaRepository.getTVShowDetails(tvShow.getTmdbId(), new MediaRepository.TVShowDetailsCallback() {
            @Override
            public void onSuccess(MediaItems detailedShow, List<Season> seasonsList) {
                loadingProgress.setVisibility(View.GONE);

                // Update UI with detailed info
                tvShow = detailedShow;
                seasons.clear();
                seasons.addAll(seasonsList);

                // Update basic info
                seasonsCountText.setText(seasonsList.size() + " seasons");
                genresText.setText(detailedShow.getGenresAsString());

                // Notify adapter
                seasonAdapter.notifyDataSetChanged();

                // Auto-select first season
                if (!seasons.isEmpty()) {
                    selectedSeasonNumber = seasons.get(0).getSeasonNumber();
                    seasonAdapter.setSelectedSeason(selectedSeasonNumber);
                    loadEpisodes(selectedSeasonNumber);
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Error loading TV show details: " + error);
                Toast.makeText(DetailsActivityTv.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEpisodes(int seasonNumber) {
        loadingProgress.setVisibility(View.VISIBLE);
        currentEpisodes.clear();
        episodeAdapter.notifyDataSetChanged();

        selectedSeasonTitle.setText("Season " + seasonNumber);

        mediaRepository.getSeasonEpisodes(tvShow.getTmdbId(), seasonNumber, new MediaRepository.EpisodesCallback() {
            @Override
            public void onSuccess(List<Episode> episodes) {
                loadingProgress.setVisibility(View.GONE);

                currentEpisodes.clear();
                currentEpisodes.addAll(episodes);
                episodeAdapter.notifyDataSetChanged();

                // Auto-select first episode
                if (!episodes.isEmpty()) {
                    selectedEpisode = episodes.get(0);
                    episodeAdapter.setSelectedEpisode(0);
                    updateSelectedEpisodeInfo(episodes.get(0));
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Error loading episodes: " + error);
                Toast.makeText(DetailsActivityTv.this, "Failed to load episodes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelectedEpisodeInfo(Episode episode) {
        selectedEpisodeTitle.setText(episode.getName());
        selectedEpisodeDate.setText(episode.getAirDate());
        selectedEpisodeDescription.setText(episode.getOverview());

        // Load episode thumbnail
        if (episode.getStillPath() != null && !episode.getStillPath().isEmpty()) {
            Glide.with(this)
                    .load(episode.getStillPath())
                    .centerCrop()
                    .into(selectedEpisodeThumbnail);
        }
    }

    private void playEpisode(Episode episode) {
        // Create MediaItems for the episode
        MediaItems episodeMedia = new MediaItems();
        episodeMedia.setTitle(tvShow.getTitle());
        episodeMedia.setDescription(episode.getOverview());
        episodeMedia.setTmdbId(tvShow.getTmdbId());
        episodeMedia.setMediaType("tv");
        episodeMedia.setSeason(String.valueOf(selectedSeasonNumber));
        episodeMedia.setEpisode(String.valueOf(episode.getEpisodeNumber()));
        episodeMedia.setYear(tvShow.getYear());
        episodeMedia.setPosterUrl(episode.getStillPath());
        episodeMedia.setFromTMDB(true);

        String season = episodeMedia.getSeason() != null ? episodeMedia.getSeason() : "1";
        String episode1 = episodeMedia.getEpisode() != null ? episodeMedia.getEpisode() : "1";

        mediaRepository.fetchVideasyStreamsTV(tvShow.getTitle(), String.valueOf(tvShow.getYear()), tvShow.getTmdbId(), season, episode1,
                new MediaRepository.VideasyCallback() {
                    @OptIn(markerClass = UnstableApi.class) @Override
                    public void onSuccess(MediaItems updatedItem) {
                        //loadingOverlay.setVisibility(View.GONE);
                        playButton.setEnabled(true);

                        // Update current media item with video sources
                        episodeMedia.setVideoSources(updatedItem.getVideoSources());
                        episodeMedia.setSubtitles(updatedItem.getSubtitles());

                        Log.i(TAG, "Video sources fetched: " +
                                episodeMedia.getVideoSources().size());

                        Intent intent = new Intent(DetailsActivityTv.this, PlayerActivity.class);
                        intent.putExtra("media_item", episodeMedia);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String error) {
                        //loadingOverlay.setVisibility(View.GONE);
                        playButton.setEnabled(true);

                        Toast.makeText(DetailsActivityTv.this,
                                "Failed to fetch video sources: " + error,
                                Toast.LENGTH_LONG).show();

                        Log.e(TAG, "Error fetching video sources: " + error);
                    }
                });


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRepository != null) {
            mediaRepository.cleanup();
        }
    }
}