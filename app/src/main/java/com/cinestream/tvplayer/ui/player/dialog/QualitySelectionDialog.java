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
import androidx.media3.common.Format;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.util.QualityItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UnstableApi
public class QualitySelectionDialog extends DialogFragment {

    private DefaultTrackSelector trackSelector;
    private ExoPlayer player;
    private ListView qualityListView;
    private QualityAdapter qualityAdapter;
    private List<QualityItem> qualityItems;
    private List<MediaItems.VideoSource> videoSources;
    private int currentSelectedIndex = 0;

    public interface OnQualitySelectedListener {
        void onQualitySelected(QualityItem qualityItem);
    }

    private OnQualitySelectedListener listener;

    public void setOnQualitySelectedListener(OnQualitySelectedListener listener) {
        this.listener = listener;
    }

    public void setTrackSelector(DefaultTrackSelector trackSelector, ExoPlayer player) {
        this.trackSelector = trackSelector;
        this.player = player;
    }

    public void setVideoSources(List<MediaItems.VideoSource> sources) {
        this.videoSources = sources;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_quality_selection, container, false);

        initializeViews(view);
        setupList();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set dialog properties
        if (getDialog() != null) {
            getDialog().setTitle("Select Quality");
        }
    }

    private void initializeViews(View view) {
        qualityListView = view.findViewById(R.id.qualityListView);
    }

    private void setupList() {
        qualityItems = new ArrayList<>();
        Set<String> addedQualities = new HashSet<>();

        // Always add "Auto" option first
        qualityItems.add(new QualityItem("Auto", "auto", "Automatically select best quality"));
        addedQualities.add("auto");

        // Add qualities from video sources if available
        if (videoSources != null && !videoSources.isEmpty()) {
            for (MediaItems.VideoSource source : videoSources) {
                String quality = source.getQuality();
                if (quality != null && !addedQualities.contains(quality.toLowerCase())) {
                    String description = getQualityDescription(quality);
                    qualityItems.add(new QualityItem(quality, quality.toLowerCase(), description));
                    addedQualities.add(quality.toLowerCase());
                }
            }
        }

        // Load available quality tracks from player
        if (player != null) {
            loadAvailableQualitiesFromPlayer(addedQualities);
        }

        // Sort qualities by resolution (highest first)
        sortQualitiesByResolution();

        // If only Auto is available, add a message
        if (qualityItems.size() == 1) {
            qualityItems.add(new QualityItem(
                    "Auto quality only",
                    "none",
                    "No manual quality options available"
            ));
        }

        qualityAdapter = new QualityAdapter(getContext(), qualityItems);
        qualityListView.setAdapter(qualityAdapter);

        // Highlight current selection
        if (currentSelectedIndex >= 0 && currentSelectedIndex < qualityItems.size()) {
            qualityListView.setSelection(currentSelectedIndex);
        }

        qualityListView.setOnItemClickListener((parent, view, position, id) -> {
            QualityItem selectedQuality = qualityItems.get(position);

            // Don't process the "no quality" message
            if ("none".equals(selectedQuality.getValue())) {
                return;
            }

            currentSelectedIndex = position;

            if (listener != null) {
                listener.onQualitySelected(selectedQuality);
            }

            // Apply quality selection
            applyQualitySelection(selectedQuality);

            dismiss();
        });
    }

    private void loadAvailableQualitiesFromPlayer(Set<String> addedQualities) {
        if (player == null) return;

        try {
            Tracks tracks = player.getCurrentTracks();
            List<Tracks.Group> trackGroups = tracks.getGroups();

            for (Tracks.Group group : trackGroups) {
                if (group.getType() == C.TRACK_TYPE_VIDEO) {
                    for (int i = 0; i < group.length; i++) {
                        if (group.isTrackSupported(i)) {
                            Format format = group.getMediaTrackGroup().getFormat(i);
                            int height = format.height;

                            if (height > 0) {
                                String quality = height + "p";
                                String qualityKey = quality.toLowerCase();

                                if (!addedQualities.contains(qualityKey)) {
                                    String description = getQualityDescription(quality);
                                    qualityItems.add(new QualityItem(quality, qualityKey, description));
                                    addedQualities.add(qualityKey);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("QualityDialog", "Error loading player tracks", e);
        }
    }

    private String getQualityDescription(String quality) {
        if (quality == null) return "Unknown quality";

        String lowerQuality = quality.toLowerCase().replace("p", "");

        try {
            int height = Integer.parseInt(lowerQuality);

            if (height >= 2160) {
                return "Ultra HD 4K (" + getResolutionFromHeight(height) + ")";
            } else if (height >= 1440) {
                return "Quad HD (" + getResolutionFromHeight(height) + ")";
            } else if (height >= 1080) {
                return "Full HD (" + getResolutionFromHeight(height) + ")";
            } else if (height >= 720) {
                return "HD (" + getResolutionFromHeight(height) + ")";
            } else if (height >= 480) {
                return "Standard Definition (" + getResolutionFromHeight(height) + ")";
            } else if (height >= 360) {
                return "Low Definition (" + getResolutionFromHeight(height) + ")";
            } else {
                return "Very Low (" + getResolutionFromHeight(height) + ")";
            }
        } catch (NumberFormatException e) {
            // If quality is not a number, return as is
            return quality;
        }
    }

    private String getResolutionFromHeight(int height) {
        // Estimate width based on 16:9 aspect ratio
        int width = (int) (height * 16.0 / 9.0);
        return width + "x" + height;
    }

    private void sortQualitiesByResolution() {
        // Keep Auto at the top, sort the rest by resolution
        if (qualityItems.size() <= 1) return;

        QualityItem autoItem = qualityItems.get(0);
        List<QualityItem> otherItems = new ArrayList<>(qualityItems.subList(1, qualityItems.size()));

        Collections.sort(otherItems, (q1, q2) -> {
            try {
                int height1 = Integer.parseInt(q1.getValue().replace("p", ""));
                int height2 = Integer.parseInt(q2.getValue().replace("p", ""));
                return Integer.compare(height2, height1); // Descending order
            } catch (NumberFormatException e) {
                return 0;
            }
        });

        qualityItems.clear();
        qualityItems.add(autoItem);
        qualityItems.addAll(otherItems);
    }

    private void applyQualitySelection(QualityItem qualityItem) {
        if (trackSelector != null) {
            try {
                if ("auto".equals(qualityItem.getValue())) {
                    // Set to auto (adaptive bitrate)
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .clearVideoSizeConstraints()
                                    .setMaxVideoBitrate(Integer.MAX_VALUE)
                    );
                } else {
                    // Set specific quality
                    try {
                        int height = Integer.parseInt(qualityItem.getValue().replace("p", ""));
                        // Set max height with some tolerance
                        trackSelector.setParameters(
                                trackSelector.buildUponParameters()
                                        .setMaxVideoSize(Integer.MAX_VALUE, height)
                                        .setMinVideoSize(0, height - 50) // Allow some tolerance
                        );
                    } catch (NumberFormatException e) {
                        android.util.Log.e("QualityDialog", "Invalid quality format: " + qualityItem.getValue());
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("QualityDialog", "Error applying quality selection", e);
            }
        }
    }

    private static class QualityAdapter extends ArrayAdapter<QualityItem> {

        public QualityAdapter(Context context, List<QualityItem> items) {
            super(context, R.layout.item_quality, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            QualityItem qualityItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_quality, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            TextView subtitleTextView = convertView.findViewById(R.id.subtitleTextView);

            titleTextView.setText(qualityItem.getTitle());
            subtitleTextView.setText(qualityItem.getDescription());

            // Make "no quality" item look disabled
            if ("none".equals(qualityItem.getValue())) {
                titleTextView.setAlpha(0.5f);
                subtitleTextView.setAlpha(0.5f);
            } else {
                titleTextView.setAlpha(1.0f);
                subtitleTextView.setAlpha(1.0f);
            }

            convertView.setFocusable(true);
            convertView.setFocusableInTouchMode(true);
            convertView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start();
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                }
            });

            return convertView;
        }
    }
}