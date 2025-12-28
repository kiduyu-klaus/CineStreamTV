package com.cinestream.tvplayer.data.repository;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.MediaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for TMDB API data with caching and async operations
 */
public class TMDBRepository {
    private static final String TAG = "TMDBRepository";
    private static TMDBRepository instance;
    private final TMDBApiClient tmdbApiClient;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    
    // Cache for API responses (1 hour cache)
    private static final long CACHE_DURATION = 60 * 60 * 1000; // 1 hour in milliseconds
    private final List<CachedResponse> cache = new ArrayList<>();
    
    public interface TMDBCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    private static class CachedResponse {
        String endpoint;
        long timestamp;
        List<MediaItems> data;
        
        CachedResponse(String endpoint, List<MediaItems> data) {
            this.endpoint = endpoint;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION;
        }
    }
    
    private TMDBRepository() {
        this.tmdbApiClient = new TMDBApiClient();
        this.executorService = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized TMDBRepository getInstance() {
        if (instance == null) {
            instance = new TMDBRepository();
        }
        return instance;
    }
    
    /**
     * Get popular movies with caching
     */
    public void getPopularMovies(int page, TMDBCallback<List<MediaItems>> callback) {
        String cacheKey = "popular_movies_" + page;
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Returning cached popular movies for page " + page);
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>(cached.data)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                List<MediaItems> movies = tmdbApiClient.getPopularMovies(page);
                
                // Cache the result
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cacheResponse(cacheKey, movies);
                }

                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching popular movies", e);
                mainHandler.post(() -> callback.onError("Failed to fetch popular movies: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get popular TV shows with caching
     */
    public void getPopularTVShows(int page, TMDBCallback<List<MediaItems>> callback) {
        String cacheKey = "popular_tv_" + page;
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Returning cached popular TV shows for page " + page);
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>(cached.data)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                List<MediaItems> tvShows = tmdbApiClient.getPopularTVShows(page);
                
                // Cache the result
                cacheResponse(cacheKey, tvShows);
                
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching popular TV shows", e);
                mainHandler.post(() -> callback.onError("Failed to fetch popular TV shows: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get top rated movies with caching
     */
    public void getTopRatedMovies(int page, TMDBCallback<List<MediaItems>> callback) {
        String cacheKey = "top_rated_movies_" + page;
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Returning cached top rated movies for page " + page);
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>(cached.data)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                List<MediaItems> movies = tmdbApiClient.getTopRatedMovies(page);
                
                // Cache the result
                cacheResponse(cacheKey, movies);
                
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching top rated movies", e);
                mainHandler.post(() -> callback.onError("Failed to fetch top rated movies: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get trending content with caching
     */
    public void getTrending(TMDBApiClient.ContentType contentType, 
                           TMDBApiClient.TimeWindow timeWindow,
                           TMDBCallback<List<MediaItems>> callback) {
        String cacheKey = "trending_" + contentType.name().toLowerCase() + "_" + timeWindow.name().toLowerCase();
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Returning cached trending " + contentType.name() + " for " + timeWindow.name());
            mainHandler.post(() -> callback.onSuccess(new ArrayList<>(cached.data)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                List<MediaItems> trending = tmdbApiClient.getTrending(contentType, timeWindow);
                
                // Cache the result
                cacheResponse(cacheKey, trending);
                
                mainHandler.post(() -> callback.onSuccess(trending));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching trending content", e);
                mainHandler.post(() -> callback.onError("Failed to fetch trending content: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Search for content by title (general search)
     */
    public void searchContent(String query, TMDBApiClient.ContentType contentType, 
                             int page, TMDBCallback<List<MediaItems>> callback) {
        
        // Don't cache search results
        executorService.execute(() -> {
            try {
                List<MediaItems> results = tmdbApiClient.searchContent(query, contentType, page);
                mainHandler.post(() -> callback.onSuccess(results));
            } catch (Exception e) {
                Log.e(TAG, "Error searching content", e);
                mainHandler.post(() -> callback.onError("Failed to search content: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Search for all content (movies and TV shows combined)
     */
    public void searchAllContent(String query, int page, TMDBCallback<List<MediaItems>> callback) {
        searchContent(query, TMDBApiClient.ContentType.ALL, page, callback);
    }
    
    /**
     * Search specifically for movies
     */
    public void searchMovies(String query, int page, TMDBCallback<List<MediaItems>> callback) {
        searchContent(query, TMDBApiClient.ContentType.MOVIE, page, callback);
    }
    
    /**
     * Search specifically for TV shows
     */
    public void searchTVShows(String query, int page, TMDBCallback<List<MediaItems>> callback) {
        searchContent(query, TMDBApiClient.ContentType.TV, page, callback);
    }
    
    /**
     * Get trending search suggestions (popular queries)
     */
    public void getTrendingSearches(TMDBCallback<List<String>> callback) {
        mainHandler.post(() -> {
            List<String> trending = new ArrayList<>();
            trending.add("Avengers");
            trending.add("Star Wars");
            trending.add("Spider-Man");
            trending.add("Marvel");
            trending.add("DC");
            trending.add("Stranger Things");
            trending.add("Breaking Bad");
            trending.add("The Crown");
            trending.add("James Bond");
            trending.add("Fast & Furious");
            callback.onSuccess(trending);
        });
    }
    
    /**
     * Get movie details
     */
    public void getMovieDetails(int movieId, TMDBCallback<MediaItems> callback) {
        String cacheKey = "movie_details_" + movieId;
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid() && !cached.data.isEmpty()) {
            Log.d(TAG, "Returning cached movie details for ID " + movieId);
            mainHandler.post(() -> callback.onSuccess(cached.data.get(0)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                MediaItems movie = tmdbApiClient.getMovieDetails(movieId);
                
                if (movie != null) {
                    // Cache the result
                    List<MediaItems> singleItem = new ArrayList<>();
                    singleItem.add(movie);
                    cacheResponse(cacheKey, singleItem);
                }
                
                mainHandler.post(() -> callback.onSuccess(movie));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching movie details", e);
                mainHandler.post(() -> callback.onError("Failed to fetch movie details: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get TV show details
     */
    public void getTVShowDetails(int tvShowId, TMDBCallback<MediaItems> callback) {
        String cacheKey = "tv_details_" + tvShowId;
        
        // Check cache first
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null && cached.isValid() && !cached.data.isEmpty()) {
            Log.d(TAG, "Returning cached TV show details for ID " + tvShowId);
            mainHandler.post(() -> callback.onSuccess(cached.data.get(0)));
            return;
        }
        
        // Fetch from API
        executorService.execute(() -> {
            try {
                MediaItems tvShow = tmdbApiClient.getTVShowDetails(tvShowId);
                
                if (tvShow != null) {
                    // Cache the result
                    List<MediaItems> singleItem = new ArrayList<>();
                    singleItem.add(tvShow);
                    cacheResponse(cacheKey, singleItem);
                }
                
                mainHandler.post(() -> callback.onSuccess(tvShow));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching TV show details", e);
                mainHandler.post(() -> callback.onError("Failed to fetch TV show details: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Clear all cached data
     */

    
    /**
     * Get cached response by key
     */
    private CachedResponse getCachedResponse(String key) {
        for (CachedResponse cached : cache) {
            if (cached.endpoint.equals(key) && cached.isValid()) {
                return cached;
            }
        }
        return null;
    }
    
    /**
     * Cache API response
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void cacheResponse(String key, List<MediaItems> data) {
        // Remove old cache entry if exists
        cache.removeIf(cached -> cached.endpoint.equals(key));
        
        // Add new cache entry
        cache.add(new CachedResponse(key, data));
        
        // Limit cache size (keep last 20 entries)
        if (cache.size() > 20) {
            cache.remove(0);
        }
        
        Log.d(TAG, "Cached response for: " + key + " (" + data.size() + " items)");
    }
    
    /**
     * Get cache statistics
     */
    public void getCacheStats(TMDBCallback<String> callback) {
        mainHandler.post(() -> {
            int validEntries = 0;
            int totalEntries = cache.size();
            long oldestEntry = Long.MAX_VALUE;
            
            for (CachedResponse cached : cache) {
                if (cached.isValid()) {
                    validEntries++;
                }
                oldestEntry = Math.min(oldestEntry, cached.timestamp);
            }
            
            String stats = String.format("Cache: %d/%d entries valid, oldest: %d", 
                    validEntries, totalEntries, oldestEntry);
            callback.onSuccess(stats);
        });
    }
    
    /**
     * Clear all cached data
     */
    public void clearCache() {
        cache.clear();
        Log.d(TAG, "Cache cleared");
    }
}