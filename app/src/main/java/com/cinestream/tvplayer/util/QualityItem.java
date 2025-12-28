package com.cinestream.tvplayer.util;

public class QualityItem {
    private String title;
    private String value;
    private String description;

    public QualityItem(String title, String value, String description) {
        this.title = title;
        this.value = value;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}