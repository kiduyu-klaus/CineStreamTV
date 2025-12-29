package com.cinestream.tvplayer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.MediaItems;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepository {
    private static final String TAG = "TMDBRepository";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";

    // Image sizes
    private static final String POSTER_SIZE = "w500";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String ORIGINAL_SIZE = "original";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MTAzZmMzMDY1YzEyMmViNWRiNmJkY2ZmNzQ5ZmRlNyIsIm5iZiI6MTY2ODA2NDAzNC4yNDk5OTk4LCJzdWIiOiI2MzZjYTMyMjA0OTlmMjAwN2ZlYjA4MWEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.tjvtYPTPfLOyMdOouQ14GGgOzmfnZRW4RgvOzfoq19w";

    private static final int TIMEOUT_MS = 10000;

    // Thread management
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Callback interface for async operations
    public interface TMDBCallback {
        void onSuccess(List<MediaItems> movies);
        void onError(String error);
    }

    /**
     * Async method to fetch featured/popular movies from TMDB API
     */
    public void getFeaturedMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching featured movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch top rated movies from TMDB API
     */
    public void getTopRatedMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/movie/top_rated?language=en-US&page=1";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching top rated movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch comedy movies from TMDB API
     */
    public void getComedyMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 35 is Comedy
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=35";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching comedy movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch drama movies from TMDB API
     */
    public void getDramaMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 18 is Drama
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=18";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching drama movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch documentary movies from TMDB API
     */
    public void getDocumentariesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 99 is Documentary
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=99";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching documentaries", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch action movies from TMDB API
     */
    public void getActionMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 28 is Action
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=28";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching action movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Async method to fetch trending movies from TMDB API
     */
    public void getTrendingMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/trending/movie/week?language=en-US";
                List<MediaItems> movies = fetchMoviesFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(movies));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching trending movies", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Common method to fetch movies from TMDB API - runs on background thread
     */
    private List<MediaItems> fetchMoviesFromTMDB(String urlString) throws IOException, JSONException {
        List<MediaItems> movies = new ArrayList<>();

        Connection.Response response = Jsoup.connect(urlString)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .ignoreContentType(true)
                .timeout(TIMEOUT_MS)
                .method(Connection.Method.GET)
                .execute();

        if (response.statusCode() == 200) {
            Log.d(TAG, "fetchMoviesFromTMDB: Success");

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray results = jsonResponse.getJSONArray("results");

            for (int i = 0; i < results.length() && i < 20; i++) { // Limit to 20 items
                JSONObject movieJson = results.getJSONObject(i);
                MediaItems movie = createMediaItemFromTMDB(movieJson, TMDBApiClient.ContentType.MOVIE);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        } else {
            Log.e(TAG, "fetchMoviesFromTMDB: Failed with status " + response.statusCode());
            throw new IOException("Failed to fetch data: " + response.statusCode());
        }

        return movies;
    }

    private MediaItems createMediaItemFromTMDB(JSONObject tmdbItem, TMDBApiClient.ContentType contentType) {
        try {
            MediaItems mediaItems = new MediaItems();

            // Basic info
            int id = tmdbItem.getInt("id");
            String title = contentType == TMDBApiClient.ContentType.MOVIE ?
                    tmdbItem.getString("title") : tmdbItem.getString("name");
            String description = tmdbItem.optString("overview", "No description available");
            String releaseDate = contentType == TMDBApiClient.ContentType.MOVIE ?
                    tmdbItem.optString("release_date", "") :
                    tmdbItem.optString("first_air_date", "");
            double rating = tmdbItem.optDouble("vote_average", 0.0);

            // Set basic properties
            mediaItems.setId(String.valueOf(id));
            mediaItems.setTitle(title);
            mediaItems.setDescription(description);

            // Parse year safely
            if (!releaseDate.isEmpty() && releaseDate.length() >= 4) {
                try {
                    mediaItems.setYear(Integer.parseInt(releaseDate.substring(0, 4)));
                } catch (NumberFormatException e) {
                    mediaItems.setYear(0);
                }
            } else {
                mediaItems.setYear(0);
            }

            mediaItems.setRating((float) rating);
            mediaItems.setTmdbId(String.valueOf(id));

            // Set content type
            if (contentType == TMDBApiClient.ContentType.MOVIE) {
                mediaItems.setMediaType("movie");
            } else {
                mediaItems.setMediaType("tv");
            }

            // Set poster and backdrop URLs
            String posterPath = tmdbItem.optString("poster_path", "");
            String backdropPath = tmdbItem.optString("backdrop_path", "");

            if (!posterPath.isEmpty()) {
                mediaItems.setPosterUrl(IMAGE_BASE_URL + POSTER_SIZE + posterPath);
                mediaItems.setCardImageUrl(IMAGE_BASE_URL + POSTER_SIZE + posterPath);
            }

            if (!backdropPath.isEmpty()) {
                mediaItems.setBackgroundImageUrl(IMAGE_BASE_URL + BACKDROP_SIZE + backdropPath);
                mediaItems.setHeroImageUrl(IMAGE_BASE_URL + ORIGINAL_SIZE + backdropPath);
            } else if (!posterPath.isEmpty()) {
                // Fallback to poster if no backdrop
                mediaItems.setBackgroundImageUrl(IMAGE_BASE_URL + ORIGINAL_SIZE + posterPath);
                mediaItems.setHeroImageUrl(IMAGE_BASE_URL + ORIGINAL_SIZE + posterPath);
            }

            // Set from TMDB flag
            mediaItems.setFromTMDB(true);

            return mediaItems;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating MediaItem from TMDB data", e);
            return null;
        }
    }

    /**
     * Create detailed MediaItem from TMDB detailed response
     */
    private MediaItems createDetailedMediaItemFromTMDB(JSONObject tmdbItem, TMDBApiClient.ContentType contentType) {
        MediaItems mediaItems = createMediaItemFromTMDB(tmdbItem, contentType);

        if (mediaItems != null) {
            try {
                // Additional details for movies/TV shows
                int runtime = tmdbItem.optInt("runtime", 0);
                String status = tmdbItem.optString("status", "");
                String tagline = tmdbItem.optString("tagline", "");
                int voteCount = tmdbItem.optInt("vote_count", 0);

                mediaItems.setDuration(runtime > 0 ? String.valueOf(runtime) + " min" : "");
                mediaItems.setStatus(status);
                mediaItems.setTagline(tagline);
                mediaItems.setVoteCount(voteCount);

                // Add genre information
                JSONArray genres = tmdbItem.optJSONArray("genres");
                if (genres != null && genres.length() > 0) {
                    List<String> genreList = new ArrayList<>();
                    for (int i = 0; i < genres.length(); i++) {
                        JSONObject genre = genres.getJSONObject(i);
                        genreList.add(genre.getString("name"));
                    }
                    mediaItems.setGenres(genreList);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error adding detailed info to MediaItem", e);
            }
        }

        return mediaItems;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}