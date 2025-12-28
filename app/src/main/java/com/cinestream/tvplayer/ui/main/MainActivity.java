package com.cinestream.tvplayer.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.data.repository.MediaRepository;
import com.cinestream.tvplayer.data.repository.MediaRepositoryVideasy;
import com.cinestream.tvplayer.ui.details.DetailsActivity;
import com.cinestream.tvplayer.ui.presenter.CardPresenter;

import java.util.List;

public class MainActivity extends BrowseSupportFragment {

    private ArrayObjectAdapter rowsAdapter;
    private MediaRepository mediaRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mediaRepository = new MediaRepository();
        
        // Set up the browse fragment
        setupUIElements();
        setupEventListeners();
        
        // Load data
        loadRows();
    }

    private void setupUIElements() {
        // Set title
        setTitle("CineStream TV");
        
        // Set headers
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        
        // Set brand color
        setBrandColor(getResources().getColor(R.color.cinema_red));
        
        // Create adapter
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter(FocusHighlight.ZOOM_FACTOR_SMALL));
        
        // Set adapter
        setAdapter(rowsAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, 
                                     RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof MediaItems) {
                    MediaItems mediaItems = (MediaItems) item;
                    
                    // Navigate to details activity
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("media_item", mediaItems);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadRows() {
        rowsAdapter.clear();
        
        // Get static media data
        List<MediaItems> featuredMovies = mediaRepository.getFeaturedMovies();
        List<MediaItems> actionMovies = mediaRepository.getActionMovies();
        List<MediaItems> comedyMovies = mediaRepository.getComedyMovies();
        List<MediaItems> dramaMovies = mediaRepository.getDramaMovies();
        List<MediaItems> documentaries = mediaRepository.getDocumentaries();
        List<MediaItems> formatDemos = mediaRepository.getFormatDemos();
        
        // Get API sample content
        List<MediaItems> apiSamples = MediaRepositoryVideasy.getInstance().getAPISampleContent();
        
        // Featured movies row
        addRow("Featured Movies", featuredMovies);
        
        // Action movies row
        addRow("Action & Adventure", actionMovies);
        
        // Comedy movies row
        addRow("Comedy", comedyMovies);
        
        // Drama movies row
        addRow("Drama", dramaMovies);
        
        // Documentaries row
        addRow("Documentaries", documentaries);
        
        // Format demonstrations row
        addRow("Format Demos (MP4, MKV, WebM)", formatDemos);
        
        // NEW: API Sample Content row
        addRow("🎆 Live API Content (Videasy)", apiSamples);
    }

    private void addRow(String title, List<MediaItems> items) {
        if (items != null && !items.isEmpty()) {
            CardPresenter cardPresenter = new CardPresenter();
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            
            for (MediaItems item : items) {
                listRowAdapter.add(item);
            }
            
            ListRow listRow = new ListRow(listRowAdapter);
            //listRow.setHeaderItem(new ListRow.HeaderItem(title));
            rowsAdapter.add(listRow);
        }
    }
}