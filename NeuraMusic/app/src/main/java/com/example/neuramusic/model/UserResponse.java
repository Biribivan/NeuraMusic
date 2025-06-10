package com.example.neuramusic.model;

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

    public UserResponse() {}

    /** Convenience constructor used when only the user id is known. */
    public UserResponse(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public String getBio() {
        return bio;
    }

    public String getRole() {
        return role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public List<String> getProfessions() {
        return professions;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getYoutube() {
        return youtube;
    }

    public String getSpotify() {
        return spotify;
    }

    public String getSoundcloud() {
        return soundcloud;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isApproved() {
        return isApproved;
    }

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
