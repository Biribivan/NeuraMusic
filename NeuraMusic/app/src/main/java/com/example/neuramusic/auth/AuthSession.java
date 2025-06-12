package com.example.neuramusic.auth;

import android.util.Log;

public class AuthSession {

    private static final String TAG = "AuthSession";

    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4b3hoZG1paHlkam90c2dncGNvIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODM3OTcyNiwiZXhwIjoyMDYzOTU1NzI2fQ.eWRZRHeiIuAA7p6wC6kYULBSXj4y-M9KMaPsOPiRJt8";

    private static String accessToken = null;
    private static String refreshToken = null;
    public static String uid = null;

    public static void setSession(String newAccessToken, String newRefreshToken, String newUid) {
        accessToken = newAccessToken;
        refreshToken = newRefreshToken;
        uid = newUid;
        Log.d(TAG, "✅ Session updated");
    }

    public static void clear() {
        accessToken = null;
        refreshToken = null;
        uid = null;
        Log.d(TAG, "🧹 Session cleared");
    }

    public static boolean isAuthenticated() {
        return accessToken != null && uid != null;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    public static String getUid() {
        return uid;
    }

    public static void setUid(String newUid) {
        uid = newUid;
    }

    public static String getBearerToken() {
        return accessToken != null ? "Bearer " + accessToken : null;
    }

    // Añadidos para compatibilidad
    public static boolean isTokenExpired() {
        return false; // Token permanente o no caduca en esta lógica simplificada
    }

    public static boolean refreshSessionIfNeeded() {
        if (isTokenExpired()) {
            return refreshTokenSync();
        }
        return true;
    }

    public static boolean refreshTokenSync() {
        Log.d(TAG, "🔄 Token no expirado. No se necesita refresh.");
        return true;
    }
}
