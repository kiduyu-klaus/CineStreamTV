package com.cinestream.tvplayer.ui.details;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.repository.MediaRepository;
import com.cinestream.tvplayer.ui.adapter.RecommendationsAdapter;
import com.cinestream.tvplayer.ui.player.PlayerActivity;

import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    private MediaItems mediaItems;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView yearTextView;
    private TextView ratingTextView;
    private TextView durationTextView;
    private TextView genresTextView;
    private TextView creatorsTextView;
    private TextView starsTextView;
    private AppCompatButton playButton;
    private AppCompatButton favoriteButton;
    private ProgressBar loadingProgressBar;
    private RelativeLayout loadingOverlay;
    private RecyclerView recommendationsRecyclerView;
    private TextView recommendationsTitle;
    private ProgressBar recommendationsLoadingBar;

    private MediaRepository mediaRepository;
    private RecommendationsAdapter recommendationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mediaItems = getIntent().getParcelableExtra("media_item");

        if (mediaItems == null) {
            finish();
            return;
        }

        mediaRepository = new MediaRepository();

        initializeViews();
        setupViews();
        setupClickListeners();
        setupRecommendations();
        loadRecommendations();
    }

    private void initializeViews() {
        backdropImageView = findViewById(R.id.backdropImageView);
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        yearTextView = findViewById(R.id.yearTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        durationTextView = findViewById(R.id.durationTextView);
        genresTextView = findViewById(R.id.genresTextView);
        creatorsTextView = findViewById(R.id.creatorsTextView);
        starsTextView = findViewById(R.id.starsTextView);
        playButton = findViewById(R.id.playButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView);
        recommendationsTitle = findViewById(R.id.recommendationsTitle);
        recommendationsLoadingBar = findViewById(R.id.recommendationsLoadingBar);
    }

    private void setupViews() {
        if (mediaItems.getBackgroundImageUrl() != null) {
            Glide.with(this)
                    .load(mediaItems.getBackgroundImageUrl())
                    .centerCrop()
                    .into(backdropImageView);
        }
        /**if (mediaItems.getPosterUrl() != null) {
            Glide.with(this)
                    .load(mediaItems.getPosterUrl())
                    .centerCrop()
                    .into(posterImageView);
        }**/
        titleTextView.setText(mediaItems.getTitle());
        descriptionTextView.setText(mediaItems.getDescription());

        if (mediaItems.getYear() > 0) {
            yearTextView.setText(String.valueOf(mediaItems.getYear()));
        } else {
            yearTextView.setVisibility(View.GONE);
        }

        if (mediaItems.getRating() > 0) {
            ratingTextView.setText(String.format("%.1f", mediaItems.getRating()));
        } else {
            ratingTextView.setVisibility(View.GONE);
        }

        if (mediaItems.getDuration() != null && !mediaItems.getDuration().isEmpty()) {
            durationTextView.setText(mediaItems.getDuration());
        } else {
            durationTextView.setVisibility(View.GONE);
        }

        if (mediaItems.getGenres() != null && !mediaItems.getGenres().isEmpty()) {
            genresTextView.setText(String.join(" • ", mediaItems.getGenres()));
        } else if (mediaItems.getGenre() != null && !mediaItems.getGenre().isEmpty()) {
            genresTextView.setText(mediaItems.getGenre());
        } else {
            genresTextView.setVisibility(View.GONE);
        }

        creatorsTextView.setText("Loading...");
        starsTextView.setText("Loading...");
    }

    private void setupRecommendations() {
        recommendationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recommendationsAdapter = new RecommendationsAdapter(this, item -> {
            Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
            intent.putExtra("media_item", item);
            startActivity(intent);
        });
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);
    }

    private void loadRecommendations() {
        if (mediaItems.getTmdbId() == null || mediaItems.getTmdbId().isEmpty()) {
            recommendationsTitle.setVisibility(View.GONE);
            recommendationsRecyclerView.setVisibility(View.GONE);
            return;
        }

        recommendationsLoadingBar.setVisibility(View.VISIBLE);

        mediaRepository.getRecommendationsAsync(
                mediaItems.getTmdbId(),
                mediaItems.getMediaType() != null ? mediaItems.getMediaType() : "movie",
                new MediaRepository.TMDBCallback() {
                    @Override
                    public void onSuccess(List<MediaItems> recommendations) {
                        recommendationsLoadingBar.setVisibility(View.GONE);

                        if (recommendations != null && !recommendations.isEmpty()) {
                            recommendationsTitle.setVisibility(View.VISIBLE);
                            recommendationsRecyclerView.setVisibility(View.VISIBLE);
                            recommendationsAdapter.setItems(recommendations);
                        } else {
                            recommendationsTitle.setVisibility(View.GONE);
                            recommendationsRecyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        recommendationsLoadingBar.setVisibility(View.GONE);
                        recommendationsTitle.setVisibility(View.GONE);
                        recommendationsRecyclerView.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setupClickListeners() {
        playButton.setOnClickListener(v -> {
            // Check if we need to fetch video sources
                fetchVideoSources();

        });

        favoriteButton.setOnClickListener(v -> {
            Toast.makeText(DetailsActivity.this,
                    "Added to favorites",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchVideoSources() {
        loadingOverlay.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);

        String title = mediaItems.getTitle();
        String year = String.valueOf(mediaItems.getYear());
        String tmdbId = mediaItems.getTmdbId();
        String mediaType = mediaItems.getMediaType();

        Log.d(TAG, "Fetching video sources for: " + title + " (" + year + ") [" + tmdbId + "]");
        Log.i(TAG, "fetchVideoSources: "+ title + " (" + year + ") [" + tmdbId + "]");

        if ("TV".equals(mediaType)) {
            // For TV shows - you'll need season/episode data
            String season = mediaItems.getSeason() != null ? mediaItems.getSeason() : "1";
            String episode = mediaItems.getEpisode() != null ? mediaItems.getEpisode() : "1";

            mediaRepository.fetchVideasyStreamsTV(title, year, tmdbId, season, episode,
                    new MediaRepository.VideasyCallback() {
                        @Override
                        public void onSuccess(MediaItems updatedItem) {
                            loadingOverlay.setVisibility(View.GONE);
                            playButton.setEnabled(true);

                            // Update current media item with video sources
                            mediaItems.setVideoSources(updatedItem.getVideoSources());
                            mediaItems.setSubtitles(updatedItem.getSubtitles());

                            Log.i(TAG, "Video sources fetched: " +
                                    mediaItems.getVideoSources().size());

                            launchPlayer();
                        }

                        @Override
                        public void onError(String error) {
                            loadingOverlay.setVisibility(View.GONE);
                            playButton.setEnabled(true);

                            Toast.makeText(DetailsActivity.this,
                                    "Failed to fetch video sources: " + error,
                                    Toast.LENGTH_LONG).show();

                            Log.e(TAG, "Error fetching video sources: " + error);
                        }
                    });
        } else {
            // For movies
            mediaRepository.fetchVideasyStreamsMovie(title, year, tmdbId,
                    new MediaRepository.VideasyCallback() {
                        @Override
                        public void onSuccess(MediaItems updatedItem) {
                            loadingOverlay.setVisibility(View.GONE);
                            playButton.setEnabled(true);

                            // Update current media item with video sources
                            mediaItems.setVideoSources(updatedItem.getVideoSources());
                            mediaItems.setSubtitles(updatedItem.getSubtitles());

                            Log.d(TAG, "Video sources fetched: " +
                                    mediaItems.getVideoSources().size());

                            launchPlayer();
                        }

                        @Override
                        public void onError(String error) {
                            loadingOverlay.setVisibility(View.GONE);
                            playButton.setEnabled(true);

                            Toast.makeText(DetailsActivity.this,
                                    "Failed to fetch video sources: " + error,
                                    Toast.LENGTH_LONG).show();

                            Log.e(TAG, "Error fetching video sources: " + error);
                        }
                    });
        }
    }

    private void launchPlayer() {
        if (!mediaItems.hasValidVideoSources()) {
            Toast.makeText(this, "No video sources available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("media_item", mediaItems);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRepository != null) {
            mediaRepository.cleanup();
        }
    }
}