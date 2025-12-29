package com.cinestream.tvplayer.ui.player.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
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

    private void initializeViews(View view) {
        subtitleListView = view.findViewById(R.id.subtitleListView);

        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());
    }

    private void setupList() {
        subtitleItems = new ArrayList<>();

        // Add "Off" option first
        subtitleItems.add(new SubtitleItem("Off", "off", "No subtitles"));

        // Add subtitles from media item if available
        if (mediaSubtitles != null && !mediaSubtitles.isEmpty()) {
            for (MediaItems.SubtitleItem subtitle : mediaSubtitles) {
                String displayName = formatSubtitleName(subtitle);
                subtitleItems.add(new SubtitleItem(
                        displayName,
                        subtitle.getLang(),
                        subtitle.getLanguage()
                ));
            }
        }

        // Load available subtitle tracks from player
        if (player != null) {
            loadAvailableSubtitlesFromPlayer();
        }

        subtitleAdapter = new SubtitleAdapter(getContext(), subtitleItems, currentSelectedIndex);
        subtitleListView.setAdapter(subtitleAdapter);

        subtitleListView.setSelection(currentSelectedIndex);
        subtitleListView.requestFocus();

        subtitleListView.setOnItemClickListener((parent, view, position, id) -> {
            SubtitleItem selectedSubtitle = subtitleItems.get(position);
            currentSelectedIndex = position;

            if (listener != null) {
                listener.onSubtitleSelected(selectedSubtitle);
            }

            applySubtitleSelection(selectedSubtitle);
            dismiss();
        });
    }

    private String formatSubtitleName(MediaItems.SubtitleItem subtitle) {
        String lang = subtitle.getLang();
        String language = subtitle.getLanguage();

        // Format: [English] [OpenSubtitles] - Subtitle 1
        StringBuilder name = new StringBuilder();
        name.append("[").append(getLanguageDisplayName(lang)).append("]");

        if (language != null && !language.isEmpty()) {
            name.append(" [").append(language).append("]");
        }

        return name.toString();
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
            case "el": return "Greek";
            case "he": return "Hebrew";
            case "id": return "Indonesia";
            default:
                return languageCode.substring(0, 1).toUpperCase() +
                        languageCode.substring(1);
        }
    }

    private void applySubtitleSelection(SubtitleItem subtitleItem) {
        if (trackSelector != null) {
            try {
                if ("off".equals(subtitleItem.getValue())) {
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setPreferredTextLanguage(null)
                                    .setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    );
                } else {
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
        private int selectedPosition;

        public SubtitleAdapter(Context context, List<SubtitleItem> items, int selectedPosition) {
            super(context, R.layout.item_subtitle, items);
            this.selectedPosition = selectedPosition;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SubtitleItem subtitleItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_subtitle, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            ImageView radioButton = convertView.findViewById(R.id.radioButton);

            titleTextView.setText(subtitleItem.getTitle());

            // Update radio button based on selection
            if (position == selectedPosition) {
                radioButton.setImageResource(R.drawable.radio_button_selected);
            } else {
                radioButton.setImageResource(R.drawable.radio_button_unselected);
            }

            return convertView;
        }
    }
}