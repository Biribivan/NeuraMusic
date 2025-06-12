package com.example.neuramusic.model;

import java.util.List;

public class FeedPost {
    public String id;
    public UserResponse user;
    public String content;
    public List<String> mediaUrls;
    public String caption;
    public boolean isMedia;
    public String createdAt;

    public FeedPost(UserResponse user, String id, List<String> mediaUrls,
                    String text, boolean isMedia, String createdAt) {
        this.user = user;
        this.id = id;
        this.mediaUrls = mediaUrls;
        this.isMedia = isMedia;
        this.createdAt = createdAt;

        if (isMedia) {
            this.caption = text;
            this.content = "";
        } else {
            this.content = text;
            this.caption = "";
        }
    }

    public String getId() {
        return id;
    }
}
