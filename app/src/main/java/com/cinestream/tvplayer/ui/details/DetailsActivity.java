package com.cinestream.tvplayer.ui.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.repository.MediaRepositoryVideasy;
import com.cinestream.tvplayer.ui.player.PlayerActivity;

public class DetailsActivity extends AppCompatActivity {

    private MediaItems mediaItems;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView yearTextView;
    private TextView genreTextView;
    private TextView ratingTextView;
    private TextView durationTextView;
    private AppCompatButton playButton;
    private ProgressBar loadingProgressBar;
    private RelativeLayout loadingOverlay;

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
        
        initializeViews();
        setupViews();
        setupClickListeners();
    }

    private void initializeViews() {
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        yearTextView = findViewById(R.id.yearTextView);
        genreTextView = findViewById(R.id.genreTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        durationTextView = findViewById(R.id.durationTextView);
        playButton = findViewById(R.id.playButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupViews() {
        // Load poster image
        Glide.with(this)
                .load(mediaItems.getPosterUrl())
                .centerCrop()
                .into(posterImageView);
        
        // Set text content
        titleTextView.setText(mediaItems.getTitle());
        descriptionTextView.setText(mediaItems.getDescription());
        yearTextView.setText(String.valueOf(mediaItems.getYear()));
        genreTextView.setText(mediaItems.getGenre());
        ratingTextView.setText(String.format("%.1f/5", mediaItems.getRating()));
        durationTextView.setText(mediaItems.getDuration());
    }

    private void setupClickListeners() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if this is API content
                if (mediaItems.isFromAPI()) {
                    // Show loading and fetch video sources
                    fetchVideoSources();
                } else {
                    // Direct launch for static content
                    launchPlayer();
                }
            }
        });
    }

    private void fetchVideoSources() {
        // Show loading overlay
        loadingOverlay.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);
        
        // Fetch video sources from API
        MediaRepositoryVideasy.getInstance().fetchVideoSources(
            mediaItems.getTitle(),
            mediaItems.getMediaType(),
            String.valueOf(mediaItems.getYear()),
            mediaItems.getTmdbId(),
            mediaItems.getSeason(),
            mediaItems.getEpisode(),
            new MediaRepositoryVideasy.ApiCallback<MediaItems>() {
                @Override
                public void onSuccess(MediaItems result) {
                    // Hide loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                    playButton.setEnabled(true);
                    
                    // Update the media item with actual video sources
                    mediaItems = result;
                    
                    // Launch player
                    launchPlayer();
                }
                
                @Override
                public void onError(String error) {
                    // Hide loading overlay
                    loadingOverlay.setVisibility(View.GONE);
                    playButton.setEnabled(true);
                    
                    // Show error
                    Toast.makeText(DetailsActivity.this, 
                        "Failed to load video sources: " + error, 
                        Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    private void launchPlayer() {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("media_item", mediaItems);
        startActivity(intent);
    }
}