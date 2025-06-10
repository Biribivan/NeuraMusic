package com.example.neuramusic.model;

import java.util.List;

public class FeedPost {
    public String id; // ← Añadir esto
    public UserResponse user;
    public String content;
    public List<String> mediaUrls;
    public String createdAt;
    public boolean isMedia;
    public String caption;

    public FeedPost(UserResponse user, String caption, List<String> mediaUrls, String createdAt, boolean isMedia, String content) {
        this.user = user;
        this.caption = caption;
        this.mediaUrls = mediaUrls;
        this.createdAt = createdAt;
        this.isMedia = isMedia;
        this.content = content;
    }

    public String getId() {
        return id;
    }
}
