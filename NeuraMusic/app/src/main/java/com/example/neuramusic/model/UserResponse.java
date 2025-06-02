package com.example.neuramusic.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {

    @SerializedName("id")
    public String uid;

    @SerializedName("username")
    public String username;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("art_title")
    public String artTitle;

    @SerializedName("bio")
    public String bio;

    @SerializedName("role")
    public String role;

    @SerializedName("profile_image_url")
    public String profileImageUrl;

    @SerializedName("professions")
    public List<String> professions;

    @SerializedName("instagram_url")
    public String instagram;

    @SerializedName("youtube_url")
    public String youtube;

    @SerializedName("spotify_url")
    public String spotify;

    @SerializedName("soundcloud_url")
    public String soundcloud;

    @SerializedName("gender")
    public String gender;

    @SerializedName("birth_date")
    public String birthDate;

    @SerializedName("is_blocked")
    public boolean isBlocked;

    @SerializedName("is_approved")
    public boolean isApproved;

    // Constructor vacío requerido por Gson
    public UserResponse() {}

    // Método utilitario para parsear JSON
    public static UserResponse fromJson(String json) {
        try {
            // Supabase devuelve un array de usuarios
            UserResponse[] arr = new Gson().fromJson(json, UserResponse[].class);
            return arr.length > 0 ? arr[0] : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Opcional: para logs/debug
    @Override
    public String toString() {
        return "UserResponse{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", artTitle='" + artTitle + '\'' +
                ", bio='" + bio + '\'' +
                ", role='" + role + '\'' +
                ", isBlocked=" + isBlocked +
                ", isApproved=" + isApproved +
                '}';
    }
}
