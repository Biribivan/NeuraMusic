package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MediaPost {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("caption")
    private String caption;

    @SerializedName("media_urls")
    private List<String> mediaUrls;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public MediaPost(String userId, String caption, List<String> mediaUrls) {
        this.userId = userId;
        this.caption = caption;
        this.mediaUrls = mediaUrls;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
} 