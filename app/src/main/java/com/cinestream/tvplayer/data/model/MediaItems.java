package com.cinestream.tvplayer.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class MediaItems implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String posterUrl;
    private String videoUrl;
    private String subtitleUrl;
    private String duration;
    private int year;
    private String genre;
    private float rating;
    private boolean isLive;
    private String hlsUrl;
    private String dashUrl;
    
    // TMDB-specific fields
    private String tmdbId;
    private String mediaType; // "movie" or "tv"
    private String season;
    private String episode;
    private boolean isFromTMDB;
    private String status;
    private String tagline;
    private int voteCount;
    private List<String> genres;
    
    // Image URLs
    private String backgroundImageUrl;
    private String heroImageUrl;
    private String cardImageUrl;
    
    // API sources
    private List<VideoSource> videoSources;
    private List<SubtitleItem> subtitles;

    public MediaItems() {
        // Default constructor
        this.videoSources = new ArrayList<>();
        this.subtitles = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.isFromTMDB = false;
    }

    protected MediaItems(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        posterUrl = in.readString();
        videoUrl = in.readString();
        subtitleUrl = in.readString();
        duration = in.readString();
        year = in.readInt();
        genre = in.readString();
        rating = in.readFloat();
        isLive = in.readByte() != 0;
        hlsUrl = in.readString();
        dashUrl = in.readString();
        tmdbId = in.readString();
        mediaType = in.readString();
        season = in.readString();
        episode = in.readString();
        isFromTMDB = in.readByte() != 0;
        status = in.readString();
        tagline = in.readString();
        voteCount = in.readInt();
        
        // Read lists
        genres = new ArrayList<>();
        in.readList(genres, String.class.getClassLoader());
        
        backgroundImageUrl = in.readString();
        heroImageUrl = in.readString();
        cardImageUrl = in.readString();
        
        videoSources = new ArrayList<>();
        in.readList(videoSources, VideoSource.class.getClassLoader());
        
        subtitles = new ArrayList<>();
        in.readList(subtitles, SubtitleItem.class.getClassLoader());
    }

    public static final Creator<MediaItems> CREATOR = new Creator<MediaItems>() {
        @Override
        public MediaItems createFromParcel(Parcel in) {
            return new MediaItems(in);
        }

        @Override
        public MediaItems[] newArray(int size) {
            return new MediaItems[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getSubtitleUrl() {
        return subtitleUrl;
    }

    public void setSubtitleUrl(String subtitleUrl) {
        this.subtitleUrl = subtitleUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public String getDashUrl() {
        return dashUrl;
    }

    public void setDashUrl(String dashUrl) {
        this.dashUrl = dashUrl;
    }

    // TMDB-specific getters and setters
    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }
    
    public boolean isFromTMDB() {
        return isFromTMDB;
    }
    
    public void setFromTMDB(boolean fromTMDB) {
        isFromTMDB = fromTMDB;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTagline() {
        return tagline;
    }
    
    public void setTagline(String tagline) {
        this.tagline = tagline;
    }
    
    public int getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
    
    public List<String> getGenres() {
        return genres;
    }
    
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
    
    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return String.join(", ", genres);
    }
    
    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }
    
    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }
    
    public String getHeroImageUrl() {
        return heroImageUrl;
    }
    
    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }
    
    public String getCardImageUrl() {
        return cardImageUrl;
    }
    
    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public List<VideoSource> getVideoSources() {
        return videoSources;
    }

    public void setVideoSources(List<VideoSource> videoSources) {
        this.videoSources = videoSources;
    }

    public List<SubtitleItem> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<SubtitleItem> subtitles) {
        this.subtitles = subtitles;
    }

    // Helper methods
    public boolean isFromAPI() {
        return tmdbId != null && !tmdbId.isEmpty();
    }
    
    public boolean hasValidVideoSources() {
        return (videoSources != null && !videoSources.isEmpty()) ||
               (hlsUrl != null && !hlsUrl.isEmpty()) ||
               (dashUrl != null && !dashUrl.isEmpty()) ||
               (videoUrl != null && !videoUrl.isEmpty());
    }
    
    public String getPrimaryImageUrl() {
        // Priority order: hero, background, poster
        if (heroImageUrl != null && !heroImageUrl.isEmpty()) {
            return heroImageUrl;
        }
        if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
            return backgroundImageUrl;
        }
        if (cardImageUrl != null && !cardImageUrl.isEmpty()) {
            return cardImageUrl;
        }
        return posterUrl;
    }

    public String getBestVideoUrl() {
        // Return the best quality URL available
        if (videoSources != null && !videoSources.isEmpty()) {
            // Try to find the highest quality
            for (VideoSource source : videoSources) {
                if ("1080p".equals(source.getQuality()) || "720p".equals(source.getQuality())) {
                    return source.getUrl();
                }
            }
            // Return first available source
            return videoSources.get(0).getUrl();
        }
        
        // Fallback to regular video URL
        if (hlsUrl != null && !hlsUrl.isEmpty()) {
            return hlsUrl;
        }
        if (dashUrl != null && !dashUrl.isEmpty()) {
            return dashUrl;
        }
        return videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(posterUrl);
        dest.writeString(videoUrl);
        dest.writeString(subtitleUrl);
        dest.writeString(duration);
        dest.writeInt(year);
        dest.writeString(genre);
        dest.writeFloat(rating);
        dest.writeByte((byte) (isLive ? 1 : 0));
        dest.writeString(hlsUrl);
        dest.writeString(dashUrl);
        dest.writeString(tmdbId);
        dest.writeString(mediaType);
        dest.writeString(season);
        dest.writeString(episode);
        dest.writeByte((byte) (isFromTMDB ? 1 : 0));
        dest.writeString(status);
        dest.writeString(tagline);
        dest.writeInt(voteCount);
        dest.writeList(genres);
        dest.writeString(backgroundImageUrl);
        dest.writeString(heroImageUrl);
        dest.writeString(cardImageUrl);
        dest.writeList(videoSources);
        dest.writeList(subtitles);
    }
    
    // Static classes to match VideasyAPI structure
    public static class VideoSource {
        private String quality;
        private String url;

        public VideoSource() {}

        public VideoSource(String quality, String url) {
            this.quality = quality;
            this.url = url;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    public static class SubtitleItem {
        private String url;
        private String lang;
        private String language;

        public SubtitleItem() {}

        public SubtitleItem(String url, String lang, String language) {
            this.url = url;
            this.lang = lang;
            this.language = language;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }
}