package com.cinestream.tvplayer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.repository.MediaRepository;
import com.cinestream.tvplayer.data.repository.MediaRepositoryCombined;
import com.cinestream.tvplayer.data.repository.MediaRepositoryVideasy;
import com.cinestream.tvplayer.ui.adapter.ContentCarouselAdapter;
import com.cinestream.tvplayer.ui.details.DetailsActivity;
import com.cinestream.tvplayer.ui.player.PlayerActivity;
import com.cinestream.tvplayer.ui.search.SearchActivity;
import com.cinestream.tvplayer.ui.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class NetflixMainActivity extends AppCompatActivity {

    // UI Components
    private ImageView heroBackgroundImage;
    private ImageView searchIcon;
    private ImageView settingsIcon;
    private TextView nSeriesBadge;
    private TextView matchScoreText;
    private TextView contentTitle;
    private TextView yearText;
    private TextView maturityRating;
    private TextView durationText;
    private TextView qualityBadge;
    private TextView synopsisText;
    private Button playButton;
    private Button moreInfoButton;
    private TextView categoryTitle;
    private RecyclerView contentRecyclerView;
    private RelativeLayout loadingOverlay;
    private ProgressBar loadingProgressBar;

    // Navigation
    private LinearLayout navigationSidebar;

    // Data
    private List<MediaItems> allContent = new ArrayList<>();
    private ContentCarouselAdapter carouselAdapter;
    private MediaRepository mediaRepository;
    private MediaRepositoryVideasy apiRepository;
    private MediaRepositoryCombined combinedRepository;
    private int currentSelectedPosition = 0;
    
    // TMDB Loading states
    private boolean isLoadingTMDB = false;
    private int loadedTMDBCategories = 0;
    private static final int TOTAL_TMDB_CATEGORIES = 4;

    // Categories - Updated with TMDB content
    private static final String[] CATEGORIES = {
        "Featured Movies",
        "Action & Adventure", 
        "Comedy",
        "Drama",
        "Documentaries",
        "Format Demos",
        "🎆 Live API Content",
        "🎬 Popular Movies",
        "📺 Popular TV Shows", 
        "⭐ Top Rated Movies",
        "🔥 Trending Now"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_netflix);
        
        mediaRepository = new MediaRepository();
        apiRepository = MediaRepositoryVideasy.getInstance();
        combinedRepository = MediaRepositoryCombined.getInstance();
        
        initializeViews();
        setupClickListeners();
        loadContent();
    }

    private void initializeViews() {
        // Hero content views
        heroBackgroundImage = findViewById(R.id.heroBackgroundImage);
        searchIcon = findViewById(R.id.searchIcon);
        settingsIcon = findViewById(R.id.settingsIcon);
        nSeriesBadge = findViewById(R.id.nSeriesBadge);
        matchScoreText = findViewById(R.id.matchScoreText);
        contentTitle = findViewById(R.id.contentTitle);
        yearText = findViewById(R.id.yearText);
        maturityRating = findViewById(R.id.maturityRating);
        durationText = findViewById(R.id.durationText);
        qualityBadge = findViewById(R.id.qualityBadge);
        synopsisText = findViewById(R.id.synopsisText);
        playButton = findViewById(R.id.playButton);
        moreInfoButton = findViewById(R.id.moreInfoButton);

        // Navigation
        navigationSidebar = findViewById(R.id.navigationSidebar);

        // Carousel
        categoryTitle = findViewById(R.id.categoryTitle);
        contentRecyclerView = findViewById(R.id.contentRecyclerView);
        
        // Setup RecyclerView
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        // Loading
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }

    private void setupClickListeners() {
        // Play button
        playButton.setOnClickListener(v -> {
            MediaItems currentItem = allContent.get(currentSelectedPosition);
            if (currentItem.isFromAPI() || currentItem.isFromTMDB()) {
                fetchAndPlay(currentItem);
            } else {
                launchPlayer(currentItem);
            }
        });

        // More info button
        moreInfoButton.setOnClickListener(v -> {
            MediaItems currentItem = allContent.get(currentSelectedPosition);
            launchDetails(currentItem);
        });
        
        // Search icon click
        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(NetflixMainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        
        // Settings icon click
        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(NetflixMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadContent() {
        loadingOverlay.setVisibility(View.VISIBLE);
        isLoadingTMDB = true;
        loadedTMDBCategories = 0;
        
        // Combine all content from different categories
        allContent.clear();
        allContent.addAll(mediaRepository.getFeaturedMovies());
        allContent.addAll(mediaRepository.getActionMovies());
        allContent.addAll(mediaRepository.getComedyMovies());
        allContent.addAll(mediaRepository.getDramaMovies());
        allContent.addAll(mediaRepository.getDocumentaries());
        allContent.addAll(mediaRepository.getFormatDemos());
        allContent.addAll(apiRepository.getAPISampleContent());
        
        // Load TMDB content in the background
        loadTMDBContent();
        
        // Setup adapter with initial content
        carouselAdapter = new ContentCarouselAdapter(allContent);
        contentRecyclerView.setAdapter(carouselAdapter);

        carouselAdapter.setOnItemClickListener(new ContentCarouselAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItems mediaItems, int position) {
                currentSelectedPosition = position;
                updateHeroContent(mediaItems, position);
            }

            @Override
            public void onFocusChanged(MediaItems mediaItems, int position, boolean hasFocus) {
                if (hasFocus) {
                    currentSelectedPosition = position;
                    updateHeroContent(mediaItems, position);
                }
            }
        });

        // Set initial content if available
        if (!allContent.isEmpty()) {
            updateHeroContent(allContent.get(0), 0);
            carouselAdapter.setSelectedPosition(0);
        }
        
        // Don't hide loading overlay yet - wait for TMDB content
    }
    
    private void loadTMDBContent() {
        Log.d(TAG, "Loading TMDB content...");
        
        // Load Popular Movies
        combinedRepository.getPopularMovies(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Loaded " + movies.size() + " popular movies from TMDB");
                addTMDBContentToList(movies, 7); // Popular Movies category index
                checkTMDBLoadingComplete();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading popular movies: " + error);
                checkTMDBLoadingComplete();
            }
        });
        
        // Load Popular TV Shows
        combinedRepository.getPopularTVShows(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> tvShows) {
                Log.d(TAG, "Loaded " + tvShows.size() + " popular TV shows from TMDB");
                addTMDBContentToList(tvShows, 8); // Popular TV Shows category index
                checkTMDBLoadingComplete();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading popular TV shows: " + error);
                checkTMDBLoadingComplete();
            }
        });
        
        // Load Top Rated Movies
        combinedRepository.getTopRatedMovies(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Loaded " + movies.size() + " top rated movies from TMDB");
                addTMDBContentToList(movies, 9); // Top Rated Movies category index
                checkTMDBLoadingComplete();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading top rated movies: " + error);
                checkTMDBLoadingComplete();
            }
        });
        
        // Load Trending Content
        combinedRepository.getTrending(TMDBApiClient.ContentType.ALL, TMDBApiClient.TimeWindow.DAY, 
            new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
                @Override
                public void onSuccess(List<MediaItems> trending) {
                    Log.d(TAG, "Loaded " + trending.size() + " trending items from TMDB");
                    addTMDBContentToList(trending, 10); // Trending Now category index
                    checkTMDBLoadingComplete();
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading trending content: " + error);
                    checkTMDBLoadingComplete();
                }
            });
    }
    
    private void addTMDBContentToList(List<MediaItems> newContent, int categoryIndex) {
        int insertPosition = getCategoryInsertPosition(categoryIndex);
        allContent.addAll(insertPosition, newContent);
        
        // Update adapter
        if (carouselAdapter != null) {
            carouselAdapter.notifyDataSetChanged();
        }
    }
    
    private int getCategoryInsertPosition(int categoryIndex) {
        // Calculate the position where to insert TMDB content
        int position = 0;
        int[] categorySizes = {
            mediaRepository.getFeaturedMovies().size(),
            mediaRepository.getActionMovies().size(),
            mediaRepository.getComedyMovies().size(),
            mediaRepository.getDramaMovies().size(),
            mediaRepository.getDocumentaries().size(),
            mediaRepository.getFormatDemos().size(),
            apiRepository.getAPISampleContent().size()
        };
        
        // Add sizes of categories before the TMDB category
        for (int i = 0; i < categoryIndex && i < categorySizes.length; i++) {
            position += categorySizes[i];
        }
        
        // Add already loaded TMDB categories
        position += loadedTMDBCategories * 20; // Assume 20 items per TMDB category
        
        return position;
    }
    
    private void checkTMDBLoadingComplete() {
        loadedTMDBCategories++;
        Log.d(TAG, "TMDB categories loaded: " + loadedTMDBCategories + "/" + TOTAL_TMDB_CATEGORIES);
        
        if (loadedTMDBCategories >= TOTAL_TMDB_CATEGORIES) {
            isLoadingTMDB = false;
            loadingOverlay.setVisibility(View.GONE);
            Log.d(TAG, "All TMDB content loaded. Total items: " + allContent.size());
            
            // Update initial content if needed
            if (!allContent.isEmpty()) {
                updateHeroContent(allContent.get(0), 0);
                if (carouselAdapter != null) {
                    carouselAdapter.setSelectedPosition(0);
                }
            }
        }
    }

    private void updateHeroContent(MediaItems mediaItems, int position) {
        // Update category title based on position
        int categoryIndex = getCategoryIndex(position);
        if (categoryIndex >= 0 && categoryIndex < CATEGORIES.length) {
            categoryTitle.setText(CATEGORIES[categoryIndex]);
        }

        // Update hero background - Use primary image URL
        String imageUrl = mediaItems.getPrimaryImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(heroBackgroundImage);
        }

        // Update content information
        contentTitle.setText(mediaItems.getTitle());
        yearText.setText(mediaItems.getYear() > 0 ? String.valueOf(mediaItems.getYear()) : "");
        durationText.setText(mediaItems.getDuration());
        synopsisText.setText(mediaItems.getDescription());
        
        // Update metadata
        maturityRating.setText("PG-13"); // Default, could be dynamic
        qualityBadge.setText(mediaItems.hasValidVideoSources() ? "HD" : "Trailer");
        
        // Update match score (simulated)
        int matchScore = 85 + (position % 15); // 85-99%
        matchScoreText.setText(matchScore + "% Match");

        // Set N Series badge for API content (Videasy or TMDB)
        if (mediaItems.isFromAPI() || mediaItems.isFromTMDB()) {
            nSeriesBadge.setVisibility(View.VISIBLE);
            nSeriesBadge.setText(mediaItems.isFromTMDB() ? "TMDB" : "N Series");
        } else {
            nSeriesBadge.setVisibility(View.GONE);
        }

        // Update button states
        playButton.setEnabled(true);
        moreInfoButton.setEnabled(true);
    }

    private int getCategoryIndex(int position) {
        // Map position to category
        int[] categorySizes = {
            mediaRepository.getFeaturedMovies().size(),
            mediaRepository.getActionMovies().size(),
            mediaRepository.getComedyMovies().size(),
            mediaRepository.getDramaMovies().size(),
            mediaRepository.getDocumentaries().size(),
            mediaRepository.getFormatDemos().size(),
            apiRepository.getAPISampleContent().size()
        };

        int cumulativeSize = 0;
        for (int i = 0; i < categorySizes.length; i++) {
            cumulativeSize += categorySizes[i];
            if (position < cumulativeSize) {
                return i;
            }
        }
        return 0;
    }

    private void fetchAndPlay(MediaItems mediaItems) {
        loadingOverlay.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);
        moreInfoButton.setEnabled(false);

        if (mediaItems.isFromTMDB()) {
            // Use combined repository for TMDB content
            combinedRepository.fetchStreamingSources(mediaItems,
                new MediaRepositoryCombined.CombinedCallback<MediaItems>() {
                    @Override
                    public void onSuccess(MediaItems result) {
                        loadingOverlay.setVisibility(View.GONE);
                        if (result.hasValidVideoSources()) {
                            launchPlayer(result);
                        } else {
                            // Show message that streaming sources not available
                            Toast.makeText(NetflixMainActivity.this, 
                                "Streaming sources not available for this content", 
                                Toast.LENGTH_LONG).show();
                            playButton.setEnabled(true);
                            moreInfoButton.setEnabled(true);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        loadingOverlay.setVisibility(View.GONE);
                        playButton.setEnabled(true);
                        moreInfoButton.setEnabled(true);
                        Toast.makeText(NetflixMainActivity.this, 
                            "Failed to load video sources: " + error, 
                            Toast.LENGTH_LONG).show();
                    }
                });
        } else {
            // Use original API repository for Videasy content
            apiRepository.fetchVideoSources(
                mediaItems.getTitle(),
                mediaItems.getMediaType(),
                String.valueOf(mediaItems.getYear()),
                mediaItems.getTmdbId(),
                mediaItems.getSeason(),
                mediaItems.getEpisode(),
                new MediaRepositoryVideasy.ApiCallback<MediaItems>() {
                    @Override
                    public void onSuccess(MediaItems result) {
                        loadingOverlay.setVisibility(View.GONE);
                        launchPlayer(result);
                    }

                    @Override
                    public void onError(String error) {
                        loadingOverlay.setVisibility(View.GONE);
                        playButton.setEnabled(true);
                        moreInfoButton.setEnabled(true);
                        Toast.makeText(NetflixMainActivity.this, 
                            "Failed to load video sources: " + error, 
                            Toast.LENGTH_LONG).show();
                    }
                }
            );
        }
    }

    private void launchPlayer(MediaItems mediaItems) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("media_item", mediaItems);
        startActivity(intent);
    }

    private void launchDetails(MediaItems mediaItems) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("media_item", mediaItems);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh content when returning to main activity
        loadContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiRepository != null) {
            apiRepository.cleanup();
        }
        if (combinedRepository != null) {
            combinedRepository.cleanup();
        }
    }
    
    private static final String TAG = "NetflixMainActivity";
}