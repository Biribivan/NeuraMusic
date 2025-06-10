package com.example.neuramusic.api;

import com.example.neuramusic.model.*;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseService {

    // --- Autenticación ---
    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> signUp(
            @Body AuthRequest request,
            @Header("apikey") String apiKey
    );

    @POST("auth/v1/token?grant_type=password")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> login(
            @Body AuthRequest request,
            @Header("apikey") String apiKey
    );

    @POST("auth/v1/token?grant_type=refresh_token")
    Call<SupabaseSignupResponse> refreshAccessToken(
            @HeaderMap Map<String, String> headers,
            @Body Map<String, String> refreshToken
    );

    @DELETE("rest/v1/media_posts")
    Call<ResponseBody> deleteMediaPost(
            @Query("id") String postId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @DELETE("rest/v1/text_posts")
    Call<ResponseBody> deleteTextPost(
            @Query("id") String postId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );


    // --- Usuarios ---
    @GET("rest/v1/users")
    Call<List<UserResponse>> getUserById(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/users")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> updateUser(
            @Query("id") String userId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> updates
    );

    @PATCH("rest/v1/{table}")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> updateTable(
            @Path("table") String table,
            @Query("id") String filter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> updates
    );

    @POST("rest/v1/users")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> insertUser(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> data
    );

    // --- Imagen de perfil u otros medios ---
    @Multipart
    @POST("storage/v1/object/user-images/{filename}")
    Call<ResponseBody> uploadImage(
            @Path("filename") String filename,
            @Part MultipartBody.Part file,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- Posts ---
    @POST("rest/v1/text_posts")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> createTextPost(
            @HeaderMap Map<String, String> headers,
            @Body TextPost post
    );

    @GET("rest/v1/text_posts")
    Call<List<TextPost>> getTextPosts(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @POST("rest/v1/media_posts")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> createMediaPost(
            @HeaderMap Map<String, String> headers,
            @Body MediaPost post
    );

    @GET("rest/v1/media_posts")
    Call<List<MediaPost>> getMediaPosts(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @Multipart
    @POST("storage/v1/object/post-media/{filename}")
    Call<ResponseBody> uploadPostMedia(
            @Path("filename") String filename,
            @Part MultipartBody.Part file,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- Playlists y Tracks ---
    @GET("rest/v1/playlist")
    Call<List<Playlist>> getPlaylists(
            @QueryMap Map<String, String> filters,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @POST("rest/v1/playlist")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<List<Playlist>> createPlaylist(
            @Body List<Playlist> playlists,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/playlist")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> updatePlaylist(
            @Query("id") String filter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> updates
    );

    @POST("rest/v1/track")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> createSingleTrack(
            @Body Track track,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @GET("rest/v1/track")
    Call<List<Track>> getTracks(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- Calendario ---
    @GET("rest/v1/calendar_items")
    Call<List<CalendarItem>> getCalendarItems(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/calendar_items")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> updateCalendarItem(
            @Query("id") String itemId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> updates
    );

    @POST("rest/v1/calendar_items")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<List<CalendarItem>> createCalendarItem(
            @Body CalendarItem item,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @DELETE("rest/v1/calendar_items")
    Call<ResponseBody> deleteCalendarItem(
            @Query("id") String itemId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- Notas de calendario ---
    @POST("rest/v1/calendar_notes")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<List<CalendarNote>> createCalendarNote(
            @Body CalendarNote note,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @DELETE("rest/v1/calendar_notes")
    Call<ResponseBody> deleteCalendarNote(
            @Query("id") String noteId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @GET("rest/v1/calendar_notes")
    Call<List<CalendarNote>> getCalendarNotes(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );
}
