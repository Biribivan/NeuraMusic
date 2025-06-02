package com.example.neuramusic.model;

import android.net.Uri;

public class MediaItem {
    private Uri uri;
    private boolean isVideo;
    private float aspectRatio;
    private int aspectRatioIndex;
    private int rotation;

    public MediaItem(Uri uri, boolean isVideo) {
        this.uri = uri;
        this.isVideo = isVideo;
        this.aspectRatio = 1.0f; // Por defecto 1:1
        this.aspectRatioIndex = 0; // Por defecto 1:1
        this.rotation = 0;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public int getAspectRatioIndex() {
        return aspectRatioIndex;
    }

    public void setAspectRatioIndex(int aspectRatioIndex) {
        this.aspectRatioIndex = aspectRatioIndex;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
} 