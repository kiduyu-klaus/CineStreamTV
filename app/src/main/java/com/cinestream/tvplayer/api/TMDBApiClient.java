package com.cinestream.tvplayer.api;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.cinestream.tvplayer.data.model.MediaItems;

/**
 * TMDB API Client for fetching movies and TV shows
 * Uses OkHttp with Bearer token authentication
 */
public class TMDBApiClient {
    private static final String TAG = "TMDBApiClient";
    private static final String API_KEY = "4103fc3065c122eb5db6bdcff749fde7";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MTAzZmMzMDY1YzEyMmViNWRiNmJkY2ZmNzQ5ZmRlNyIsIm5iZiI6MTY2ODA2NDAzNC4yNDk5OTk4LCJzdWIiOiI2MzZjYTMyMjA0OTlmMjAwN2ZlYjA4MWEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.tjvtYPTPfLOyMdOouQ14GGgOzmfnZRW4RgvOzfoq19w";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";

    // Image sizes
    private static final String POSTER_SIZE = "w500";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String ORIGINAL_SIZE = "original";

    // OkHttp client instance
    private final OkHttpClient client;

    public enum ContentType {
        MOVIE, TV, ALL
    }

    public enum TimeWindow {
        DAY, WEEK
    }

    /**
     * Constructor initializes OkHttpClient
     */
    public TMDBApiClient() {
        this.client = new OkHttpClient();
    }

    /**
     * Fetch popular movies from TMDB
     */
    public List<MediaItems> getPopularMovies(int page) {
        List<MediaItems> movies = new ArrayList<>();

        try {
            String url = String.format("%s/movie/popular?language=en-US&page=%d",
                    BASE_URL, page);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    MediaItems mediaItems = createMediaItemFromTMDB(movie, ContentType.MOVIE);
                    if (mediaItems != null) {
                        movies.add(mediaItems);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing popular movies response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching popular movies", e);
        }

        return movies;
    }

    /**
     * Fetch popular TV shows from TMDB
     */
    public List<MediaItems> getPopularTVShows(int page) {
        List<MediaItems> tvShows = new ArrayList<>();

        try {
            String url = String.format("%s/tv/popular?language=en-US&page=%d",
                    BASE_URL, page);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject tvShow = results.getJSONObject(i);
                    MediaItems mediaItems = createMediaItemFromTMDB(tvShow, ContentType.TV);
                    if (mediaItems != null) {
                        tvShows.add(mediaItems);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing popular TV shows response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching popular TV shows", e);
        }

        return tvShows;
    }

    /**
     * Fetch top rated movies from TMDB
     */
    public List<MediaItems> getTopRatedMovies(int page) {
        List<MediaItems> movies = new ArrayList<>();

        try {
            String url = String.format("%s/movie/top_rated?language=en-US&page=%d",
                    BASE_URL, page);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.getJSONObject(i);
                    MediaItems mediaItems = createMediaItemFromTMDB(movie, ContentType.MOVIE);
                    if (mediaItems != null) {
                        movies.add(mediaItems);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing top rated movies response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching top rated movies", e);
        }

        return movies;
    }

    /**
     * Fetch trending content from TMDB
     */
    public List<MediaItems> getTrending(ContentType contentType, TimeWindow timeWindow) {
        List<MediaItems> trending = new ArrayList<>();

        try {
            String contentPath = contentType == ContentType.ALL ? "all" :
                    contentType == ContentType.MOVIE ? "movie" : "tv";
            String timePath = timeWindow == TimeWindow.DAY ? "day" : "week";

            String url = String.format("%s/trending/%s/%s",
                    BASE_URL, contentPath, timePath);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    ContentType detectedType = item.getString("media_type").equals("movie") ?
                            ContentType.MOVIE : ContentType.TV;
                    MediaItems mediaItems = createMediaItemFromTMDB(item, detectedType);
                    if (mediaItems != null) {
                        trending.add(mediaItems);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing trending response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching trending content", e);
        }

        return trending;
    }

    /**
     * Get movie details including full metadata
     */
    public MediaItems getMovieDetails(int movieId) {
        try {
            String url = String.format("%s/movie/%d?language=en-US",
                    BASE_URL, movieId);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject movie = new JSONObject(response);
                return createDetailedMediaItemFromTMDB(movie, ContentType.MOVIE);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing movie details response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching movie details", e);
        }

        return null;
    }

    /**
     * Get TV show details including full metadata
     */
    public MediaItems getTVShowDetails(int tvShowId) {
        try {
            String url = String.format("%s/tv/%d?language=en-US",
                    BASE_URL, tvShowId);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject tvShow = new JSONObject(response);
                return createDetailedMediaItemFromTMDB(tvShow, ContentType.TV);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing TV show details response", e);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching TV show details", e);
        }

        return null;
    }

    /**
     * Create MediaItem from TMDB JSON object
     */
    private MediaItems createMediaItemFromTMDB(JSONObject tmdbItem, ContentType contentType) {
        try {
            MediaItems mediaItems = new MediaItems();

            // Basic info
            int id = tmdbItem.getInt("id");
            String title = contentType == ContentType.MOVIE ?
                    tmdbItem.getString("title") : tmdbItem.getString("name");
            String description = tmdbItem.optString("overview", "No description available");
            String releaseDate = contentType == ContentType.MOVIE ?
                    tmdbItem.optString("release_date", "") :
                    tmdbItem.optString("first_air_date", "");
            double rating = tmdbItem.optDouble("vote_average", 0.0);

            // Set basic properties
            mediaItems.setId(String.valueOf(id));
            mediaItems.setTitle(title);
            mediaItems.setDescription(description);
            mediaItems.setYear(Integer.parseInt(releaseDate.isEmpty() ? "" : releaseDate.substring(0, 4)));
            mediaItems.setRating((float) rating);
            mediaItems.setTmdbId(String.valueOf(id));

            // Set content type
            if (contentType == ContentType.MOVIE) {
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
    private MediaItems createDetailedMediaItemFromTMDB(JSONObject tmdbItem, ContentType contentType) {
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
     * Make HTTP request to TMDB API using OkHttp
     */
    private String makeRequest(String urlString) {
        try {
            Request request = new Request.Builder()
                    .url(urlString)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer " + BEARER_TOKEN)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                response.close();
                return responseBody;
            } else {
                Log.e(TAG, "HTTP Error: " + response.code() + " - " + response.message());
                response.close();
                return null;
            }

        } catch (IOException e) {
            Log.e(TAG, "Error making HTTP request", e);
            return null;
        }
    }

    /**
     * Search for content by title
     */
    public List<MediaItems> searchContent(String query, ContentType contentType, int page) {
        List<MediaItems> results = new ArrayList<>();

        try {
            String endpoint = contentType == ContentType.MOVIE ? "movie" : "tv";
            String url = String.format("%s/search/%s?query=%s&page=%d",
                    BASE_URL, endpoint, query.replace(" ", "%20"), page);
            String response = makeRequest(url);

            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject item = resultsArray.getJSONObject(i);
                    MediaItems mediaItems = createMediaItemFromTMDB(item, contentType);
                    if (mediaItems != null) {
                        results.add(mediaItems);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing search results", e);
        } catch (Exception e) {
            Log.e(TAG, "Error searching content", e);
        }

        return results;
    }
}