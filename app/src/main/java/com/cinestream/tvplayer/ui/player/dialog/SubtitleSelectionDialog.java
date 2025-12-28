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
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.util.SubtitleItem;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class SubtitleSelectionDialog extends DialogFragment {

    private DefaultTrackSelector trackSelector;
    private ListView subtitleListView;
    private SubtitleAdapter subtitleAdapter;
    private List<SubtitleItem> subtitleItems;

    public interface OnSubtitleSelectedListener {
        void onSubtitleSelected(SubtitleItem subtitleItem);
    }

    private OnSubtitleSelectedListener listener;

    public void setOnSubtitleSelectedListener(OnSubtitleSelectedListener listener) {
        this.listener = listener;
    }

    public void setTrackSelector(DefaultTrackSelector trackSelector) {
        this.trackSelector = trackSelector;
        loadAvailableSubtitles();
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
        
        // Add subtitle options
        subtitleItems.add(new SubtitleItem("Off", "off", "No subtitles"));
        subtitleItems.add(new SubtitleItem("English", "en", "English subtitles"));
        subtitleItems.add(new SubtitleItem("Spanish", "es", "Spanish subtitles"));
        subtitleItems.add(new SubtitleItem("French", "fr", "French subtitles"));
        subtitleItems.add(new SubtitleItem("German", "de", "German subtitles"));
        subtitleItems.add(new SubtitleItem("Italian", "it", "Italian subtitles"));
        subtitleItems.add(new SubtitleItem("Portuguese", "pt", "Portuguese subtitles"));
        subtitleItems.add(new SubtitleItem("Japanese", "ja", "Japanese subtitles"));
        subtitleItems.add(new SubtitleItem("Korean", "ko", "Korean subtitles"));
        subtitleItems.add(new SubtitleItem("Chinese", "zh", "Chinese subtitles"));
        
        subtitleAdapter = new SubtitleAdapter(getContext(), subtitleItems);
        subtitleListView.setAdapter(subtitleAdapter);
        
        subtitleListView.setOnItemClickListener((parent, view, position, id) -> {
            SubtitleItem selectedSubtitle = subtitleItems.get(position);
            
            if (listener != null) {
                listener.onSubtitleSelected(selectedSubtitle);
            }
            
            // Apply subtitle selection
            applySubtitleSelection(selectedSubtitle);
            
            dismiss();
        });
    }

    private void loadAvailableSubtitles() {
        // This would typically load available subtitle tracks from the track selector
        // For now, we'll just keep the default options
    }

    private void applySubtitleSelection(SubtitleItem subtitleItem) {
        if (trackSelector != null) {
            try {
                if ("off".equals(subtitleItem.getValue())) {
                    // Disable subtitles
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    );
                } else {
                    // Enable specific subtitle language
                    // This is a simplified version - in practice, you'd need to find the actual track
                    // index for the selected language
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setPreferredTextLanguage(subtitleItem.getValue())
                    );
                }
            } catch (Exception e) {
                // Handle error silently for now
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
            
            return convertView;
        }
    }
}