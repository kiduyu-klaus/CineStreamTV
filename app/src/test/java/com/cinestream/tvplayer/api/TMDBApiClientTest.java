package com.cinestream.tvplayer.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.List;

import com.cinestream.tvplayer.data.model.MediaItems;

import static org.junit.Assert.*;

/**
 * Unit tests for TMDBApiClient
 * Tests API calls to TMDB using JUnit
 */
public class TMDBApiClientTest {

    private TMDBApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = new TMDBApiClient();
    }

    @After
    public void tearDown() {
        apiClient = null;
    }

    // ================ Popular Movies Tests ================

    @Test
    public void testGetPopularMovies_ReturnsResults() {
        List<MediaItems> movies = apiClient.getPopularMovies(1);

        assertNotNull("Movies list should not be null", movies);
        assertFalse("Movies list should not be empty", movies.isEmpty());
        assertTrue("Should return at least 10 movies", movies.size() >= 10);
    }

    @Test
    public void testGetPopularMovies_ContainsValidData() {
        List<MediaItems> movies = apiClient.getPopularMovies(1);

        assertNotNull(movies);
        assertFalse(movies.isEmpty());

        MediaItems firstMovie = movies.get(0);
        assertNotNull("Movie should not be null", firstMovie);
        assertNotNull("Movie title should not be null", firstMovie.getTitle());
        assertFalse("Movie title should not be empty", firstMovie.getTitle().isEmpty());
        assertNotNull("Movie ID should not be null", firstMovie.getId());
        assertEquals("Media type should be movie", "movie", firstMovie.getMediaType());
        assertTrue("Rating should be greater than 0", firstMovie.getRating() > 0);
    }

    @Test
    public void testGetPopularMovies_HasImages() {
        List<MediaItems> movies = apiClient.getPopularMovies(1);

        assertNotNull(movies);
        assertFalse(movies.isEmpty());

        MediaItems firstMovie = movies.get(0);
        assertNotNull("Poster URL should not be null", firstMovie.getPosterUrl());
        assertFalse("Poster URL should not be empty", firstMovie.getPosterUrl().isEmpty());
        assertTrue("Poster URL should start with image base URL",
                firstMovie.getPosterUrl().startsWith("https://image.tmdb.org"));
    }

    @Test
    public void testGetPopularMovies_Pagination() {
        List<MediaItems> page1 = apiClient.getPopularMovies(1);
        List<MediaItems> page2 = apiClient.getPopularMovies(2);

        assertNotNull(page1);
        assertNotNull(page2);
        assertFalse(page1.isEmpty());
        assertFalse(page2.isEmpty());

        // Pages should have different content
        String firstMoviePage1 = page1.get(0).getId();
        String firstMoviePage2 = page2.get(0).getId();
        assertNotEquals("First movie on page 1 and page 2 should be different",
                firstMoviePage1, firstMoviePage2);
    }

    // ================ Popular TV Shows Tests ================

    @Test
    public void testGetPopularTVShows_ReturnsResults() {
        List<MediaItems> tvShows = apiClient.getPopularTVShows(1);

        assertNotNull("TV shows list should not be null", tvShows);
        assertFalse("TV shows list should not be empty", tvShows.isEmpty());
        assertTrue("Should return at least 10 TV shows", tvShows.size() >= 10);
    }

    @Test
    public void testGetPopularTVShows_ContainsValidData() {
        List<MediaItems> tvShows = apiClient.getPopularTVShows(1);

        assertNotNull(tvShows);
        assertFalse(tvShows.isEmpty());

        MediaItems firstShow = tvShows.get(0);
        assertNotNull("TV show should not be null", firstShow);
        assertNotNull("TV show title should not be null", firstShow.getTitle());
        assertFalse("TV show title should not be empty", firstShow.getTitle().isEmpty());
        assertEquals("Media type should be tv", "tv", firstShow.getMediaType());
    }

    // ================ Top Rated Movies Tests ================

    @Test
    public void testGetTopRatedMovies_ReturnsResults() {
        List<MediaItems> movies = apiClient.getTopRatedMovies(1);

        assertNotNull("Top rated movies list should not be null", movies);
        assertFalse("Top rated movies list should not be empty", movies.isEmpty());
    }

    @Test
    public void testGetTopRatedMovies_HasHighRatings() {
        List<MediaItems> movies = apiClient.getTopRatedMovies(1);

        assertNotNull(movies);
        assertFalse(movies.isEmpty());

        // Top rated movies should have ratings above 7.0
        for (MediaItems movie : movies) {
            assertTrue("Top rated movie should have rating above 7.0",
                    movie.getRating() >= 7.0f);
        }
    }

    // ================ Trending Tests ================

    @Test
    public void testGetTrending_Movies_Day() {
        List<MediaItems> trending = apiClient.getTrending(
                TMDBApiClient.ContentType.MOVIE,
                TMDBApiClient.TimeWindow.DAY
        );

        assertNotNull("Trending movies should not be null", trending);
        assertFalse("Trending movies should not be empty", trending.isEmpty());

        // Verify all items are movies
        for (MediaItems item : trending) {
            assertEquals("All items should be movies", "movie", item.getMediaType());
        }
    }

    @Test
    public void testGetTrending_TV_Week() {
        List<MediaItems> trending = apiClient.getTrending(
                TMDBApiClient.ContentType.TV,
                TMDBApiClient.TimeWindow.WEEK
        );

        assertNotNull("Trending TV shows should not be null", trending);
        assertFalse("Trending TV shows should not be empty", trending.isEmpty());

        // Verify all items are TV shows
        for (MediaItems item : trending) {
            assertEquals("All items should be TV shows", "tv", item.getMediaType());
        }
    }

    @Test
    public void testGetTrending_All() {
        List<MediaItems> trending = apiClient.getTrending(
                TMDBApiClient.ContentType.ALL,
                TMDBApiClient.TimeWindow.DAY
        );

        assertNotNull("Trending content should not be null", trending);
        assertFalse("Trending content should not be empty", trending.isEmpty());
    }

    // ================ Movie Details Tests ================

    @Test
    public void testGetMovieDetails_ValidId() {
        // Using a well-known movie ID (The Shawshank Redemption)
        int movieId = 278;
        MediaItems movie = apiClient.getMovieDetails(movieId);

        assertNotNull("Movie details should not be null", movie);
        assertNotNull("Movie title should not be null", movie.getTitle());
        assertEquals("Movie ID should match", String.valueOf(movieId), movie.getTmdbId());
        assertEquals("Media type should be movie", "movie", movie.getMediaType());
        assertNotNull("Movie description should not be null", movie.getDescription());
        assertTrue("Movie should be from TMDB", movie.isFromTMDB());
    }

    @Test
    public void testGetMovieDetails_HasDetailedInfo() {
        int movieId = 278; // The Shawshank Redemption
        MediaItems movie = apiClient.getMovieDetails(movieId);

        assertNotNull(movie);
        assertNotNull("Duration should not be null", movie.getDuration());
        assertFalse("Duration should not be empty", movie.getDuration().isEmpty());
        assertNotNull("Genres should not be null", movie.getGenres());
        assertFalse("Genres list should not be empty", movie.getGenres().isEmpty());
    }

    @Test
    public void testGetMovieDetails_InvalidId() {
        int invalidId = -1;
        MediaItems movie = apiClient.getMovieDetails(invalidId);

        assertNull("Invalid movie ID should return null", movie);
    }

    // ================ TV Show Details Tests ================

    @Test
    public void testGetTVShowDetails_ValidId() {
        // Using a well-known TV show ID (Breaking Bad)
        int tvShowId = 1396;
        MediaItems tvShow = apiClient.getTVShowDetails(tvShowId);

        assertNotNull("TV show details should not be null", tvShow);
        assertNotNull("TV show title should not be null", tvShow.getTitle());
        assertEquals("TV show ID should match", String.valueOf(tvShowId), tvShow.getTmdbId());
        assertEquals("Media type should be tv", "tv", tvShow.getMediaType());
        assertTrue("TV show should be from TMDB", tvShow.isFromTMDB());
    }

    @Test
    public void testGetTVShowDetails_HasDetailedInfo() {
        int tvShowId = 1396; // Breaking Bad
        MediaItems tvShow = apiClient.getTVShowDetails(tvShowId);

        assertNotNull(tvShow);
        assertNotNull("Genres should not be null", tvShow.getGenres());
        assertFalse("Genres list should not be empty", tvShow.getGenres().isEmpty());
    }

    // ================ Search Tests ================

    @Test
    public void testSearchContent_Movies() {
        String query = "Inception";
        List<MediaItems> results = apiClient.searchContent(
                query,
                TMDBApiClient.ContentType.MOVIE,
                1
        );

        assertNotNull("Search results should not be null", results);
        assertFalse("Search results should not be empty", results.isEmpty());

        // Verify results are movies
        for (MediaItems item : results) {
            assertEquals("All results should be movies", "movie", item.getMediaType());
        }

        // First result should contain the search term
        String firstTitle = results.get(0).getTitle().toLowerCase();
        assertTrue("First result should contain search term",
                firstTitle.contains(query.toLowerCase()));
    }

    @Test
    public void testSearchContent_TVShows() {
        String query = "Breaking Bad";
        List<MediaItems> results = apiClient.searchContent(
                query,
                TMDBApiClient.ContentType.TV,
                1
        );

        assertNotNull("Search results should not be null", results);
        assertFalse("Search results should not be empty", results.isEmpty());

        // Verify results are TV shows
        for (MediaItems item : results) {
            assertEquals("All results should be TV shows", "tv", item.getMediaType());
        }
    }

    @Test
    public void testSearchContent_EmptyQuery() {
        String query = "";
        List<MediaItems> results = apiClient.searchContent(
                query,
                TMDBApiClient.ContentType.MOVIE,
                1
        );

        assertNotNull("Search results should not be null for empty query", results);
        // Empty query might return empty list or popular items depending on API
    }

    @Test
    public void testSearchContent_NoResults() {
        String query = "xyzabc123nonexistentmovie999";
        List<MediaItems> results = apiClient.searchContent(
                query,
                TMDBApiClient.ContentType.MOVIE,
                1
        );

        assertNotNull("Search results should not be null", results);
        assertTrue("Search should return empty list for nonsense query", results.isEmpty());
    }

    @Test
    public void testSearchContent_WithSpaces() {
        String query = "Star Wars";
        List<MediaItems> results = apiClient.searchContent(
                query,
                TMDBApiClient.ContentType.MOVIE,
                1
        );

        assertNotNull("Search results should not be null", results);
        assertFalse("Search with spaces should return results", results.isEmpty());
    }

    // ================ Integration Tests ================

    @Test
    public void testMultipleAPICalls_Sequential() {
        // Test that multiple sequential calls work correctly
        List<MediaItems> movies = apiClient.getPopularMovies(1);
        List<MediaItems> tvShows = apiClient.getPopularTVShows(1);
        List<MediaItems> topRated = apiClient.getTopRatedMovies(1);

        assertNotNull(movies);
        assertNotNull(tvShows);
        assertNotNull(topRated);

        assertFalse(movies.isEmpty());
        assertFalse(tvShows.isEmpty());
        assertFalse(topRated.isEmpty());
    }

    @Test
    public void testDataConsistency() {
        // Get popular movies
        List<MediaItems> movies = apiClient.getPopularMovies(1);
        assertNotNull(movies);
        assertFalse(movies.isEmpty());

        // Get details for first movie
        MediaItems firstMovie = movies.get(0);
        int movieId = Integer.parseInt(firstMovie.getTmdbId());
        MediaItems detailedMovie = apiClient.getMovieDetails(movieId);

        assertNotNull(detailedMovie);
        assertEquals("Title should match", firstMovie.getTitle(), detailedMovie.getTitle());
        assertEquals("TMDB ID should match", firstMovie.getTmdbId(), detailedMovie.getTmdbId());
    }
}