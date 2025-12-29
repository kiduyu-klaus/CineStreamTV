package com.cinestream.tvplayer.ui.details;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.cinestream.tvplayer.data.repository.MediaRepositoryVideasy;
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

        // Get media item from intent
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
        // Load backdrop image with gradient overlay
        if (mediaItems.getBackgroundImageUrl() != null) {
            Glide.with(this)
                    .load(mediaItems.getBackgroundImageUrl())
                    .centerCrop()
                    .into(backdropImageView);
        }

        // Load poster image
        Glide.with(this)
                .load(mediaItems.getPosterUrl())
                .centerCrop()
                .into(posterImageView);

        // Set text content
        titleTextView.setText(mediaItems.getTitle());
        descriptionTextView.setText(mediaItems.getDescription());

        // Year
        if (mediaItems.getYear() > 0) {
            yearTextView.setText(String.valueOf(mediaItems.getYear()));
        } else {
            yearTextView.setVisibility(View.GONE);
        }

        // Rating with TMDb style
        if (mediaItems.getRating() > 0) {
            ratingTextView.setText(String.format("%.1f", mediaItems.getRating()));
        } else {
            ratingTextView.setVisibility(View.GONE);
        }

        // Duration
        if (mediaItems.getDuration() != null && !mediaItems.getDuration().isEmpty()) {
            durationTextView.setText(mediaItems.getDuration());
        } else {
            durationTextView.setVisibility(View.GONE);
        }

        // Genres as text
        if (mediaItems.getGenres() != null && !mediaItems.getGenres().isEmpty()) {
            genresTextView.setText(String.join(" • ", mediaItems.getGenres()));
        } else if (mediaItems.getGenre() != null && !mediaItems.getGenre().isEmpty()) {
            genresTextView.setText(mediaItems.getGenre());
        } else {
            genresTextView.setVisibility(View.GONE);
        }

        // Creators/Directors (placeholder - would need to fetch from TMDB details)
        creatorsTextView.setText("Loading...");

        // Stars/Cast (placeholder - would need to fetch from TMDB details)
        starsTextView.setText("Loading...");
    }

    private void setupRecommendations() {
        recommendationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recommendationsAdapter = new RecommendationsAdapter(this, item -> {
            // Handle recommendation click - open new details activity
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
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if this is API content
                    // Show loading and fetch video sources
                    fetchVideoSources();

            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle favorite status
                Toast.makeText(DetailsActivity.this,
                        "Added to favorites",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVideoSources() {
        // Show loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);

        // Fetch video sources from API

    }

    private void launchPlayer() {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("media_item", mediaItems);
        startActivity(intent);
    }
}