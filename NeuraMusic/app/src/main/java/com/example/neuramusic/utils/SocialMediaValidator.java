package com.example.neuramusic.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class SocialMediaValidator {

    private static final String INSTAGRAM_PATTERN =
            "^https?://(www\\.)?instagram\\.com/([a-zA-Z0-9._]+)/?$";
    private static final String YOUTUBE_PATTERN =
            "^https?://(www\\.)?(youtube\\.com/(c|channel|user)/[a-zA-Z0-9_-]+|youtu\\.be/[a-zA-Z0-9_-]+)/?$";
    private static final String SOUNDCLOUD_PATTERN =
            "^https?://(www\\.)?soundcloud\\.com/[a-zA-Z0-9_-]+/?$";
    private static final String SPOTIFY_PATTERN =
            "^https?://(open\\.)?spotify\\.com/(artist|user|track|album)/[a-zA-Z0-9]+/?$";

    public static boolean isValidInstagramUrl(String url) {
        return url == null || url.isEmpty() || Pattern.matches(INSTAGRAM_PATTERN, url);
    }

    public static boolean isValidYoutubeUrl(String url) {
        return url == null || url.isEmpty() || Pattern.matches(YOUTUBE_PATTERN, url);
    }

    public static boolean isValidSoundcloudUrl(String url) {
        return url == null || url.isEmpty() || Pattern.matches(SOUNDCLOUD_PATTERN, url);
    }

    public static boolean isValidSpotifyUrl(String url) {
        return url == null || url.isEmpty() || Pattern.matches(SPOTIFY_PATTERN, url);
    }

    public static String formatYoutubeUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        if (Patterns.WEB_URL.matcher(url).matches()) return url;
        return "https://youtube.com/" + url;
    }

    public static String formatSoundcloudUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        if (Patterns.WEB_URL.matcher(url).matches()) return url;
        return "https://soundcloud.com/" + url;
    }

    public static String formatSpotifyUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        if (Patterns.WEB_URL.matcher(url).matches()) return url;
        return "https://open.spotify.com/artist/" + url;
    }
} 