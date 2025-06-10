package com.example.neuramusic.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaPost {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("media_urls")
    @Expose
    private List<String> mediaUrls;

    @SerializedName("caption")
    @Expose
    private String caption;

    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    // 🔧 Constructor requerido por Retrofit/Gson
    public MediaPost() {}

    // 🔨 Constructor útil para crear nuevos posts
    public MediaPost(String userId, String caption, List<String> mediaUrls) {
        this.userId = userId;
        this.caption = caption;
        this.mediaUrls = mediaUrls;
    }

    // ✅ Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getCaption() {
        return caption;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // ✅ Setters (opcional)
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
