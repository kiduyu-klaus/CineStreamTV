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

}