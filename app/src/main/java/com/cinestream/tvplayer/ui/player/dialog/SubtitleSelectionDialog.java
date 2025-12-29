package com.cinestream.tvplayer.ui.player.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.media3.common.C;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.util.SubtitleItem;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class SubtitleSelectionDialog extends DialogFragment {

    private DefaultTrackSelector trackSelector;
    private ExoPlayer player;
    private ListView subtitleListView;
    private SubtitleAdapter subtitleAdapter;
    private List<SubtitleItem> subtitleItems;
    private List<MediaItems.SubtitleItem> mediaSubtitles;
    private int currentSelectedIndex = 0;

    public interface OnSubtitleSelectedListener {
        void onSubtitleSelected(SubtitleItem subtitleItem);
    }

    private OnSubtitleSelectedListener listener;

    public void setOnSubtitleSelectedListener(OnSubtitleSelectedListener listener) {
        this.listener = listener;
    }

    public void setTrackSelector(DefaultTrackSelector trackSelector, ExoPlayer player) {
        this.trackSelector = trackSelector;
        this.player = player;
    }

    public void setMediaSubtitles(List<MediaItems.SubtitleItem> subtitles) {
        this.mediaSubtitles = subtitles;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_subtitle_selection, container, false);

        initializeViews(view);
        setupList();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set dialog properties
        if (getDialog() != null) {
            getDialog().setTitle("Select Subtitles");
        }
    }

    private void initializeViews(View view) {
        subtitleListView = view.findViewById(R.id.subtitleListView);
    }

    private void setupList() {
        subtitleItems = new ArrayList<>();

        // Always add "Off" option first
        subtitleItems.add(new SubtitleItem("Off", "off", "No subtitles"));

        // Add subtitles from media item if available
        if (mediaSubtitles != null && !mediaSubtitles.isEmpty()) {
            for (MediaItems.SubtitleItem subtitle : mediaSubtitles) {
                String displayName = getLanguageDisplayName(subtitle.getLang());
                String description = subtitle.getLanguage() != null ?
                        subtitle.getLanguage() : displayName + " subtitles";

                subtitleItems.add(new SubtitleItem(
                        displayName,
                        subtitle.getLang(),
                        description
                ));
            }
        }

        // Load available subtitle tracks from player
        if (player != null) {
            loadAvailableSubtitlesFromPlayer();
        }

        // If no subtitles found, show message
        if (subtitleItems.size() == 1) {
            subtitleItems.add(new SubtitleItem(
                    "No subtitles available",
                    "none",
                    "This video has no subtitle tracks"
            ));
        }

        subtitleAdapter = new SubtitleAdapter(getContext(), subtitleItems);
        subtitleListView.setAdapter(subtitleAdapter);

        // Highlight current selection
        if (currentSelectedIndex >= 0 && currentSelectedIndex < subtitleItems.size()) {
            subtitleListView.setSelection(currentSelectedIndex);
        }

        subtitleListView.setOnItemClickListener((parent, view, position, id) -> {
            SubtitleItem selectedSubtitle = subtitleItems.get(position);

            // Don't process the "no subtitles" message
            if ("none".equals(selectedSubtitle.getValue())) {
                return;
            }

            currentSelectedIndex = position;

            if (listener != null) {
                listener.onSubtitleSelected(selectedSubtitle);
            }

            // Apply subtitle selection
            applySubtitleSelection(selectedSubtitle);

            dismiss();
        });
    }

    private void loadAvailableSubtitlesFromPlayer() {
        if (player == null) return;

        try {
            Tracks tracks = player.getCurrentTracks();
            List<Tracks.Group> trackGroups = tracks.getGroups();

            for (Tracks.Group group : trackGroups) {
                if (group.getType() == C.TRACK_TYPE_TEXT) {
                    for (int i = 0; i < group.length; i++) {
                        if (group.isTrackSupported(i)) {
                            String language = group.getMediaTrackGroup().getFormat(i).language;
                            if (language != null && !language.isEmpty()) {
                                // Check if this language is already in the list
                                boolean alreadyExists = false;
                                for (SubtitleItem item : subtitleItems) {
                                    if (item.getValue().equals(language)) {
                                        alreadyExists = true;
                                        break;
                                    }
                                }

                                if (!alreadyExists) {
                                    String displayName = getLanguageDisplayName(language);
                                    subtitleItems.add(new SubtitleItem(
                                            displayName,
                                            language,
                                            displayName + " subtitles"
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SubtitleDialog", "Error loading player tracks", e);
        }
    }

    private String getLanguageDisplayName(String languageCode) {
        if (languageCode == null) return "Unknown";

        // Map common language codes to display names
        switch (languageCode.toLowerCase()) {
            case "en": return "English";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "zh": return "Chinese";
            case "ar": return "Arabic";
            case "ru": return "Russian";
            case "hi": return "Hindi";
            case "nl": return "Dutch";
            case "sv": return "Swedish";
            case "no": return "Norwegian";
            case "da": return "Danish";
            case "fi": return "Finnish";
            case "pl": return "Polish";
            case "tr": return "Turkish";
            case "th": return "Thai";
            case "vi": return "Vietnamese";
            case "id": return "Indonesian";
            case "ms": return "Malay";
            case "he": return "Hebrew";
            case "cs": return "Czech";
            case "hu": return "Hungarian";
            case "ro": return "Romanian";
            case "uk": return "Ukrainian";
            case "el": return "Greek";
            default:
                // Capitalize first letter
                return languageCode.substring(0, 1).toUpperCase() +
                        languageCode.substring(1);
        }
    }

    private void applySubtitleSelection(SubtitleItem subtitleItem) {
        if (trackSelector != null) {
            try {
                if ("off".equals(subtitleItem.getValue())) {
                    // Disable subtitles
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setPreferredTextLanguage(null)
                                    .setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    );
                } else {
                    // Enable specific subtitle language
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setPreferredTextLanguage(subtitleItem.getValue())
                                    .setDisabledTextTrackSelectionFlags(0)
                    );
                }
            } catch (Exception e) {
                android.util.Log.e("SubtitleDialog", "Error applying subtitle selection", e);
            }
        }
    }

    private static class SubtitleAdapter extends ArrayAdapter<SubtitleItem> {

        public SubtitleAdapter(Context context, List<SubtitleItem> items) {
            super(context, R.layout.item_subtitle, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SubtitleItem subtitleItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_subtitle, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            TextView subtitleTextView = convertView.findViewById(R.id.subtitleTextView);

            titleTextView.setText(subtitleItem.getTitle());
            subtitleTextView.setText(subtitleItem.getDescription());

            // Make "no subtitles" item look disabled
            if ("none".equals(subtitleItem.getValue())) {
                titleTextView.setAlpha(0.5f);
                subtitleTextView.setAlpha(0.5f);
            } else {
                titleTextView.setAlpha(1.0f);
                subtitleTextView.setAlpha(1.0f);
            }

            return convertView;
        }
    }
}