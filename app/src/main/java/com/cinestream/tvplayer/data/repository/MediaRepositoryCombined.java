package com.cinestream.tvplayer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.MediaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Combined repository that integrates TMDB API with VideasyAPI
 * Fetches movies/TV shows from TMDB, then tries to get streaming sources from Videasy
 */
public class MediaRepositoryCombined {
    private static final String TAG = "MediaRepositoryCombined";
    private static MediaRepositoryCombined instance;
    private final TMDBRepository tmdbRepository;
    private final MediaRepositoryVideasy videasyRepository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private MediaRepositoryCombined() {
        this.tmdbRepository = TMDBRepository.getInstance();
        this.videasyRepository = MediaRepositoryVideasy.getInstance();
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized MediaRepositoryCombined getInstance() {
        if (instance == null) {
            instance = new MediaRepositoryCombined();
        }
        return instance;
    }

    public interface CombinedCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    /**
     * Get popular movies from TMDB with streaming sources
     */
    public void getPopularMovies(int page, CombinedCallback<List<MediaItems>> callback) {
        Log.d(TAG, "Fetching popular movies from TMDB (page " + page + ")");
        
        tmdbRepository.getPopularMovies(page, new TMDBRepository.TMDBCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Fetched " + movies.size() + " popular movies from TMDB");
                // Enhance with streaming sources
                enhanceWithVideasySources(movies, new EnhancedCallback() {
                    @Override
                    public void onEnhanced(List<MediaItems> enhanced) {
                        mainHandler.post(() -> callback.onSuccess(enhanced));
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching popular movies from TMDB: " + error);
                mainHandler.post(() -> callback.onError("Failed to fetch popular movies: " + error));
            }
        });
    }

    /**
     * Get popular TV shows from TMDB with streaming sources
     */
    public void getPopularTVShows(int page, CombinedCallback<List<MediaItems>> callback) {
        Log.d(TAG, "Fetching popular TV shows from TMDB (page " + page + ")");
        
        tmdbRepository.getPopularTVShows(page, new TMDBRepository.TMDBCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> tvShows) {
                Log.d(TAG, "Fetched " + tvShows.size() + " popular TV shows from TMDB");
                // Enhance with streaming sources
                enhanceWithVideasySources(tvShows, new EnhancedCallback() {
                    @Override
                    public void onEnhanced(List<MediaItems> enhanced) {
                        mainHandler.post(() -> callback.onSuccess(enhanced));
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching popular TV shows from TMDB: " + error);
                mainHandler.post(() -> callback.onError("Failed to fetch popular TV shows: " + error));
            }
        });
    }

    /**
     * Get top rated movies from TMDB with streaming sources
     */
    public void getTopRatedMovies(int page, CombinedCallback<List<MediaItems>> callback) {
        Log.d(TAG, "Fetching top rated movies from TMDB (page " + page + ")");
        
        tmdbRepository.getTopRatedMovies(page, new TMDBRepository.TMDBCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> movies) {
                Log.d(TAG, "Fetched " + movies.size() + " top rated movies from TMDB");
                // Enhance with streaming sources
                enhanceWithVideasySources(movies, new EnhancedCallback() {
                    @Override
                    public void onEnhanced(List<MediaItems> enhanced) {
                        mainHandler.post(() -> callback.onSuccess(enhanced));
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching top rated movies from TMDB: " + error);
                mainHandler.post(() -> callback.onError("Failed to fetch top rated movies: " + error));
            }
        });
    }

    /**
     * Get trending content from TMDB with streaming sources
     */
    public void getTrending(TMDBApiClient.ContentType contentType, 
                           TMDBApiClient.TimeWindow timeWindow,
                           CombinedCallback<List<MediaItems>> callback) {
        Log.d(TAG, "Fetching trending " + contentType.name() + " from TMDB");
        
        tmdbRepository.getTrending(contentType, timeWindow, new TMDBRepository.TMDBCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> trending) {
                Log.d(TAG, "Fetched " + trending.size() + " trending " + contentType.name() + " from TMDB");
                // Enhance with streaming sources
                enhanceWithVideasySources(trending, new EnhancedCallback() {
                    @Override
                    public void onEnhanced(List<MediaItems> enhanced) {
                        mainHandler.post(() -> callback.onSuccess(enhanced));
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching trending content from TMDB: " + error);
                mainHandler.post(() -> callback.onError("Failed to fetch trending content: " + error));
            }
        });
    }

    /**
     * Get combined sample content (original + TMDB + Videasy)
     */
    public List<MediaItems> getAllSampleContent() {
        List<MediaItems> allContent = new ArrayList<>();
        
        // Original static content
        allContent.addAll(getOriginalStaticContent());
        
        // VideasyAPI sample content
        allContent.addAll(videasyRepository.getAPISampleContent());
        
        // TMDB popular content (first page)
        // Note: This would normally be async, but for demo purposes we'll use sync call
        try {
            List<MediaItems> tmdbMovies = (List<MediaItems>) tmdbRepository.getInstance(); // This would need to be sync for demo
            allContent.addAll(tmdbMovies);
        } catch (Exception e) {
            Log.e(TAG, "Error adding TMDB content to samples", e);
        }
        
        return allContent;
    }

    /**
     * Fetch streaming sources for a specific TMDB item
     */
    public void fetchStreamingSources(MediaItems tmdbItem, CombinedCallback<MediaItems> callback) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Fetching streaming sources for: " + tmdbItem.getTitle() + " (TMDB: " + tmdbItem.getTmdbId() + ")");
                
                String year = tmdbItem.getYear() > 0 ? String.valueOf(tmdbItem.getYear()) : "2023";
                
                videasyRepository.fetchVideoSources(
                    tmdbItem.getTitle(),
                    tmdbItem.getMediaType(),
                    year,
                    tmdbItem.getTmdbId(),
                    tmdbItem.getSeason(),
                    tmdbItem.getEpisode(),
                    new MediaRepositoryVideasy.ApiCallback<MediaItems>() {
                        @Override
                        public void onSuccess(MediaItems enhancedItem) {
                            // Update the original TMDB item with streaming sources
                            tmdbItem.setVideoSources(enhancedItem.getVideoSources());
                            tmdbItem.setSubtitles(enhancedItem.getSubtitles());
                            
                            mainHandler.post(() -> callback.onSuccess(tmdbItem));
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "No streaming sources found for: " + tmdbItem.getTitle(), 
                                  new Exception("VideasyAPI: " + error));
                            // Return the TMDB item without streaming sources
                            mainHandler.post(() -> callback.onSuccess(tmdbItem));
                        }
                    }
                );

            } catch (Exception e) {
                Log.e(TAG, "Error fetching streaming sources", e);
                mainHandler.post(() -> callback.onError("Failed to fetch streaming sources: " + e.getMessage()));
            }
        });
    }

    /**
     * Enhanced callback interface for streaming source enhancement
     */
    private interface EnhancedCallback {
        void onEnhanced(List<MediaItems> enhanced);
    }

    /**
     * Enhance a list of TMDB items with streaming sources from VideasyAPI
     */
    private void enhanceWithVideasySources(List<MediaItems> items, EnhancedCallback callback) {
        if (items.isEmpty()) {
            callback.onEnhanced(items);
            return;
        }

        List<MediaItems> enhanced = new ArrayList<>();
        final int[] completed = {0};
        final int total = items.size();

        for (MediaItems item : items) {
            fetchStreamingSources(item, new CombinedCallback<MediaItems>() {
                @Override
                public void onSuccess(MediaItems enhancedItem) {
                    enhanced.add(enhancedItem);
                    checkCompletion();
                }

                @Override
                public void onError(String error) {
                    // Add the item anyway, it just won't have streaming sources
                    enhanced.add(item);
                    Log.w(TAG, "Could not enhance " + item.getTitle() + " with streaming sources: " + error);
                    checkCompletion();
                }

                private void checkCompletion() {
                    completed[0]++;
                    if (completed[0] == total) {
                        callback.onEnhanced(enhanced);
                    }
                }
            });
        }
    }

    /**
     * Get original static content for comparison
     */
    private List<MediaItems> getOriginalStaticContent() {
        List<MediaItems> staticContent = new ArrayList<>();
        
        // Big Buck Bunny
        MediaItems bigBuckBunny = new MediaItems();
        bigBuckBunny.setId("static_1");
        bigBuckBunny.setTitle("Big Buck Bunny");
        bigBuckBunny.setDescription("A large and lovable rabbit deals with three bullying rodents: Frank, Rinky, and Dandelion.");
        bigBuckBunny.setPosterUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.jpg");
        bigBuckBunny.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        bigBuckBunny.setDuration("10m");
        bigBuckBunny.setYear(2008);
        bigBuckBunny.setGenre("Animation");
        bigBuckBunny.setRating(4.3f);
        staticContent.add(bigBuckBunny);

        // Sintel
        MediaItems sintel = new MediaItems();
        sintel.setId("static_2");
        sintel.setTitle("Sintel");
        sintel.setDescription("A lone girl searches for a baby dragon she calls Scales, but she's not the only one who wants the creature.");
        sintel.setPosterUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.jpg");
        sintel.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4");
        sintel.setDuration("15m");
        sintel.setYear(2010);
        sintel.setGenre("Fantasy");
        sintel.setRating(4.6f);
        staticContent.add(sintel);

        return staticContent;
    }

    /**
     * Search content across TMDB and enhance with streaming sources
     */
    public void searchContent(String query, TMDBApiClient.ContentType contentType, 
                             CombinedCallback<List<MediaItems>> callback) {
        Log.d(TAG, "Searching TMDB for: " + query);
        
        tmdbRepository.searchContent(query, contentType, 1, new TMDBRepository.TMDBCallback<List<MediaItems>>() {
            @Override
            public void onSuccess(List<MediaItems> results) {
                Log.d(TAG, "Found " + results.size() + " search results from TMDB");
                // Enhance with streaming sources
                enhanceWithVideasySources(results, new EnhancedCallback() {
                    @Override
                    public void onEnhanced(List<MediaItems> enhanced) {
                        mainHandler.post(() -> callback.onSuccess(enhanced));
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error searching TMDB: " + error);
                mainHandler.post(() -> callback.onError("Search failed: " + error));
            }
        });
    }

    /**
     * Get cache statistics
     */
    public void getCacheStats(CombinedCallback<String> callback) {
        tmdbRepository.getCacheStats(new TMDBRepository.TMDBCallback<String>() {
            @Override
            public void onSuccess(String stats) {
                mainHandler.post(() -> callback.onSuccess("TMDB Cache: " + stats));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onSuccess("TMDB Cache: Error getting stats"));
            }
        });
    }

    /**
     * Clear all caches
     */
    public void clearCaches() {
        tmdbRepository.clearCache();
        Log.d(TAG, "All caches cleared");
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}