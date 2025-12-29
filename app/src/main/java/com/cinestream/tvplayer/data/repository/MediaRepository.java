package com.cinestream.tvplayer.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cinestream.tvplayer.api.TMDBApiClient;
import com.cinestream.tvplayer.data.model.Episode;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.model.Season;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRepository {
    private static final String TAG = "MediaRepository";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String VIDEASY_API_BASE = "https://api.videasy.net";
    private static final String DECRYPT_API = "https://enc-dec.app/api/dec-videasy";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0MTAzZmMzMDY1YzEyMmViNWRiNmJkY2ZmNzQ5ZmRlNyIsIm5iZiI6MTY2ODA2NDAzNC4yNDk5OTk4LCJzdWIiOiI2MzZjYTMyMjA0OTlmMjAwN2ZlYjA4MWEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.tjvtYPTPfLOyMdOouQ14GGgOzmfnZRW4RgvOzfoq19w";

    private static final String POSTER_SIZE = "w500";
    private static final String BACKDROP_SIZE = "w1280";
    private static final String ORIGINAL_SIZE = "original";
    private static final int TIMEOUT_MS = 10000;
    private static MediaRepository instance;

    // Thread management
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());



    // Callback interface for async operations
    public interface TMDBCallback {
        void onSuccess(List<MediaItems> movies);
        void onError(String error);
    }

    /**
     * Async method to fetch movie/TV recommendations
     */
    public void getRecommendationsAsync(String tmdbId, String mediaType, TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/" + mediaType + "/" + tmdbId + "/recommendations?language=en-US&page=1";
                List<MediaItems> recommendations = fetchRecommendationsFromTMDB(urlString, mediaType);
                mainHandler.post(() -> callback.onSuccess(recommendations));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching recommendations", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch recommendations from TMDB API using Jsoup
     */
    private List<MediaItems> fetchRecommendationsFromTMDB(String urlString, String mediaType) throws IOException, JSONException {
        List<MediaItems> recommendations = new ArrayList<>();

        Connection.Response response = Jsoup.connect(urlString)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .ignoreContentType(true)
                .timeout(TIMEOUT_MS)
                .method(Connection.Method.GET)
                .execute();

        if (response.statusCode() == 200) {
            Log.d(TAG, "fetchRecommendationsFromTMDB: Success");

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray results = jsonResponse.getJSONArray("results");

            for (int i = 0; i < results.length() && i < 20; i++) {
                JSONObject itemJson = results.getJSONObject(i);

                // Determine content type for parsing
                TMDBApiClient.ContentType contentType = "movie".equals(mediaType) ?
                        TMDBApiClient.ContentType.MOVIE : TMDBApiClient.ContentType.TV;

                MediaItems item = createMediaItemFromTMDB(itemJson, contentType);
                if (item != null) {
                    recommendations.add(item);
                }
            }
        } else {
            Log.e(TAG, "fetchRecommendationsFromTMDB: Failed with status " + response.statusCode());
            throw new IOException("Failed to fetch recommendations: " + response.statusCode());
        }

        return recommendations;
    }

    /**
     * Async method to fetch featured/popular movies from TMDB API
     */
    public void getFeaturedMoviesAsync(TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String urlString = TMDB_BASE_URL + "/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&region=US&sort_by=popularity.desc";
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
                String urlString = TMDB_BASE_URL + "/movie/top_rated?language=en-US&page=1&region=US";
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

            // Handle genres from genre_ids array
            JSONArray genreIds = tmdbItem.optJSONArray("genre_ids");
            if (genreIds != null && genreIds.length() > 0) {
                List<String> genres = new ArrayList<>();
                for (int i = 0; i < genreIds.length(); i++) {
                    String genreName = getGenreName(genreIds.getInt(i));
                    if (genreName != null) {
                        genres.add(genreName);
                    }
                }
                mediaItems.setGenres(genres);

                // Set first genre as the genre string for backward compatibility
                if (!genres.isEmpty()) {
                    mediaItems.setGenre(genres.get(0));
                }
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
     * Map genre IDs to genre names
     */
    private String getGenreName(int genreId) {
        switch (genreId) {
            case 28: return "Action";
            case 12: return "Adventure";
            case 16: return "Animation";
            case 35: return "Comedy";
            case 80: return "Crime";
            case 99: return "Documentary";
            case 18: return "Drama";
            case 10751: return "Family";
            case 14: return "Fantasy";
            case 36: return "History";
            case 27: return "Horror";
            case 10402: return "Music";
            case 9648: return "Mystery";
            case 10749: return "Romance";
            case 878: return "Science Fiction";
            case 10770: return "TV Movie";
            case 53: return "Thriller";
            case 10752: return "War";
            case 37: return "Western";
            // TV genres
            case 10759: return "Action & Adventure";
            case 10762: return "Kids";
            case 10763: return "News";
            case 10764: return "Reality";
            case 10765: return "Sci-Fi & Fantasy";
            case 10766: return "Soap";
            case 10767: return "Talk";
            case 10768: return "War & Politics";
            default: return null;
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

                // Add genre information from genres array (not genre_ids)
                JSONArray genres = tmdbItem.optJSONArray("genres");
                if (genres != null && genres.length() > 0) {
                    List<String> genreList = new ArrayList<>();
                    for (int i = 0; i < genres.length(); i++) {
                        JSONObject genre = genres.getJSONObject(i);
                        genreList.add(genre.getString("name"));
                    }
                    mediaItems.setGenres(genreList);

                    // Set first genre as the genre string
                    if (!genreList.isEmpty()) {
                        mediaItems.setGenre(genreList.get(0));
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error adding detailed info to MediaItem", e);
            }
        }

        return mediaItems;
    }

    // Callback interface for video sources
    public interface VideasyCallback {
        void onSuccess(MediaItems updatedMediaItem);
        void onError(String error);
    }

    /**
     * Fetch Videasy streams for a movie
     * @param title Movie title
     * @param year Release year
     * @param tmdbId TMDB ID
     * @param callback Callback to return updated MediaItems with video sources
     */
    public void fetchVideasyStreamsMovie(String title, String year, String tmdbId, VideasyCallback callback) {
        executorService.execute(() -> {
            try {
                // Step 1: Get encrypted data from Videasy API
                String encodedTitle = URLEncoder.encode(title, "UTF-8");
                String videasyUrl = String.format(
                        "%s/1movies/sources-with-title?title=%s&mediaType=movie&year=%s&tmdbId=%s",
                        VIDEASY_API_BASE, encodedTitle, year, tmdbId
                );

                Log.d(TAG, "Fetching from Videasy: " + videasyUrl);

                Connection.Response encResponse = Jsoup.connect(videasyUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Connection", "keep-alive")
                        .ignoreContentType(true)
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.GET)
                        .execute();

                if (encResponse.statusCode() != 200) {
                    throw new IOException("Videasy API returned status: " + encResponse.statusCode());
                }

                String encryptedData = encResponse.body();
                Log.d(TAG, "Encrypted data received");

                // Step 2: Decrypt the data
                JSONObject decryptPayload = new JSONObject();
                decryptPayload.put("text", encryptedData);
                decryptPayload.put("id", tmdbId);

                Connection.Response decResponse = Jsoup.connect(DECRYPT_API)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .ignoreContentType(true)
                        .requestBody(decryptPayload.toString())
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.POST)
                        .execute();

                if (decResponse.statusCode() != 200) {
                    throw new IOException("Decrypt API returned status: " + decResponse.statusCode());
                }

                JSONObject decryptedResponse = new JSONObject(decResponse.body());
                String decryptedDataStr = decryptedResponse.getString("result");

                Log.d(TAG, "Decrypted data: " + decryptedDataStr);

                // Step 3: Parse the decrypted JSON
                JSONObject streamData = new JSONObject(decryptedDataStr);

                List<MediaItems.VideoSource> videoSources = new ArrayList<>();
                List<MediaItems.SubtitleItem> subtitles = new ArrayList<>();

                // Parse video sources
                if (streamData.has("sources")) {
                    JSONArray sourcesArray = streamData.getJSONArray("sources");
                    for (int i = 0; i < sourcesArray.length(); i++) {
                        JSONObject sourceObj = sourcesArray.getJSONObject(i);
                        String quality = sourceObj.optString("quality", "Unknown");
                        String url = sourceObj.optString("url", "");

                        if (!url.isEmpty()) {
                            videoSources.add(new MediaItems.VideoSource(quality, url));
                            Log.d(TAG, "Added source: " + quality + " - " + url);
                        }
                    }
                }

                // Parse subtitles
                if (streamData.has("subtitles")) {
                    JSONArray subtitlesArray = streamData.getJSONArray("subtitles");
                    for (int i = 0; i < subtitlesArray.length(); i++) {
                        JSONObject subObj = subtitlesArray.getJSONObject(i);
                        String url = subObj.optString("url", "");
                        String lang = subObj.optString("lang", "Unknown");
                        String language = subObj.optString("language", lang);

                        if (!url.isEmpty()) {
                            subtitles.add(new MediaItems.SubtitleItem(url, lang, language));
                        }
                    }
                }

                // Create updated MediaItems with video sources
                MediaItems updatedItem = new MediaItems();
                updatedItem.setVideoSources(videoSources);
                updatedItem.setSubtitles(subtitles);

                Log.d(TAG, "Successfully fetched " + videoSources.size() + " video sources and " +
                        subtitles.size() + " subtitles");

                // Return on main thread
                mainHandler.post(() -> callback.onSuccess(updatedItem));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching Videasy streams", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Fetch Videasy streams for a TV show episode
     * @param title Show title
     * @param year Release year
     * @param tmdbId TMDB ID
     * @param season Season number
     * @param episode Episode number
     * @param callback Callback to return updated MediaItems with video sources
     */
    public void fetchVideasyStreamsTV(String title, String year, String tmdbId,
                                      String season, String episode, VideasyCallback callback) {
        executorService.execute(() -> {
            try {
                String encodedTitle = URLEncoder.encode(title, "UTF-8");
                String videasyUrl = String.format(
                        "%s/1movies/sources-with-title?title=%s&mediaType=tv&year=%s&tmdbId=%s&seasonId=%s&episodeId=%s",
                        VIDEASY_API_BASE, encodedTitle, year, tmdbId, season, episode
                );

                Log.d(TAG, "Fetching TV from Videasy: " + videasyUrl);

                Connection.Response encResponse = Jsoup.connect(videasyUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Connection", "keep-alive")
                        .ignoreContentType(true)
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.GET)
                        .execute();

                if (encResponse.statusCode() != 200) {
                    throw new IOException("Videasy API returned status: " + encResponse.statusCode());
                }

                String encryptedData = encResponse.body();

                // Decrypt
                JSONObject decryptPayload = new JSONObject();
                decryptPayload.put("text", encryptedData);
                decryptPayload.put("id", tmdbId);

                Connection.Response decResponse = Jsoup.connect(DECRYPT_API)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .ignoreContentType(true)
                        .requestBody(decryptPayload.toString())
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.POST)
                        .execute();

                if (decResponse.statusCode() != 200) {
                    throw new IOException("Decrypt API returned status: " + decResponse.statusCode());
                }

                JSONObject decryptedResponse = new JSONObject(decResponse.body());
                String decryptedDataStr = decryptedResponse.getString("result");

                JSONObject streamData = new JSONObject(decryptedDataStr);

                List<MediaItems.VideoSource> videoSources = new ArrayList<>();
                List<MediaItems.SubtitleItem> subtitles = new ArrayList<>();

                // Parse sources and subtitles (same as movie)
                if (streamData.has("sources")) {
                    JSONArray sourcesArray = streamData.getJSONArray("sources");
                    for (int i = 0; i < sourcesArray.length(); i++) {
                        JSONObject sourceObj = sourcesArray.getJSONObject(i);
                        String quality = sourceObj.optString("quality", "Unknown");
                        String url = sourceObj.optString("url", "");

                        if (!url.isEmpty()) {
                            videoSources.add(new MediaItems.VideoSource(quality, url));
                        }
                    }
                }

                if (streamData.has("subtitles")) {
                    JSONArray subtitlesArray = streamData.getJSONArray("subtitles");
                    for (int i = 0; i < subtitlesArray.length(); i++) {
                        JSONObject subObj = subtitlesArray.getJSONObject(i);
                        String url = subObj.optString("url", "");
                        String lang = subObj.optString("lang", "Unknown");
                        String language = subObj.optString("language", lang);

                        if (!url.isEmpty()) {
                            subtitles.add(new MediaItems.SubtitleItem(url, lang, language));
                        }
                    }
                }

                MediaItems updatedItem = new MediaItems();
                updatedItem.setVideoSources(videoSources);
                updatedItem.setSubtitles(subtitles);

                mainHandler.post(() -> callback.onSuccess(updatedItem));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching Videasy TV streams", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Add these methods to your existing MediaRepository.java class

    // Add these callback interfaces at the top of the class:
    public interface TVShowDetailsCallback {
        void onSuccess(MediaItems detailedShow, List<Season> seasons);
        void onError(String error);
    }

    public interface EpisodesCallback {
        void onSuccess(List<Episode> episodes);
        void onError(String error);
    }

    /**
     * Get detailed TV show information including all seasons
     */
    public void getTVShowDetails(String tmdbId, TVShowDetailsCallback callback) {
        executorService.execute(() -> {
            try {
                String url = TMDB_BASE_URL + "/tv/" + tmdbId + "?language=en-US";

                Connection.Response response = Jsoup.connect(url)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .ignoreContentType(true)
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.GET)
                        .execute();

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());

                    // Create detailed MediaItems
                    MediaItems tvShow = createDetailedMediaItemFromTMDB(jsonResponse, TMDBApiClient.ContentType.TV);

                    // Parse seasons
                    List<Season> seasons = new ArrayList<>();
                    JSONArray seasonsArray = jsonResponse.optJSONArray("seasons");

                    if (seasonsArray != null) {
                        for (int i = 0; i < seasonsArray.length(); i++) {
                            JSONObject seasonJson = seasonsArray.getJSONObject(i);

                            // Skip "Season 0" (specials)
                            int seasonNumber = seasonJson.optInt("season_number", 0);
                            if (seasonNumber == 0) continue;

                            Season season = new Season();
                            season.setId(seasonJson.optInt("id", 0));
                            season.setName(seasonJson.optString("name", "Season " + seasonNumber));
                            season.setOverview(seasonJson.optString("overview", ""));
                            season.setSeasonNumber(seasonNumber);
                            season.setEpisodeCount(seasonJson.optInt("episode_count", 0));
                            season.setAirDate(seasonJson.optString("air_date", ""));

                            String posterPath = seasonJson.optString("poster_path", "");
                            if (!posterPath.isEmpty()) {
                                season.setPosterPath(IMAGE_BASE_URL + POSTER_SIZE + posterPath);
                            }

                            seasons.add(season);
                        }
                    }

                    MediaItems finalTvShow = tvShow;
                    mainHandler.post(() -> callback.onSuccess(finalTvShow, seasons));

                } else {
                    throw new IOException("Failed with status: " + response.statusCode());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching TV show details", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Get episodes for a specific season
     */
    public void getSeasonEpisodes(String tmdbId, int seasonNumber, EpisodesCallback callback) {
        executorService.execute(() -> {
            try {
                String url = TMDB_BASE_URL + "/tv/" + tmdbId + "/season/" + seasonNumber + "?language=en-US";

                Connection.Response response = Jsoup.connect(url)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .ignoreContentType(true)
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.GET)
                        .execute();

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());

                    List<Episode> episodes = new ArrayList<>();
                    JSONArray episodesArray = jsonResponse.optJSONArray("episodes");

                    if (episodesArray != null) {
                        for (int i = 0; i < episodesArray.length(); i++) {
                            JSONObject episodeJson = episodesArray.getJSONObject(i);

                            Episode episode = new Episode();
                            episode.setId(episodeJson.optInt("id", 0));
                            episode.setName(episodeJson.optString("name", "Episode " + (i + 1)));
                            episode.setOverview(episodeJson.optString("overview", "No description available"));
                            episode.setEpisodeNumber(episodeJson.optInt("episode_number", i + 1));
                            episode.setSeasonNumber(seasonNumber);
                            episode.setAirDate(episodeJson.optString("air_date", ""));
                            episode.setVoteAverage(episodeJson.optDouble("vote_average", 0.0));
                            episode.setRuntime(episodeJson.optInt("runtime", 0));

                            String stillPath = episodeJson.optString("still_path", "");
                            if (!stillPath.isEmpty()) {
                                episode.setStillPath(IMAGE_BASE_URL + BACKDROP_SIZE + stillPath);
                            }

                            episodes.add(episode);
                        }
                    }

                    mainHandler.post(() -> callback.onSuccess(episodes));

                } else {
                    throw new IOException("Failed with status: " + response.statusCode());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching season episodes", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Get episode details (for playing specific episode)
     */
    public void getEpisodeDetails(String tmdbId, int seasonNumber, int episodeNumber,
                                  TMDBCallback callback) {
        executorService.execute(() -> {
            try {
                String url = TMDB_BASE_URL + "/tv/" + tmdbId + "/season/" + seasonNumber +
                        "/episode/" + episodeNumber + "?language=en-US";

                Connection.Response response = Jsoup.connect(url)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .ignoreContentType(true)
                        .timeout(TIMEOUT_MS)
                        .method(Connection.Method.GET)
                        .execute();

                if (response.statusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());

                    // Create MediaItems for this episode
                    MediaItems episodeItem = new MediaItems();
                    episodeItem.setId(String.valueOf(jsonResponse.optInt("id", 0)));
                    episodeItem.setTitle(jsonResponse.optString("name", ""));
                    episodeItem.setDescription(jsonResponse.optString("overview", ""));
                    episodeItem.setSeason(String.valueOf(seasonNumber));
                    episodeItem.setEpisode(String.valueOf(episodeNumber));
                    episodeItem.setTmdbId(tmdbId);
                    episodeItem.setMediaType("tv");
                    episodeItem.setFromTMDB(true);

                    String stillPath = jsonResponse.optString("still_path", "");
                    if (!stillPath.isEmpty()) {
                        episodeItem.setPosterUrl(IMAGE_BASE_URL + BACKDROP_SIZE + stillPath);
                    }

                    List<MediaItems> result = new ArrayList<>();
                    result.add(episodeItem);

                    mainHandler.post(() -> callback.onSuccess(result));

                } else {
                    throw new IOException("Failed with status: " + response.statusCode());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching episode details", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
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