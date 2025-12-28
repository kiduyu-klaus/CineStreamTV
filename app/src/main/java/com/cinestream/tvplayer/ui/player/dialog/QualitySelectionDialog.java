package com.cinestream.tvplayer.ui.player.dialog;

import android.app.Dialog;
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
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.util.QualityItem;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class QualitySelectionDialog extends DialogFragment {

    private DefaultTrackSelector trackSelector;
    private ListView qualityListView;
    private QualityAdapter qualityAdapter;
    private List<QualityItem> qualityItems;

    public interface OnQualitySelectedListener {
        void onQualitySelected(QualityItem qualityItem);
    }

    private OnQualitySelectedListener listener;

    public void setOnQualitySelectedListener(OnQualitySelectedListener listener) {
        this.listener = listener;
    }

    public void setTrackSelector(DefaultTrackSelector trackSelector) {
        this.trackSelector = trackSelector;
        loadAvailableQualities();
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
        
        // Add quality options
        qualityItems.add(new QualityItem("Auto", "auto", "Automatically select best quality"));
        qualityItems.add(new QualityItem("1080p", "1080", "High Definition (1920x1080)"));
        qualityItems.add(new QualityItem("720p", "720", "High Definition (1280x720)"));
        qualityItems.add(new QualityItem("480p", "480", "Standard Definition (854x480)"));
        qualityItems.add(new QualityItem("360p", "360", "Low Definition (640x360)"));
        qualityItems.add(new QualityItem("240p", "240", "Very Low Definition (426x240)"));
        
        qualityAdapter = new QualityAdapter(getContext(), qualityItems);
        qualityListView.setAdapter(qualityAdapter);
        
        qualityListView.setOnItemClickListener((parent, view, position, id) -> {
            QualityItem selectedQuality = qualityItems.get(position);
            
            if (listener != null) {
                listener.onQualitySelected(selectedQuality);
            }
            
            // Apply quality selection
            applyQualitySelection(selectedQuality);
            
            dismiss();
        });
    }

    private void loadAvailableQualities() {
        // This would typically load available qualities from the track selector
        // For now, we'll just keep the default options
    }

    private void applyQualitySelection(QualityItem qualityItem) {
        if (trackSelector != null) {
            try {
                if ("auto".equals(qualityItem.getValue())) {
                    // Set to auto (adaptive bitrate)
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setMaxVideoBitrate(Integer.MAX_VALUE)
                                    .setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE)
                    );
                } else {
                    // Set specific quality
                    int height = Integer.parseInt(qualityItem.getValue());
                    trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                    .setMaxVideoSize(height,height)
                    );
                }
            } catch (Exception e) {
                // Handle error silently for now
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
            
            return convertView;
        }
    }
}