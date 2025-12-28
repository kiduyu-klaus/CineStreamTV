package com.cinestream.tvplayer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cinestream.tvplayer.api.VideasyAPI;
import com.cinestream.tvplayer.data.model.MediaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepositoryVideasy {
    private static final String TAG = "MediaRepositoryVideasy";
    private static MediaRepositoryVideasy instance;
    private ExecutorService executorService;
    private Handler mainHandler;

    private MediaRepositoryVideasy() {
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized MediaRepositoryVideasy getInstance() {
        if (instance == null) {
            instance = new MediaRepositoryVideasy();
        }
        return instance;
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    /**
     * Fetch video sources for a movie or TV show from VideasyAPI
     */
    public void fetchVideoSources(String title, String mediaType, String year, String tmdbId, 
                                 String season, String episode, ApiCallback<MediaItems> callback) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Fetching video sources for: " + title + " (TMDB: " + tmdbId + ")");
                
                // Call VideasyAPI
                VideasyAPI.VideasyResult result = VideasyAPI.getVideoSources(
                    title, mediaType, year, tmdbId, season, episode
                );

                if (result.success && !result.sources.isEmpty()) {
                    // Convert API result to MediaItem
                    MediaItems mediaItems = convertApiResultToMediaItem(
                        title, mediaType, year, tmdbId, season, episode, result
                    );

                    mainHandler.post(() -> callback.onSuccess(mediaItems));
                } else {
                    String errorMsg = result.error != null ? result.error : "No video sources found";
                    Log.e(TAG, "API Error: " + errorMsg);
                    mainHandler.post(() -> callback.onError(errorMsg));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching video sources", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    /**
     * Get sample content that uses VideasyAPI
     */
    public List<MediaItems> getAPISampleContent() {
        List<MediaItems> samples = new ArrayList<>();
        
        // Sample TV Show: Cyberpunk Edgerunners
        MediaItems cyberpunk = new MediaItems();
        cyberpunk.setId("api_tv_cyberpunk");
        cyberpunk.setTitle("Cyberpunk Edgerunners");
        cyberpunk.setDescription("In a dystopia overrun by corruption, crime, and cybernetic implants, a desperate street kid tries to survive as an edgerunner— a high-level mercenary and cyberspace netrunner.");
        cyberpunk.setPosterUrl("https://image.tmdb.org/t/p/original/7jSWOc6jWSw5hZ78HB8Hw3pJxuk.jpg");
        cyberpunk.setDuration("24min");
        cyberpunk.setYear(2022);
        cyberpunk.setGenre("Animation");
        cyberpunk.setRating(4.8f);
        cyberpunk.setTmdbId("115036");
        cyberpunk.setMediaType("tv");
        cyberpunk.setSeason("1");
        cyberpunk.setEpisode("1");
        samples.add(cyberpunk);
        
        // Sample Movie: Fast X
        MediaItems fastX = new MediaItems();
        fastX.setId("api_movie_fastx");
        fastX.setTitle("Fast X");
        fastX.setDescription("Dom Toretto and his family are targeted by the vengeful son of drug kingpin Hernan Reyes.");
        fastX.setPosterUrl("https://image.tmdb.org/t/p/original/fiVW06jE7z9YnO4trhaMEdclSiC.jpg");
        fastX.setDuration("2h 21min");
        fastX.setYear(2023);
        fastX.setGenre("Action");
        fastX.setRating(4.2f);
        fastX.setTmdbId("385687");
        fastX.setMediaType("movie");
        fastX.setSeason(null);
        fastX.setEpisode(null);
        samples.add(fastX);

        // Additional sample for testing
        MediaItems strangerThings = new MediaItems();
        strangerThings.setId("api_tv_strangerthings");
        strangerThings.setTitle("Stranger Things");
        strangerThings.setDescription("When a young boy vanishes, a small town uncovers a mystery involving secret experiments.");
        strangerThings.setPosterUrl("https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg");
        strangerThings.setDuration("51min");
        strangerThings.setYear(2016);
        strangerThings.setGenre("Sci-Fi");
        strangerThings.setRating(4.7f);
        strangerThings.setTmdbId("66732");
        strangerThings.setMediaType("tv");
        strangerThings.setSeason("1");
        strangerThings.setEpisode("1");
        samples.add(strangerThings);

        return samples;
    }

    /**
     * Convert VideasyAPI result to MediaItem
     */
    private MediaItems convertApiResultToMediaItem(String title, String mediaType, String year,
                                                   String tmdbId, String season, String episode,
                                                   VideasyAPI.VideasyResult apiResult) {
        MediaItems mediaItems = new MediaItems();
        
        // Set basic metadata
        mediaItems.setId("api_" + mediaType + "_" + tmdbId + "_" + (season != null ? season + "x" + episode : "movie"));
        mediaItems.setTitle(title);
        mediaItems.setYear(Integer.parseInt(year));
        mediaItems.setMediaType(mediaType);
        mediaItems.setTmdbId(tmdbId);
        mediaItems.setSeason(season);
        mediaItems.setEpisode(episode);
        
        // Convert video sources
        List<MediaItems.VideoSource> videoSources = new ArrayList<>();
        for (VideasyAPI.VideoSource source : apiResult.sources) {
            MediaItems.VideoSource mediaSource = new MediaItems.VideoSource(source.quality, source.url);
            videoSources.add(mediaSource);
        }
        mediaItems.setVideoSources(videoSources);
        
        // Convert subtitles
        List<MediaItems.SubtitleItem> subtitles = new ArrayList<>();
        for (VideasyAPI.Subtitle subtitle : apiResult.subtitles) {
            MediaItems.SubtitleItem mediaSubtitle = new MediaItems.SubtitleItem(subtitle.url, subtitle.lang, subtitle.language);
            subtitles.add(mediaSubtitle);
        }
        mediaItems.setSubtitles(subtitles);
        
        // Set default values
        mediaItems.setDuration("Unknown");
        mediaItems.setGenre(mediaType.equals("movie") ? "Movie" : "TV Show");
        mediaItems.setRating(4.0f);
        mediaItems.setDescription("Loading description...");
        
        // Set poster URL (you could implement TMDB API call here for real posters)
        String posterUrl = "https://image.tmdb.org/t/p/w500" + getPosterPath(tmdbId);
        mediaItems.setPosterUrl(posterUrl);
        
        return mediaItems;
    }

    /**
     * Get poster path from TMDB ID (simplified mapping)
     */
    private String getPosterPath(String tmdbId) {
        // This is a simplified mapping - in a real app, you'd call TMDB API
        switch (tmdbId) {
            case "115036": return "/r1P7yRKaK6cYQ5hQ8d5cLw2KqN2.jpg"; // Cyberpunk Edgerunners
            case "385687": return "/fiVW06jE7S9U2xM7K588WaS2y8T.jpg"; // Fast X
            case "66732": return "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg"; // Stranger Things
            default: return "/placeholder.jpg";
        }
    }

    /**
     * Search and fetch content (placeholder for future implementation)
     */
    public void searchContent(String query, ApiCallback<List<MediaItems>> callback) {
        // This would implement search functionality
        // For now, return sample content
        mainHandler.post(() -> callback.onSuccess(getAPISampleContent()));
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