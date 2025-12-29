package com.cinestream.tvplayer.ui.player.dialog;

import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;

import java.util.ArrayList;
import java.util.List;

public class ServerSelectionDialog extends DialogFragment {

    private ListView serversListView;
    private ServerAdapter serverAdapter;
    private List<ServerItem> serverItems;
    private List<MediaItems.VideoSource> videoSources;
    private int currentSelectedIndex = 0;

    public interface OnServerSelectedListener {
        void onServerSelected(ServerItem serverItem);
    }

    private OnServerSelectedListener listener;

    public void setOnServerSelectedListener(OnServerSelectedListener listener) {
        this.listener = listener;
    }

    public void setVideoSources(List<MediaItems.VideoSource> sources) {
        this.videoSources = sources;
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
        View view = inflater.inflate(R.layout.dialog_servers_selection, container, false);

        initializeViews(view);
        setupList();

        return view;
    }

    private void initializeViews(View view) {
        serversListView = view.findViewById(R.id.serversListView);

        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());
    }

    private void setupList() {
        serverItems = new ArrayList<>();

        // Add servers from video sources if available
        if (videoSources != null && !videoSources.isEmpty()) {
            char serverLetter = 'A';
            int serverNumber = 1;

            for (MediaItems.VideoSource source : videoSources) {
                String quality = source.getQuality();
                String serverName = "Server " + serverLetter + serverNumber + " - " + quality;
                serverItems.add(new ServerItem(serverName, source.getUrl(), quality));

                serverNumber++;
                if (serverNumber > 3) {
                    serverNumber = 1;
                    serverLetter++;
                }
            }
        }

        // If no servers, add default message
        if (serverItems.isEmpty()) {
            serverItems.add(new ServerItem("No servers available", "", ""));
        }

        serverAdapter = new ServerAdapter(getContext(), serverItems, currentSelectedIndex);
        serversListView.setAdapter(serverAdapter);

        serversListView.setSelection(currentSelectedIndex);
        serversListView.requestFocus();

        serversListView.setOnItemClickListener((parent, view, position, id) -> {
            ServerItem selectedServer = serverItems.get(position);

            if (selectedServer.getUrl().isEmpty()) {
                return; // Don't process empty servers
            }

            currentSelectedIndex = position;

            if (listener != null) {
                listener.onServerSelected(selectedServer);
            }

            dismiss();
        });
    }

    public static class ServerItem {
        private String name;
        private String url;
        private String quality;

        public ServerItem(String name, String url, String quality) {
            this.name = name;
            this.url = url;
            this.quality = quality;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getQuality() {
            return quality;
        }
    }

    private static class ServerAdapter extends ArrayAdapter<ServerItem> {
        private int selectedPosition;

        public ServerAdapter(Context context, List<ServerItem> items, int selectedPosition) {
            super(context, R.layout.item_server, items);
            this.selectedPosition = selectedPosition;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ServerItem serverItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_server, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            ImageView radioButton = convertView.findViewById(R.id.radioButton);

            titleTextView.setText(serverItem.getName());

            // Update radio button based on selection
            if (position == selectedPosition) {
                radioButton.setImageResource(R.drawable.radio_button_selected);
            } else {
                radioButton.setImageResource(R.drawable.radio_button_unselected);
            }

            // Disable if no URL
            if (serverItem.getUrl().isEmpty()) {
                titleTextView.setAlpha(0.5f);
                radioButton.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}