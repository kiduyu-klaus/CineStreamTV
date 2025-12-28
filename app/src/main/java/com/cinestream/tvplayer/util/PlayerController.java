package com.cinestream.tvplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.media3.common.Player;

public class PlayerController {
    private static final String TAG = "PlayerController";
    private static final String PREFS_NAME = "player_preferences";
    private static final String KEY_PLAYBACK_POSITION = "playback_position";
    private static final String KEY_PLAYBACK_SPEED = "playback_speed";
    
    private Context context;
    private Player player;
    private SharedPreferences sharedPreferences;
    private Handler mainHandler;

    public PlayerController(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Save current playback position
     */
    public void savePlaybackPosition(String mediaId, long positionMs) {
        sharedPreferences.edit()
                .putLong(KEY_PLAYBACK_POSITION + "_" + mediaId, positionMs)
                .apply();
    }

    /**
     * Get saved playback position
     */
    public long getSavedPlaybackPosition(String mediaId) {
        return sharedPreferences.getLong(KEY_PLAYBACK_POSITION + "_" + mediaId, 0);
    }

    /**
     * Clear saved playback position
     */
    public void clearPlaybackPosition(String mediaId) {
        sharedPreferences.edit()
                .remove(KEY_PLAYBACK_POSITION + "_" + mediaId)
                .apply();
    }

    /**
     * Resume from saved position if available
     */
    public void resumeFromSavedPosition(String mediaId) {
        if (player != null) {
            long savedPosition = getSavedPlaybackPosition(mediaId);
            if (savedPosition > 0) {
                player.seekTo(savedPosition);
            }
        }
    }

    /**
     * Set playback speed
     */
    public void setPlaybackSpeed(float speed) {
        if (player != null) {
            player.setPlaybackSpeed(speed);
            sharedPreferences.edit()
                    .putFloat(KEY_PLAYBACK_SPEED, speed)
                    .apply();
        }
    }

    /**
     * Get playback speed
     */
    public float getPlaybackSpeed() {
        return sharedPreferences.getFloat(KEY_PLAYBACK_SPEED, 1.0f);
    }

    /**
     * Handle player state changes
     */
    public void onPlayerStateChanged(int playbackState, String mediaId) {
        switch (playbackState) {
            case Player.STATE_READY:
                // Player is ready, can save/resume position
                if (player != null && mediaId != null) {
                    resumeFromSavedPosition(mediaId);
                }
                break;
                
            case Player.STATE_BUFFERING:
                // Player is buffering, no action needed
                break;
                
            case Player.STATE_ENDED:
                // Playback ended, clear saved position
                if (mediaId != null) {
                    clearPlaybackPosition(mediaId);
                }
                break;
                
            case Player.STATE_IDLE:
                // Player is idle, no action needed
                break;
        }
    }

    /**
     * Auto-save playback position periodically
     */
    public void startAutoSave(String mediaId) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && mediaId != null) {
                    long currentPosition = player.getCurrentPosition();
                    savePlaybackPosition(mediaId, currentPosition);
                }
                
                // Schedule next save in 5 seconds
                mainHandler.postDelayed(this, 5000);
            }
        });
    }

    /**
     * Stop auto-save
     */
    public void stopAutoSave() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Get current playback state info
     */
    public PlaybackStateInfo getPlaybackStateInfo() {
        if (player == null) {
            return new PlaybackStateInfo();
        }

        return new PlaybackStateInfo(
                player.getCurrentPosition(),
                player.getDuration(),
                player.getBufferedPercentage(),
                player.isPlaying(),
                player.getPlaybackState()
        );
    }

    /**
     * Check if player can seek to specific position
     */
    public boolean canSeekTo(long positionMs) {
        if (player == null) return false;
        
        long duration = player.getDuration();
        return duration != androidx.media3.common.C.TIME_UNSET && 
               positionMs >= 0 && 
               positionMs <= duration;
    }

    /**
     * Seek to position with bounds checking
     */
    public void seekTo(long positionMs) {
        if (player != null && canSeekTo(positionMs)) {
            player.seekTo(positionMs);
        }
    }

    /**
     * Class to hold playback state information
     */
    public static class PlaybackStateInfo {
        public final long currentPosition;
        public final long duration;
        public final int bufferedPercentage;
        public final boolean isPlaying;
        public final int playbackState;

        public PlaybackStateInfo() {
            this(0, 0, 0, false, Player.STATE_IDLE);
        }

        public PlaybackStateInfo(long currentPosition, long duration, 
                                int bufferedPercentage, boolean isPlaying, int playbackState) {
            this.currentPosition = currentPosition;
            this.duration = duration;
            this.bufferedPercentage = bufferedPercentage;
            this.isPlaying = isPlaying;
            this.playbackState = playbackState;
        }

        public boolean isReady() {
            return playbackState == Player.STATE_READY;
        }

        public boolean isBuffering() {
            return playbackState == Player.STATE_BUFFERING;
        }

        public boolean isEnded() {
            return playbackState == Player.STATE_ENDED;
        }
    }
}