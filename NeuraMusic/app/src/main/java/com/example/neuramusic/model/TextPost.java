package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class TextPost {
    public TextPost(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("content")
    private String content;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
