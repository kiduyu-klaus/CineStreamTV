package com.cinestream.tvplayer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.CategorySection;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.repository.MediaRepository;
import com.cinestream.tvplayer.data.repository.MediaRepositoryCombined;
import com.cinestream.tvplayer.data.repository.MediaRepositoryVideasy;
import com.cinestream.tvplayer.ui.adapter.CategoryAdapter;
import com.cinestream.tvplayer.ui.adapter.VerticalSpaceItemDecoration;
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
    private ImageView homeIcon;
    private ImageView moviesIcon;
    private ImageView tvIcon;
    private ImageView apiIcon;
    private ImageView myListIcon;
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
    private RecyclerView categoriesRecyclerView;
    private RelativeLayout loadingOverlay;
    private ProgressBar loadingProgressBar;

    // Navigation
    private LinearLayout navigationSidebar;

    // Data
    private List<CategorySection> categories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private MediaRepository mediaRepository;
    private MediaRepositoryVideasy apiRepository;
    private MediaRepositoryCombined combinedRepository;
    private MediaItems currentSelectedItem;

    // TMDB Loading states
    private int loadedTMDBCategories = 0;
    private static final int TOTAL_TMDB_CATEGORIES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_netflix);

        mediaRepository = new MediaRepository();
        apiRepository = MediaRepositoryVideasy.getInstance();
        combinedRepository = MediaRepositoryCombined.getInstance();

        initializeViews();
        setupClickListeners();
        setupNavigationFocus();
        loadContent();
    }

    private void initializeViews() {
        // Hero content views
        heroBackgroundImage = findViewById(R.id.heroBackgroundImage);
        searchIcon = findViewById(R.id.searchIcon);
        homeIcon = findViewById(R.id.homeIcon);
        moviesIcon = findViewById(R.id.moviesIcon);
        tvIcon = findViewById(R.id.tvIcon);
        apiIcon = findViewById(R.id.apiIcon);
        myListIcon = findViewById(R.id.myListIcon);
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

        // Categories RecyclerView
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);
        categoriesRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(16));

        // Loading
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }

    private void setupNavigationFocus() {
        homeIcon.requestFocus();
        homeIcon.setSelected(true);

        View.OnFocusChangeListener navFocusListener = (v, hasFocus) -> {
            if (hasFocus) {
                v.setSelected(true);
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                v.setSelected(false);
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        };

        searchIcon.setOnFocusChangeListener(navFocusListener);
        homeIcon.setOnFocusChangeListener(navFocusListener);
        moviesIcon.setOnFocusChangeListener(navFocusListener);
        tvIcon.setOnFocusChangeListener(navFocusListener);
        apiIcon.setOnFocusChangeListener(navFocusListener);
        myListIcon.setOnFocusChangeListener(navFocusListener);
        settingsIcon.setOnFocusChangeListener(navFocusListener);
    }

    private void setupClickListeners() {
        playButton.setOnClickListener(v -> {
            if (currentSelectedItem != null) {
                if (currentSelectedItem.isFromAPI() || currentSelectedItem.isFromTMDB()) {
                    fetchAndPlay(currentSelectedItem);
                } else {
                    launchPlayer(currentSelectedItem);
                }
            }
        });

        moreInfoButton.setOnClickListener(v -> {
            if (currentSelectedItem != null) {
                launchDetails(currentSelectedItem);
            }
        });

        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(NetflixMainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        homeIcon.setOnClickListener(v -> {
            loadContent();
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        moviesIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Movies - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        tvIcon.setOnClickListener(v -> {
            Toast.makeText(this, "TV Shows - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        apiIcon.setOnClickListener(v -> {
            Toast.makeText(this, "API Content", Toast.LENGTH_SHORT).show();
        });

        myListIcon.setOnClickListener(v -> {
            Toast.makeText(this, "My List - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(NetflixMainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (categoriesRecyclerView.hasFocus()) {
                    homeIcon.requestFocus();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (searchIcon.hasFocus() || homeIcon.hasFocus() ||
                        moviesIcon.hasFocus() || tvIcon.hasFocus() ||
                        apiIcon.hasFocus() || myListIcon.hasFocus() ||
                        settingsIcon.hasFocus()) {
                    if (!categories.isEmpty()) {
                        categoriesRecyclerView.requestFocus();
                    }
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void loadContent() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadedTMDBCategories = 0;

        categories.clear();

        // Add local categories
        categories.add(new CategorySection("Featured Movies", mediaRepository.getFeaturedMovies()));
        categories.add(new CategorySection("Action & Adventure", mediaRepository.getActionMovies()));
        categories.add(new CategorySection("Comedy", mediaRepository.getComedyMovies()));
        categories.add(new CategorySection("Drama", mediaRepository.getDramaMovies()));
        categories.add(new CategorySection("Documentaries", mediaRepository.getDocumentaries()));
        categories.add(new CategorySection("Format Demos", mediaRepository.getFormatDemos()));
        categories.add(new CategorySection("🎆 Live API Content", apiRepository.getAPISampleContent()));

        // Setup adapter
        categoryAdapter = new CategoryAdapter(categories);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItems mediaItems, int categoryPosition, int itemPosition) {
                currentSelectedItem = mediaItems;
                updateHeroContent(mediaItems, categoryPosition);
                launchDetails(currentSelectedItem);
            }

            @Override
            public void onItemFocusChanged(MediaItems mediaItems, int categoryPosition, int itemPosition, boolean hasFocus) {
                if (hasFocus) {
                    currentSelectedItem = mediaItems;
                    updateHeroContent(mediaItems, categoryPosition);
                }
            }
        });

        // Load TMDB content
        loadTMDBContent();

        // Set initial hero content
        if (!categories.isEmpty() && !categories.get(0).getItems().isEmpty()) {
            currentSelectedItem = categories.get(0).getItems().get(0);
            updateHeroContent(currentSelectedItem, 0);
        }

        loadingOverlay.setVisibility(View.GONE);
    }

    private void loadTMDBContent() {
        Log.d(TAG, "Loading TMDB content...");

        // Popular Movies
        combinedRepository.getPopularMovies(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Loaded " + movies.size() + " popular movies");
                categories.add(new CategorySection("🎬 Popular Movies", movies));
                categoryAdapter.notifyDataSetChanged();
                checkTMDBLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading popular movies: " + error);
                checkTMDBLoadingComplete();
            }
        });

        // Popular TV Shows
        combinedRepository.getPopularTVShows(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> tvShows) {
                Log.d(TAG, "Loaded " + tvShows.size() + " popular TV shows");
                categories.add(new CategorySection("📺 Popular TV Shows", tvShows));
                categoryAdapter.notifyDataSetChanged();
                checkTMDBLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading popular TV shows: " + error);
                checkTMDBLoadingComplete();
            }
        });

        // Top Rated Movies
        combinedRepository.getTopRatedMovies(1, new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Loaded " + movies.size() + " top rated movies");
                categories.add(new CategorySection("⭐ Top Rated Movies", movies));
                categoryAdapter.notifyDataSetChanged();
                checkTMDBLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading top rated movies: " + error);
                checkTMDBLoadingComplete();
            }
        });

        // Trending
        combinedRepository.getTrending(TMDBApiClient.ContentType.ALL, TMDBApiClient.TimeWindow.DAY,
                new MediaRepositoryCombined.CombinedCallback<List<MediaItems>>() {
                    @Override
                    public void onSuccess(List<MediaItems> trending) {
                        Log.d(TAG, "Loaded " + trending.size() + " trending items");
                        categories.add(new CategorySection("🔥 Trending Now", trending));
                        categoryAdapter.notifyDataSetChanged();
                        checkTMDBLoadingComplete();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error loading trending: " + error);
                        checkTMDBLoadingComplete();
                    }
                });
    }

    private void checkTMDBLoadingComplete() {
        loadedTMDBCategories++;
        Log.d(TAG, "TMDB loaded: " + loadedTMDBCategories + "/" + TOTAL_TMDB_CATEGORIES);

        if (loadedTMDBCategories >= TOTAL_TMDB_CATEGORIES) {
            Log.d(TAG, "All TMDB content loaded. Total categories: " + categories.size());
        }
    }

    private void updateHeroContent(MediaItems mediaItems, int categoryPosition) {
        String imageUrl = mediaItems.getPrimaryImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(heroBackgroundImage);
        }

        contentTitle.setText(mediaItems.getTitle());
        yearText.setText(mediaItems.getYear() > 0 ? String.valueOf(mediaItems.getYear()) : "");
        durationText.setText(mediaItems.getDuration());
        synopsisText.setText(mediaItems.getDescription());

        maturityRating.setText("PG-13");
        qualityBadge.setText(mediaItems.hasValidVideoSources() ? "HD" : "Trailer");

        int matchScore = 85 + (categoryPosition * 2);
        matchScoreText.setText(matchScore + "% Match");

        if (mediaItems.isFromAPI() || mediaItems.isFromTMDB()) {
            nSeriesBadge.setVisibility(View.VISIBLE);
            nSeriesBadge.setText(mediaItems.isFromTMDB() ? "TMDB" : "N Series");
        } else {
            nSeriesBadge.setVisibility(View.GONE);
        }

        playButton.setEnabled(true);
        moreInfoButton.setEnabled(true);
    }

    private void fetchAndPlay(MediaItems mediaItems) {
        loadingOverlay.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);
        moreInfoButton.setEnabled(false);

        if (mediaItems.isFromTMDB()) {
            combinedRepository.fetchStreamingSources(mediaItems,
                    new MediaRepositoryCombined.CombinedCallback<MediaItems>() {
                        @Override
                        public void onSuccess(MediaItems result) {
                            loadingOverlay.setVisibility(View.GONE);
                            if (result.hasValidVideoSources()) {
                                launchPlayer(result);
                            } else {
                                Toast.makeText(NetflixMainActivity.this,
                                        "Streaming sources not available",
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
                                    "Failed to load: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
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
                                    "Failed to load: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

    @OptIn(markerClass = UnstableApi.class)
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