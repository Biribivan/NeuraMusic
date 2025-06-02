package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class TextPost {
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("created_at")
    private String createdAt;

    public TextPost(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
} 