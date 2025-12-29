package com.cinestream.tvplayer.ui.player.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
    private MediaItems mediaItem;
    private int currentSelectedIndex = 0;

    public interface OnServerSelectedListener {
        void onServerSelected(ServerItem serverItem);
    }

    private OnServerSelectedListener listener;
    private View.OnClickListener viewClickListener;

    public void setOnServerSelectedListener(OnServerSelectedListener listener) {
        this.listener = listener;
    }
    public void ViewClickListener(View.OnClickListener viewClickListener) {
        //Log.i("PlayerActivity", "ViewClickListener: clicked"+serverAdapter.getSelectedPosition());
       //serverAdapter.setSelectedPosition(currentSelectedIndex);
        //Log.i("PlayerActivity", "ViewClickListener: "+serverAdapter.getSelectedPosition());
        this.viewClickListener = viewClickListener;
    }


    public void setMediaItem(MediaItems mediaItem) {
        this.mediaItem = mediaItem;
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

        // Determine if it's a movie
        boolean isMovie = mediaItem != null && "movie".equals(mediaItem.getMediaType().toLowerCase());

        // Neon servers
        serverItems.add(new ServerItem("Neon (Primary)", "https://api.videasy.net/myflixerzupcloud/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Neon (Backup)", "https://api2.videasy.net/myflixerzupcloud/sources-with-title", "Original", true));

        // Sage servers
        serverItems.add(new ServerItem("Sage (Primary)", "https://api.videasy.net/1movies/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Sage (Backup)", "https://api2.videasy.net/1movies/sources-with-title", "Original", true));

        // Cypher servers
        serverItems.add(new ServerItem("Cypher (Primary)", "https://api.videasy.net/moviebox/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Cypher (Backup)", "https://api2.videasy.net/moviebox/sources-with-title", "Original", true));

        // Yoru servers (Movies only)
        if (isMovie) {
            serverItems.add(new ServerItem("Yoru (Primary)", "https://api.videasy.net/cdn/sources-with-title", "Original", true));
            serverItems.add(new ServerItem("Yoru (Backup)", "https://api2.videasy.net/cdn/sources-with-title", "Original", true));
        }

        // Reyna servers
        serverItems.add(new ServerItem("Reyna (Primary)", "https://api.videasy.net/primewire/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Reyna (Backup)", "https://api2.videasy.net/primewire/sources-with-title", "Original", true));

        // Omen servers
        serverItems.add(new ServerItem("Omen (Primary)", "https://api.videasy.net/onionplay/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Omen (Backup)", "https://api2.videasy.net/onionplay/sources-with-title", "Original", true));

        // Breach servers
        serverItems.add(new ServerItem("Breach (Primary)", "https://api.videasy.net/m4uhd/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Breach (Backup)", "https://api2.videasy.net/m4uhd/sources-with-title", "Original", true));

        // Vyse servers
        serverItems.add(new ServerItem("Vyse (Primary)", "https://api.videasy.net/hdmovie/sources-with-title", "Original", true));
        serverItems.add(new ServerItem("Vyse (Backup)", "https://api2.videasy.net/hdmovie/sources-with-title", "Original", true));

        serverAdapter = new ServerAdapter(getContext(), serverItems, currentSelectedIndex);
        serversListView.setAdapter(serverAdapter);

        serversListView.setSelection(currentSelectedIndex);

        // Set up click listener
        serversListView.setOnItemClickListener((parent, view, position, id) -> {
            ServerItem selectedServer = serverItems.get(position);
            Log.i("PlayerActivity", "ViewClickListener: clicked");

            if (!selectedServer.isAvailable() || selectedServer.getUrl().isEmpty()) {
                return; // Don't process unavailable servers
            }

            currentSelectedIndex = position;
            serverAdapter.setSelectedPosition(position);
            serverAdapter.notifyDataSetChanged();

            if (listener != null) {
                listener.onServerSelected(selectedServer);
            }

            dismiss();
        });

        // Request focus after a short delay to ensure the list is ready
        serversListView.postDelayed(() -> {
            serversListView.requestFocus();
            if (serversListView.getChildCount() > 0) {
                serversListView.getChildAt(0).requestFocus();
            }
        }, 100);
    }

    public static class ServerItem {
        private String name;
        private String url;
        private String language;
        private boolean available;

        public ServerItem(String name, String url, String language, boolean available) {
            this.name = name;
            this.url = url;
            this.language = language;
            this.available = available;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getLanguage() {
            return language;
        }

        public boolean isAvailable() {
            return available;
        }
    }

    private static class ServerAdapter extends ArrayAdapter<ServerItem> {
        private int selectedPosition;

        public ServerAdapter(Context context, List<ServerItem> items, int selectedPosition) {
            super(context, R.layout.item_server, items);
            this.selectedPosition = selectedPosition;
        }

        public void setSelectedPosition(int position) {
            this.selectedPosition = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ServerItem serverItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_server, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            ImageView radioButton = convertView.findViewById(R.id.radioButton);

            if (serverItem != null) {
                titleTextView.setText(serverItem.getName());

                // Update radio button based on selection
                if (position == selectedPosition) {
                    radioButton.setImageResource(R.drawable.radio_button_selected);
                } else {
                    radioButton.setImageResource(R.drawable.radio_button_unselected);
                }

                // Disable if not available
                if (!serverItem.isAvailable() || serverItem.getUrl().isEmpty()) {
                    titleTextView.setAlpha(0.5f);
                    radioButton.setVisibility(View.GONE);
                    convertView.setEnabled(false);
                    convertView.setClickable(false);
                } else {
                    titleTextView.setAlpha(1.0f);
                    radioButton.setVisibility(View.VISIBLE);
                    convertView.setEnabled(true);
                    convertView.setClickable(true);
                }
            }

            return convertView;
        }

        public String getSelectedPosition() {
            return getItem(selectedPosition).getName();
        }
    }
}