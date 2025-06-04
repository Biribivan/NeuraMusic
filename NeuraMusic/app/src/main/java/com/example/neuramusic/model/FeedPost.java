package com.example.neuramusic.model;

import java.util.List;

public class FeedPost {
    public UserResponse user;
    public String content;
    public List<String> mediaUrls;
    public String createdAt;
    public boolean isMedia;

    public FeedPost(UserResponse user, String content, List<String> mediaUrls, String createdAt, boolean isMedia) {
        this.user = user;
        this.content = content;
        this.mediaUrls = mediaUrls;
        this.createdAt = createdAt;
        this.isMedia = isMedia;
    }
}
