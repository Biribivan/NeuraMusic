package com.example.neuramusic.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;


import kotlin.jvm.Transient;

public class Track implements Serializable {

    private String id;

    @SerializedName("playlist_id")
    private String playlistId;

    private String title;

    @SerializedName("local_uri")
    private String localUri;

    @SerializedName("artist_name")
    private String artistName;

    private String genre;

    @SerializedName("created_at")
    private String createdAt;

    //@Transient
    //private boolean isPlaying = false;

    // Getters
    public String getId() { return id; }
    public String getPlaylistId() { return playlistId; }
    public String getTitle() { return title; }
    public String getLocalUri() { return localUri; }
    public String getArtistName() { return artistName; }
    public String getGenre() { return genre; }
    public String getCreatedAt() { return createdAt; }
   // public boolean isPlaying() { return isPlaying; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setPlaylistId(String playlistId) { this.playlistId = playlistId; }
    public void setTitle(String title) { this.title = title; }
    public void setLocalUri(String localUri) { this.localUri = localUri; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    //public void setPlaying(boolean playing) { this.isPlaying = playing; }

    public static List<Track> fromJsonArray(String jsonArray) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Track>>() {}.getType();
        return gson.fromJson(jsonArray, listType);
    }

}
