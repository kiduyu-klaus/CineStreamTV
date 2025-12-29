package com.cinestream.tvplayer.ui.player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;

import com.cinestream.tvplayer.R;
import com.cinestream.tvplayer.data.model.MediaItems;
import com.cinestream.tvplayer.ui.player.dialog.QualitySelectionDialog;
import com.cinestream.tvplayer.ui.player.dialog.ServerSelectionDialog;
import com.cinestream.tvplayer.ui.player.dialog.SubtitleSelectionDialog;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private MediaItem currentMediaItem;
    private MediaItems sourceMediaItem;

    // Top Bar
    private LinearLayout topBar;
    private ImageView backButton;
    private TextView titleTopTextView;

    // UI Controls
    private LinearLayout controlsLayout;
    private ImageView playPauseButton;
    private TextView currentTimeTextView;
    private SeekBar seekBar;
    private ProgressBar loadingProgressBar;

    // Bottom Buttons
    private AppCompatButton serversButton;
    private AppCompatButton qualityButton;
    private AppCompatButton subtitleButton;
    private AppCompatButton speedButton;

    // Dialogs
    private QualitySelectionDialog qualityDialog;
    private SubtitleSelectionDialog subtitleDialog;
    private ServerSelectionDialog serverDialog;

    // Control visibility
    private Handler controlVisibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable;
    private static final long CONTROLS_VISIBLE_DURATION = 5000; // 5 seconds

    // Progress update
    private Handler progressUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable progressUpdateRunnable;

    // Playback state
    private boolean isControlsVisible = true;
    private boolean isPrepared = false;
    private long savedPlaybackPosition = 0;
    private boolean playWhenReady = true;
    private String currentQuality = "Auto";
    private float currentSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Keep screen on during playback
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hide system UI for fullscreen
        hideSystemUI();

        // Get media item from intent
        sourceMediaItem = getIntent().getParcelableExtra("media_item");
        if (sourceMediaItem == null) {
            finish();
            return;
        }

        // Restore saved state if available
        if (savedInstanceState != null) {
            savedPlaybackPosition = savedInstanceState.getLong("playback_position", 0);
            playWhenReady = savedInstanceState.getBoolean("play_when_ready", true);
            currentQuality = savedInstanceState.getString("current_quality", "Auto");
            currentSpeed = savedInstanceState.getFloat("current_speed", 1.0f);
        }

        initializeViews();
        setupPlayer();
        setupUI();
        setupClickListeners();
        prepareAndPlay();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Top Bar
        topBar = findViewById(R.id.topBar);
        backButton = findViewById(R.id.backButton);
        titleTopTextView = findViewById(R.id.titleTopTextView);

        // Controls
        controlsLayout = findViewById(R.id.controlsLayout);
        playPauseButton = findViewById(R.id.playPauseButton);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        seekBar = findViewById(R.id.progressBar);

        // Bottom Buttons
        serversButton = findViewById(R.id.serversButton);
        qualityButton = findViewById(R.id.qualityButton);
        subtitleButton = findViewById(R.id.subtitleButton);
        speedButton = findViewById(R.id.speedButton);

        // Initialize dialogs
        qualityDialog = new QualitySelectionDialog();
        subtitleDialog = new SubtitleSelectionDialog();
        serverDialog = new ServerSelectionDialog();
    }

    private void setupPlayer() {
        // Create ExoPlayer instance
        trackSelector = new DefaultTrackSelector(this);
        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();

        // Set player to PlayerView
        playerView.setPlayer(player);

        // Set up player event listeners
        setupPlayerEventListeners();

        // Hide default control view since we're using custom controls
        playerView.setUseController(false);

        // Add touch listener to show/hide controls
        playerView.setOnClickListener(v -> toggleControlsVisibility());
    }

    private void setupUI() {
        // Set up initial state
        updatePlayPauseButton(false);
        showControls();

        // Set movie title
        if (sourceMediaItem != null) {
            titleTopTextView.setText(sourceMediaItem.getTitle());
        }

        // Update quality button with current quality
        updateQualityButton();

        // Request initial focus on play/pause button
        playPauseButton.post(() -> playPauseButton.requestFocus());
    }

    private void setupClickListeners() {
        // Top bar
        backButton.setOnClickListener(v -> finish());

        // Play controls
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Bottom buttons
        serversButton.setOnClickListener(v -> showServersDialog());
        qualityButton.setOnClickListener(v -> showQualityDialog());
        subtitleButton.setOnClickListener(v -> showSubtitleDialog());
        speedButton.setOnClickListener(v -> showSpeedDialog());

        // Add focus change listeners for animations
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                showControls();
                v.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(150)
                        .start();
            } else {
                v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
            }
        };

        playPauseButton.setOnFocusChangeListener(focusListener);
        serversButton.setOnFocusChangeListener(focusListener);
        qualityButton.setOnFocusChangeListener(focusListener);
        subtitleButton.setOnFocusChangeListener(focusListener);
        speedButton.setOnFocusChangeListener(focusListener);
        backButton.setOnFocusChangeListener(focusListener);
        seekBar.setOnFocusChangeListener(focusListener);

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    if (duration != C.TIME_UNSET) {
                        long seekTime = (duration * progress) / 100;
                        currentTimeTextView.setText(formatTime(seekTime));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopProgressUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    long duration = player.getDuration();
                    if (duration != C.TIME_UNSET) {
                        long seekTime = (duration * seekBar.getProgress()) / 100;
                        player.seekTo(seekTime);
                    }
                }
                startProgressUpdate();
                showControls();
            }
        });
    }

    private void setupPlayerEventListeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        loadingProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        loadingProgressBar.setVisibility(View.GONE);
                        if (!isPrepared) {
                            isPrepared = true;
                            if (savedPlaybackPosition > 0) {
                                player.seekTo(savedPlaybackPosition);
                            }
                        }
                        break;
                    case Player.STATE_ENDED:
                        updatePlayPauseButton(false);
                        stopProgressUpdate();
                        showControls();
                        break;
                    case Player.STATE_IDLE:
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseButton(isPlaying);
                if (isPlaying) {
                    startProgressUpdate();
                } else {
                    stopProgressUpdate();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                loadingProgressBar.setVisibility(View.GONE);
                String errorMessage = "Playback error: " + error.getMessage();
                Toast.makeText(PlayerActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("PlayerActivity", "Playback error", error);
            }
        });
    }

    private void prepareAndPlay() {
        if (sourceMediaItem == null) {
            Toast.makeText(this, "Invalid media item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Determine media source based on URL type
        MediaSource mediaSource = createMediaSource(sourceMediaItem);
        if (mediaSource == null) {
            Toast.makeText(this, "Unsupported media format", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder();

        if (!sourceMediaItem.getId().isEmpty()) {
            // Create MediaItem with subtitle configuration
            mediaItemBuilder.setUri(getMediaUri(sourceMediaItem))
                    .setMediaId(sourceMediaItem.getId());
            // }
        }
        else{
            mediaItemBuilder
                    .setUri(getMediaUri(sourceMediaItem));
        }
        // Add subtitles if available
        List<MediaItem.SubtitleConfiguration> subtitleList = new ArrayList<>();

        if (sourceMediaItem.getSubtitles() != null && !sourceMediaItem.getSubtitles().isEmpty()) {
            for (MediaItems.SubtitleItem subtitleItem : sourceMediaItem.getSubtitles()) {
                MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(
                        Uri.parse(subtitleItem.getUrl()))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage(subtitleItem.getLang())
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build();
                subtitleList.add(subtitle);
            }
        } else if (sourceMediaItem.getSubtitleUrl() != null && !sourceMediaItem.getSubtitleUrl().isEmpty()) {
            MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(
                    Uri.parse(sourceMediaItem.getSubtitleUrl()))
                    .setMimeType(MimeTypes.TEXT_VTT)
                    .setLanguage("en")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build();
            subtitleList.add(subtitle);
        }

        if (!subtitleList.isEmpty()) {
            mediaItemBuilder.setSubtitleConfigurations(subtitleList);
        }

        currentMediaItem = mediaItemBuilder.build();

        // Set media source and prepare player
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(playWhenReady);

        // Apply saved speed
        player.setPlaybackSpeed(currentSpeed);
    }

    private MediaSource createMediaSource(MediaItems mediaItem) {
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);

        Uri uri = getMediaUri(mediaItem);
        Log.i("PlayerActivity", "createMediaSource: " + uri);
        String uriString = uri.toString().toLowerCase();

        if (uriString.contains(".m3u8") || uriString.contains("m3u8")) {
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        } else if (uriString.contains(".mpd") || uriString.contains("mpd")) {
            return new DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        } else {
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        }
    }

    private MediaSource createMediaSourceFromUrl(String url) {
        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        Uri uri = Uri.parse(url);
        String uriString = url.toLowerCase();

        if (uriString.contains(".m3u8") || uriString.contains("m3u8")) {
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        } else if (uriString.contains(".mpd") || uriString.contains("mpd")) {
            return new DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        } else {
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        }
    }

    private Uri getMediaUri(MediaItems mediaItem) {
        String bestUrl = mediaItem.getBestVideoUrl();
        return Uri.parse(bestUrl);
    }

    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
            showControls();
        }
    }

    private void showServersDialog() {
        if (sourceMediaItem != null) {
            serverDialog.setVideoSources(sourceMediaItem.getVideoSources());
            serverDialog.setOnServerSelectedListener(serverItem -> {
                switchToServer(serverItem.getUrl(), serverItem.getQuality());
            });
            serverDialog.show(getSupportFragmentManager(), "server_dialog");
        }
    }

    private void showQualityDialog() {
        if (player != null && trackSelector != null) {
            qualityDialog.setTrackSelector(trackSelector, player);

            if (sourceMediaItem != null && sourceMediaItem.getVideoSources() != null) {
                qualityDialog.setVideoSources(sourceMediaItem.getVideoSources());
            }

            qualityDialog.setOnQualitySelectedListener(qualityItem -> {
                currentQuality = qualityItem.getTitle();
                updateQualityButton();
            });

            qualityDialog.show(getSupportFragmentManager(), "quality_dialog");
        }
    }

    private void showSubtitleDialog() {
        if (player != null && trackSelector != null) {
            subtitleDialog.setTrackSelector(trackSelector, player);

            if (sourceMediaItem != null && sourceMediaItem.getSubtitles() != null) {
                subtitleDialog.setMediaSubtitles(sourceMediaItem.getSubtitles());
            }

            subtitleDialog.show(getSupportFragmentManager(), "subtitle_dialog");
        }
    }

    private void showSpeedDialog() {
        // Create speed selection dialog
        String[] speeds = {"0.5x", "0.75x", "1x", "1.25x", "1.5x", "2x"};
        float[] speedValues = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Playback Speed")
                .setItems(speeds, (dialog, which) -> {
                    currentSpeed = speedValues[which];
                    if (player != null) {
                        player.setPlaybackSpeed(currentSpeed);
                    }
                    Toast.makeText(this, "Speed: " + speeds[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void switchToServer(String newUrl, String quality) {
        if (player == null || newUrl == null || newUrl.isEmpty()) return;

        // Save current position
        long currentPosition = player.getCurrentPosition();
        boolean wasPlaying = player.isPlaying();

        try {
            // Create new media source
            MediaSource newMediaSource = createMediaSourceFromUrl(newUrl);

            // Switch source
            player.setMediaSource(newMediaSource);
            player.prepare();
            player.seekTo(currentPosition);
            player.setPlayWhenReady(wasPlaying);

            // Update quality button
            currentQuality = quality;
            updateQualityButton();

            Toast.makeText(this, "Switched to " + quality, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("PlayerActivity", "Error switching server", e);
            Toast.makeText(this, "Failed to switch server", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQualityButton() {
        qualityButton.setText(currentQuality);
    }

    private void toggleControlsVisibility() {
        if (isControlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        int drawableRes = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_circle;
        playPauseButton.setImageResource(drawableRes);
    }

    private void startProgressUpdate() {
        stopProgressUpdate();
        progressUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && isPrepared && player.isPlaying()) {
                    long currentPosition = player.getCurrentPosition();
                    long duration = player.getDuration();

                    if (duration != C.TIME_UNSET && duration > 0) {
                        currentTimeTextView.setText(formatTime(currentPosition));

                        int progress = (int) ((currentPosition * 100) / duration);
                        seekBar.setProgress(progress);
                    }

                    progressUpdateHandler.postDelayed(this, 1000);
                }
            }
        };
        progressUpdateHandler.post(progressUpdateRunnable);
    }

    private void stopProgressUpdate() {
        if (progressUpdateRunnable != null) {
            progressUpdateHandler.removeCallbacks(progressUpdateRunnable);
        }
    }

    private String formatTime(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }

    private void showControls() {
        if (!isControlsVisible) {
            controlsLayout.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.VISIBLE);
            isControlsVisible = true;
        }

        if (hideControlsRunnable != null) {
            controlVisibilityHandler.removeCallbacks(hideControlsRunnable);
        }

        hideControlsRunnable = this::hideControls;
        controlVisibilityHandler.postDelayed(hideControlsRunnable, CONTROLS_VISIBLE_DURATION);
    }

    private void hideControls() {
        if (player != null && player.isPlaying()) {
            controlsLayout.setVisibility(View.GONE);
            topBar.setVisibility(View.GONE);
            isControlsVisible = false;
        }
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (!isControlsVisible) {
                showControls();
                return true;
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                togglePlayPause();
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!isControlsVisible && player != null) {
                    long duration = player.getDuration();
                    if (duration != C.TIME_UNSET) {
                        long newPosition = Math.min(duration, player.getCurrentPosition() + 10000);
                        player.seekTo(newPosition);
                        showControls();
                    }
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!isControlsVisible && player != null) {
                    player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
                    showControls();
                    return true;
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null) {
            outState.putLong("playback_position", player.getCurrentPosition());
            outState.putBoolean("play_when_ready", player.getPlayWhenReady());
            outState.putString("current_quality", currentQuality);
            outState.putFloat("current_speed", currentSpeed);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && playWhenReady) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            savedPlaybackPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgressUpdate();
        if (hideControlsRunnable != null) {
            controlVisibilityHandler.removeCallbacks(hideControlsRunnable);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isControlsVisible) {
            hideControls();
        } else {
            super.onBackPressed();
        }
    }
}