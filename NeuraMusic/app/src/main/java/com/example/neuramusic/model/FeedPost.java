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

    /**
     * Create a feed post using the order expected across the app.
     * The {@code text} parameter represents either the caption for a media post
     * or the content of a text post depending on {@code isMedia}.
     */
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
