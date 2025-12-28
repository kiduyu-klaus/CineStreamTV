package com.cinestream.tvplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import androidx.media3.common.C;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class SubtitleManager {
    private static final String TAG = "SubtitleManager";
    private static final String PREFS_NAME = "subtitle_preferences";
    private static final String KEY_SUBTITLE_ENABLED = "subtitle_enabled";
    private static final String KEY_SUBTITLE_LANGUAGE = "subtitle_language";
    private static final String KEY_SUBTITLE_SIZE = "subtitle_size";
    private static final String KEY_SUBTITLE_COLOR = "subtitle_color";
    private static final String KEY_SUBTITLE_BG_COLOR = "subtitle_bg_color";

    // Default subtitle settings
    private static final boolean DEFAULT_ENABLED = true;
    private static final String DEFAULT_LANGUAGE = "en";
    private static final int DEFAULT_SIZE = 16;
    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;

    private Context context;
    private SharedPreferences sharedPreferences;
    private DefaultTrackSelector trackSelector;

    /**
     * Constructor requires context
     */
    public SubtitleManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setTrackSelector(DefaultTrackSelector trackSelector) {
        this.trackSelector = trackSelector;
    }

    /**
     * Enable or disable subtitles
     */
    public void setSubtitlesEnabled(boolean enabled) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putBoolean(KEY_SUBTITLE_ENABLED, enabled)
                    .apply();
        }
    }

    /**
     * Check if subtitles are enabled
     */
    public boolean isSubtitlesEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(KEY_SUBTITLE_ENABLED, DEFAULT_ENABLED);
        }
        return DEFAULT_ENABLED;
    }

    /**
     * Set subtitle language
     */
    public void setSubtitleLanguage(String language) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putString(KEY_SUBTITLE_LANGUAGE, language)
                    .apply();
        }
    }

    /**
     * Get subtitle language
     */
    public String getSubtitleLanguage() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_SUBTITLE_LANGUAGE, DEFAULT_LANGUAGE);
        }
        return DEFAULT_LANGUAGE;
    }

    /**
     * Set subtitle text size
     */
    public void setSubtitleSize(int size) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putInt(KEY_SUBTITLE_SIZE, size)
                    .apply();
        }
    }

    /**
     * Get subtitle text size
     */
    public int getSubtitleSize() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(KEY_SUBTITLE_SIZE, DEFAULT_SIZE);
        }
        return DEFAULT_SIZE;
    }

    /**
     * Set subtitle text color
     */
    public void setSubtitleColor(int color) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putInt(KEY_SUBTITLE_COLOR, color)
                    .apply();
        }
    }

    /**
     * Get subtitle text color
     */
    public int getSubtitleColor() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(KEY_SUBTITLE_COLOR, DEFAULT_COLOR);
        }
        return DEFAULT_COLOR;
    }

    /**
     * Set subtitle background color
     */
    public void setSubtitleBackgroundColor(int color) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putInt(KEY_SUBTITLE_BG_COLOR, color)
                    .apply();
        }
    }

    /**
     * Get subtitle background color
     */
    public int getSubtitleBackgroundColor() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(KEY_SUBTITLE_BG_COLOR, DEFAULT_BG_COLOR);
        }
        return DEFAULT_BG_COLOR;
    }

    /**
     * Apply subtitle settings to track selector
     */
    public void applySubtitleSettings() {
        if (trackSelector != null) {
            try {
                boolean enabled = isSubtitlesEnabled();
                String language = getSubtitleLanguage();

                TrackSelectionParameters.Builder builder = trackSelector.buildUponParameters();

                if (enabled && !"off".equals(language)) {
                    // Enable text tracks with preferred language
                    builder.setPreferredTextLanguage(language)
                            .setPreferredTextRoleFlags(C.ROLE_FLAG_SUBTITLE);
                } else {
                    // Disable text tracks
                    builder.setPreferredTextLanguage(null)
                            .setIgnoredTextSelectionFlags(~C.SELECTION_FLAG_FORCED);
                }

                trackSelector.setParameters((DefaultTrackSelector.Parameters.Builder)builder);

                Log.d(TAG, "Subtitle settings applied: enabled=" + enabled + ", language=" + language);

            } catch (Exception e) {
                Log.e(TAG, "Error applying subtitle settings", e);
            }
        } else {
            Log.w(TAG, "TrackSelector is null, cannot apply subtitle settings");
        }
    }

    /**
     * Get available subtitle tracks from the player
     */
    public List<TextTrack> getAvailableSubtitleTracks(Tracks tracks) {
        List<TextTrack> textTracks = new ArrayList<>();

        if (tracks == null) {
            return textTracks;
        }

        try {
            for (Tracks.Group trackGroup : tracks.getGroups()) {
                if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                    for (int i = 0; i < trackGroup.length; i++) {
                        if (trackGroup.isTrackSupported(i)) {
                            androidx.media3.common.Format format = trackGroup.getTrackFormat(i);
                            String language = format.language != null ? format.language : "Unknown";
                            String label = format.label != null ? format.label : language;

                            TextTrack textTrack = new TextTrack(
                                    format.id,
                                    language,
                                    label,
                                    trackGroup.isTrackSelected(i)
                            );
                            textTracks.add(textTrack);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting available subtitle tracks", e);
        }

        return textTracks;
    }

    /**
     * Select subtitle track by language
     */
    public void selectSubtitleTrack(String language) {
        if (trackSelector != null && language != null) {
            try {
                TrackSelectionParameters.Builder builder = trackSelector.buildUponParameters();

                if ("off".equals(language) || "none".equals(language)) {
                    // Disable subtitles
                    builder.setPreferredTextLanguage(null)
                            .setIgnoredTextSelectionFlags(~C.SELECTION_FLAG_FORCED);
                    setSubtitlesEnabled(false);
                } else {
                    // Enable subtitles with selected language
                    builder.setPreferredTextLanguage(language)
                            .setPreferredTextRoleFlags(C.ROLE_FLAG_SUBTITLE);
                    setSubtitlesEnabled(true);
                    setSubtitleLanguage(language);
                }

                trackSelector.setParameters((DefaultTrackSelector.Parameters.Builder)builder);
                Log.d(TAG, "Selected subtitle track: " + language);

            } catch (Exception e) {
                Log.e(TAG, "Error selecting subtitle track", e);
            }
        }
    }

    /**
     * Find subtitle track by language
     */
    public TextTrack findSubtitleTrackByLanguage(String language, List<TextTrack> subtitleTracks) {
        if (subtitleTracks == null || language == null) {
            return null;
        }

        for (TextTrack track : subtitleTracks) {
            if (language.equals(track.getLanguage())) {
                return track;
            }
        }
        return null;
    }

    /**
     * Get supported subtitle formats
     */
    public String[] getSupportedSubtitleFormats() {
        return new String[]{
                "WebVTT (.vtt)",
                "SubRip (.srt)",
                "SubStation Alpha (.ssa/.ass)",
                "TTML (.ttml/.xml)",
                "SAMI (.smi)"
        };
    }

    /**
     * Validate subtitle URL
     */
    public boolean isValidSubtitleUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Check for supported subtitle file extensions
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".vtt") ||
                lowerUrl.endsWith(".srt") ||
                lowerUrl.endsWith(".ssa") ||
                lowerUrl.endsWith(".ass") ||
                lowerUrl.endsWith(".smi") ||
                lowerUrl.endsWith(".ttml") ||
                lowerUrl.endsWith(".xml");
    }

    /**
     * Configure subtitle TextView appearance
     */
    public void configureSubtitleTextView(TextView subtitleTextView) {
        if (subtitleTextView != null) {
            subtitleTextView.setTextSize(getSubtitleSize());
            subtitleTextView.setTextColor(getSubtitleColor());
            subtitleTextView.setBackgroundColor(getSubtitleBackgroundColor());
        }
    }

    /**
     * Reset subtitle settings to defaults
     */
    public void resetToDefaults() {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putBoolean(KEY_SUBTITLE_ENABLED, DEFAULT_ENABLED)
                    .putString(KEY_SUBTITLE_LANGUAGE, DEFAULT_LANGUAGE)
                    .putInt(KEY_SUBTITLE_SIZE, DEFAULT_SIZE)
                    .putInt(KEY_SUBTITLE_COLOR, DEFAULT_COLOR)
                    .putInt(KEY_SUBTITLE_BG_COLOR, DEFAULT_BG_COLOR)
                    .apply();
        }
    }

    /**
     * Get subtitle settings as a bundle for easy transfer
     */
    public SubtitleSettings getSubtitleSettings() {
        return new SubtitleSettings(
                isSubtitlesEnabled(),
                getSubtitleLanguage(),
                getSubtitleSize(),
                getSubtitleColor(),
                getSubtitleBackgroundColor()
        );
    }

    /**
     * Apply subtitle settings from a settings object
     */
    public void applySubtitleSettings(SubtitleSettings settings) {
        if (settings != null) {
            setSubtitlesEnabled(settings.isEnabled());
            setSubtitleLanguage(settings.getLanguage());
            setSubtitleSize(settings.getSize());
            setSubtitleColor(settings.getColor());
            setSubtitleBackgroundColor(settings.getBackgroundColor());
            applySubtitleSettings();
        }
    }

    /**
     * Class to represent a text track
     */
    public static class TextTrack {
        private final String id;
        private final String language;
        private final String label;
        private final boolean selected;

        public TextTrack(String id, String language, String label, boolean selected) {
            this.id = id;
            this.language = language;
            this.label = label;
            this.selected = selected;
        }

        public String getId() { return id; }
        public String getLanguage() { return language; }
        public String getLabel() { return label; }
        public boolean isSelected() { return selected; }

        @Override
        public String toString() {
            return label + " (" + language + ")";
        }
    }

    /**
     * Class to hold subtitle settings
     */
    public static class SubtitleSettings {
        private final boolean enabled;
        private final String language;
        private final int size;
        private final int color;
        private final int backgroundColor;

        public SubtitleSettings(boolean enabled, String language, int size,
                                int color, int backgroundColor) {
            this.enabled = enabled;
            this.language = language;
            this.size = size;
            this.color = color;
            this.backgroundColor = backgroundColor;
        }

        public boolean isEnabled() { return enabled; }
        public String getLanguage() { return language; }
        public int getSize() { return size; }
        public int getColor() { return color; }
        public int getBackgroundColor() { return backgroundColor; }

        @Override
        public String toString() {
            return "SubtitleSettings{" +
                    "enabled=" + enabled +
                    ", language='" + language + '\'' +
                    ", size=" + size +
                    ", color=" + color +
                    ", backgroundColor=" + backgroundColor +
                    '}';
        }
    }
}