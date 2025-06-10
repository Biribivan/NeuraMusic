package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MediaPost {

    public MediaPost(String userId, String caption, List<String> mediaUrls) {
        this.userId = userId;
        this.caption = caption;
        this.mediaUrls = mediaUrls;
    }


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



    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCaption() {
        return caption;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
