package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class ProfileBlock {
    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("type")
    public String type; // "image", "video", "reel", "text", "tweet"

    @SerializedName("content")
    public String content;

    @SerializedName("media_url")
    public String mediaUrl;

    @SerializedName("height")
    public int height;

    @SerializedName("position")
    public int position;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;
}
