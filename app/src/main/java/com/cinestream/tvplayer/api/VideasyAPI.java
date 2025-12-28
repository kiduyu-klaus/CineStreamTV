package com.cinestream.tvplayer.api;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class VideasyAPI {
    private static final String TAG = "VideasyAPI";
    private static final String API_BASE = "https://enc-dec.app/api";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    private static final String[] SERVERS = {
        "myflixerzupcloud", // Neon
        "1movies",          // Sage
        "moviebox",         // Cypher
        "primewire",        // Reyna
        "onionplay",        // Omen
        "m4uhd",            // Breach
        "hdmovie"           // Vyse
    };

    // Result classes
    public static class VideoSource {
        public String quality;
        public String url;

        public VideoSource(String quality, String url) {
            this.quality = quality;
            this.url = url;
        }

        @Override
        public String toString() {
            return "Quality: " + quality + "\nURL: " + url;
        }
    }

    public static class Subtitle {
        public String url;
        public String lang;
        public String language;

        public Subtitle(String url, String lang, String language) {
            this.url = url;
            this.lang = lang;
            this.language = language;
        }

        @Override
        public String toString() {
            return "Language: " + language + " (" + lang + ")\nURL: " + url;
        }
    }

    public static class VideasyResult {
        public List<VideoSource> sources;
        public List<Subtitle> subtitles;
        public String error;
        public boolean success;

        public VideasyResult() {
            this.sources = new ArrayList<>();
            this.subtitles = new ArrayList<>();
            this.success = false;
        }

        @Override
        public String toString() {
            if (!success) {
                return "Error: " + error;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("=== VIDEO SOURCES ===\n");
            for (int i = 0; i < sources.size(); i++) {
                sb.append("\n[Source ").append(i + 1).append("]\n");
                sb.append(sources.get(i).toString()).append("\n");
            }
            
            if (!subtitles.isEmpty()) {
                sb.append("\n=== SUBTITLES ===\n");
                for (int i = 0; i < subtitles.size(); i++) {
                    sb.append("\n[Subtitle ").append(i + 1).append("]\n");
                    sb.append(subtitles.get(i).toString()).append("\n");
                }
            }
            
            return sb.toString();
        }
    }

    /**
     * Fetch encrypted data from Videasy API
     * @param title - Movie/TV show title
     * @param mediaType - "movie" or "tv"
     * @param year - Release year
     * @param tmdbId - TMDB ID
     * @param season - Season number (for TV shows, null for movies)
     * @param episode - Episode number (for TV shows, null for movies)
     * @param server - Server name (from SERVERS array)
     * @return Encrypted data string or error message
     */
    public static String fetchEncryptedData(String title, String mediaType, String year, 
                                           String tmdbId, String season, String episode, 
                                           String server) {
        HttpURLConnection conn = null;
        try {
            // Build URL
            String encodedTitle = URLEncoder.encode(title, "UTF-8");
            String baseUrl = "https://api.videasy.net/" + server + "/sources-with-title";
            
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?title=").append(encodedTitle);
            urlBuilder.append("&mediaType=").append(mediaType);
            urlBuilder.append("&year=").append(year);
            urlBuilder.append("&tmdbId=").append(tmdbId);
            
            if ("tv".equals(mediaType) && season != null && episode != null) {
                urlBuilder.append("&seasonId=").append(season);
                urlBuilder.append("&episodeId=").append(episode);
            }
            
            URL url = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "ERROR: HTTP " + responseCode;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String result = response.toString().trim();
            if (result.isEmpty()) {
                return "ERROR: Empty response from server";
            }
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching encrypted data", e);
            return "ERROR: " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Decrypt data from Videasy API
     * @param encryptedText - Encrypted text from fetchEncryptedData
     * @param tmdbId - TMDB ID
     * @return Decrypted JSON string or error message
     */
    public static String decryptData(String encryptedText, String tmdbId) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API_BASE + "/dec-videasy");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // Create JSON payload
            JSONObject payload = new JSONObject();
            payload.put("text", encryptedText);
            payload.put("id", tmdbId);
            
            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "ERROR: HTTP " + responseCode;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            // Parse response
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (!jsonResponse.has("result")) {
                return "ERROR: Invalid response format";
            }
            
            return jsonResponse.getString("result");
            
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return "ERROR: " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Fetch and decrypt video sources with automatic server fallback
     * @param title - Movie/TV show title
     * @param mediaType - "movie" or "tv"
     * @param year - Release year
     * @param tmdbId - TMDB ID
     * @param season - Season number (for TV shows, null for movies)
     * @param episode - Episode number (for TV shows, null for movies)
     * @return VideasyResult object containing sources and subtitles or error
     */
    public static VideasyResult getVideoSources(String title, String mediaType, String year,
                                               String tmdbId, String season, String episode) {
        VideasyResult result = new VideasyResult();
        
        for (String server : SERVERS) {
            try {
                Log.d(TAG, "Trying server: " + server);
                
                // Fetch encrypted data
                String encryptedData = fetchEncryptedData(title, mediaType, year, tmdbId, 
                                                         season, episode, server);
                
                if (encryptedData.startsWith("ERROR:")) {
                    Log.w(TAG, server + " - " + encryptedData);
                    continue;
                }
                
                // Decrypt data
                String decryptedData = decryptData(encryptedData, tmdbId);
                
                if (decryptedData.startsWith("ERROR:")) {
                    Log.w(TAG, server + " - " + decryptedData);
                    continue;
                }
                
                // Parse JSON
                JSONObject jsonData = new JSONObject(decryptedData);
                
                // Parse sources
                if (jsonData.has("sources")) {
                    JSONArray sources = jsonData.getJSONArray("sources");
                    for (int i = 0; i < sources.length(); i++) {
                        JSONObject source = sources.getJSONObject(i);
                        String quality = source.getString("quality");
                        String sourceUrl = source.getString("url");
                        
                        if (!sourceUrl.isEmpty()) {
                            result.sources.add(new VideoSource(quality, sourceUrl));
                        }
                    }
                }
                
                // Parse subtitles
                if (jsonData.has("subtitles")) {
                    JSONArray subtitles = jsonData.getJSONArray("subtitles");
                    for (int i = 0; i < subtitles.length(); i++) {
                        JSONObject subtitle = subtitles.getJSONObject(i);
                        String subUrl = subtitle.getString("url");
                        String lang = subtitle.getString("lang");
                        String language = subtitle.getString("language");
                        
                        if (!subUrl.isEmpty()) {
                            result.subtitles.add(new Subtitle(subUrl, lang, language));
                        }
                    }
                }
                
                // If we found sources, return success
                if (!result.sources.isEmpty()) {
                    result.success = true;
                    Log.d(TAG, "Successfully fetched data from server: " + server);
                    return result;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error with server " + server, e);
            }
        }
        
        // All servers failed
        result.success = false;
        result.error = "Failed to fetch data from all available servers";
        return result;
    }
}