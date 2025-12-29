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

public class MediaRepositoryTV {
    private static final String TAG = "MediaRepositoryTV";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";

    // Image sizes
    private static final String POSTER_SIZE = "w500";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String ORIGINAL_SIZE = "original";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MTAzZmMzMDY1YzEyMmViNWRiNmJkY2ZmNzQ5ZmRlNyIsIm5iZiI6MTY2ODA2NDAzNC4yNDk5OTk4LCJzdWIiOiI2MzZjYTMyMjA0OTlmMjAwN2ZlYjA4MWEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.tjvtYPTPfLOyMdOouQ14GGgOzmfnZRW4RgvOzfoq19w";

    private static final int TIMEOUT_MS = 10000; //

    // Thread management
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Singleton instance
    private static MediaRepositoryTV instance;

    private MediaRepositoryTV() {
        // Private constructor for singleton
    }

    public static synchronized MediaRepositoryTV getInstance() {
        if (instance == null) {
            instance = new MediaRepositoryTV();
        }
        return instance;
    }

    // Callback interface for async operations
    public interface TVShowCallback {
        void onSuccess(List<MediaItems> tvShows);
        void onError(String error);
    }

    /**
     * Fetch popular TV shows in the US
     */
    public void getPopularTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/tv/popular?language=en-US&page=1&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching popular TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch top rated TV shows in the US
     */
    public void getTopRatedTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/tv/top_rated?language=en-US&page=1&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching top rated TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch TV shows airing today in the US
     */
    public void getAiringTodayTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/tv/airing_today?language=en-US&page=1&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching airing today TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch TV shows on the air in the US
     */
    public void getOnTheAirTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/tv/on_the_air?language=en-US&page=1&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching on the air TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch trending TV shows (weekly)
     */
    public void getTrendingTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/trending/tv/week?language=en-US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching trending TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch action & adventure TV shows
     */
    public void getActionAdventureTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 10759 is Action & Adventure
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=10759&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching action & adventure TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch comedy TV shows
     */
    public void getComedyTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 35 is Comedy
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=35&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching comedy TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch drama TV shows
     */
    public void getDramaTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 18 is Drama
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=18&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching drama TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch sci-fi & fantasy TV shows
     */
    public void getSciFiFantasyTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 10765 is Sci-Fi & Fantasy
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=10765&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching sci-fi & fantasy TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch documentary TV shows
     */
    public void getDocumentaryTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 99 is Documentary
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=99&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching documentary TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch crime TV shows
     */
    public void getCrimeTVShowsAsync(TVShowCallback callback) {
        executorService.execute(() -> {
            try {
                // Genre ID 80 is Crime
                String urlString = TMDB_BASE_URL + "/discover/tv?include_adult=false&include_null_first_air_dates=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=80&region=US";
                List<MediaItems> tvShows = fetchTVShowsFromTMDB(urlString);
                mainHandler.post(() -> callback.onSuccess(tvShows));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching crime TV shows", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Common method to fetch TV shows from TMDB API - runs on background thread
     */
    private List<MediaItems> fetchTVShowsFromTMDB(String urlString) throws IOException, JSONException {
        List<MediaItems> tvShows = new ArrayList<>();

        Connection.Response response = Jsoup.connect(urlString)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .ignoreContentType(true)
                .timeout(TIMEOUT_MS)
                .method(Connection.Method.GET)
                .execute();

        if (response.statusCode() == 200) {
            Log.d(TAG, "fetchTVShowsFromTMDB: Success");

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray results = jsonResponse.getJSONArray("results");

            for (int i = 0; i < results.length() && i < 20; i++) { // Limit to 20 items
                JSONObject tvShowJson = results.getJSONObject(i);
                MediaItems tvShow = createMediaItemFromTMDB(tvShowJson);
                if (tvShow != null) {
                    tvShows.add(tvShow);
                }
            }
        } else {
            Log.e(TAG, "fetchTVShowsFromTMDB: Failed with status " + response.statusCode());
            throw new IOException("Failed to fetch data: " + response.statusCode());
        }

        return tvShows;
    }

    /**
     * Create MediaItem from TMDB TV show data
     */
    private MediaItems createMediaItemFromTMDB(JSONObject tmdbItem) {
        try {
            MediaItems mediaItems = new MediaItems();

            // Basic info
            int id = tmdbItem.getInt("id");
            String name = tmdbItem.getString("name");
            String description = tmdbItem.optString("overview", "No description available");
            String firstAirDate = tmdbItem.optString("first_air_date", "");
            double rating = tmdbItem.optDouble("vote_average", 0.0);
            int voteCount = tmdbItem.optInt("vote_count", 0);

            // Set basic properties
            mediaItems.setId(String.valueOf(id));
            mediaItems.setTitle(name);
            mediaItems.setDescription(description);

            // Parse year safely
            if (!firstAirDate.isEmpty() && firstAirDate.length() >= 4) {
                try {
                    mediaItems.setYear(Integer.parseInt(firstAirDate.substring(0, 4)));
                } catch (NumberFormatException e) {
                    mediaItems.setYear(0);
                }
            } else {
                mediaItems.setYear(0);
            }

            mediaItems.setRating((float) rating);
            mediaItems.setTmdbId(String.valueOf(id));
            mediaItems.setVoteCount(voteCount);

            // Set content type as TV
            mediaItems.setMediaType("tv");

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

            // Add genre IDs if available
            JSONArray genreIds = tmdbItem.optJSONArray("genre_ids");
            if (genreIds != null && genreIds.length() > 0) {
                List<String> genres = new ArrayList<>();
                for (int i = 0; i < genreIds.length(); i++) {
                    genres.add(String.valueOf(genreIds.getInt(i)));
                }
                // Note: You might want to map these IDs to genre names
                // For now, just storing the IDs
            }

            // Additional TV show specific info
            String originCountry = tmdbItem.optJSONArray("origin_country") != null
                    && tmdbItem.optJSONArray("origin_country").length() > 0
                    ? tmdbItem.optJSONArray("origin_country").getString(0)
                    : "US";

            //mediaItems.setCountry(originCountry);

            return mediaItems;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating MediaItem from TMDB TV show data", e);
            return null;
        }
    }

    /**
     * Create detailed MediaItem from TMDB detailed TV show response
     */
    public MediaItems createDetailedTVShowFromTMDB(JSONObject tmdbItem) {
        MediaItems mediaItems = createMediaItemFromTMDB(tmdbItem);

        if (mediaItems != null) {
            try {
                // Additional details for TV shows
                int numberOfSeasons = tmdbItem.optInt("number_of_seasons", 0);
                int numberOfEpisodes = tmdbItem.optInt("number_of_episodes", 0);
                String status = tmdbItem.optString("status", "");
                String tagline = tmdbItem.optString("tagline", "");
                String type = tmdbItem.optString("type", "");

                mediaItems.setStatus(status);
                mediaItems.setTagline(tagline);

                // Set duration info
                if (numberOfSeasons > 0) {
                    mediaItems.setDuration(numberOfSeasons + " Season" + (numberOfSeasons > 1 ? "s" : ""));
                }

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

                // Networks
                JSONArray networks = tmdbItem.optJSONArray("networks");
                if (networks != null && networks.length() > 0) {
                    List<String> networkList = new ArrayList<>();
                    for (int i = 0; i < networks.length(); i++) {
                        JSONObject network = networks.getJSONObject(i);
                        networkList.add(network.getString("name"));
                    }
                    // You might want to add a setNetworks method to MediaItems
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error adding detailed info to TV show MediaItem", e);
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