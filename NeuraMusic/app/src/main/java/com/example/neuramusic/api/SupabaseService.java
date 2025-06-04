package com.example.neuramusic.api;

import com.example.neuramusic.model.AuthRequest;
import com.example.neuramusic.model.ProfileBlock;
import com.example.neuramusic.model.UserRequest;
import com.example.neuramusic.model.UserResponse;
import com.example.neuramusic.model.TextPost;
import com.example.neuramusic.model.MediaPost;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseService {

    // Autenticación
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
    Call<ResponseBody> refreshToken(
        @HeaderMap Map<String, String> headers,
        @Body Map<String, String> refreshToken
    );

    // Operaciones de usuario
    @GET("rest/v1/users")
    Call<ResponseBody> getUserById(
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

    // Subida de imágenes
    @Multipart
    @POST("storage/v1/object/user-images/{filename}")
    Call<ResponseBody> uploadImage(
            @Path("filename") String filename,
            @Part MultipartBody.Part file,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // Bloques de perfil
    @GET("rest/v1/profile_blocks")
    Call<List<ProfileBlock>> getProfileBlocksByUserId(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @POST("rest/v1/profile_blocks")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> createProfileBlock(
            @Body ProfileBlock block,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/profile_blocks")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=minimal"
    })
    Call<ResponseBody> updateProfileBlock(
            @Query("id") String blockId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> updates
    );

    @DELETE("rest/v1/profile_blocks")
    Call<ResponseBody> deleteProfileBlock(
            @Query("id") String blockId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @GET("rest/v1/users")
    Call<ResponseBody> checkUsername(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey
    );

    @Multipart
    @POST("storage/v1/object/block-media")
    Call<ResponseBody> uploadBlockMedia(
            @Part MultipartBody.Part file,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @POST("rest/v1/text_posts")
    Call<ResponseBody> createTextPost(
        @HeaderMap Map<String, String> headers,
        @Body TextPost post
    );

    @Multipart
    @POST("storage/v1/object/post-media/{filename}")
    Call<ResponseBody> uploadPostMedia(
        @Path("filename") String filename,
        @Part MultipartBody.Part file,
        @Header("apikey") String apiKey,
        @Header("Authorization") String token
    );

    @POST("rest/v1/media_posts")
    Call<ResponseBody> createMediaPost(
        @HeaderMap Map<String, String> headers,
        @Body MediaPost post
    );

    @GET("rest/v1/text_posts")
    Call<List<TextPost>> getTextPosts(
        @QueryMap Map<String, String> query,
        @Header("apikey") String apiKey,
        @Header("Authorization") String token
    );

    @GET("rest/v1/media_posts")
    Call<List<MediaPost>> getMediaPosts(
        @QueryMap Map<String, String> query,
        @Header("apikey") String apiKey,
        @Header("Authorization") String token
    );

    // Calendario: Obtener ítems por usuario y fecha (opcional)
    @GET("rest/v1/calendar_items")
    Call<List<com.example.neuramusic.model.CalendarItem>> getCalendarItems(
            @QueryMap Map<String, String> query,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // Calendario: Crear ítem
    @POST("rest/v1/calendar_items")
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    Call<List<com.example.neuramusic.model.CalendarItem>> createCalendarItem(
            @Body com.example.neuramusic.model.CalendarItem item,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );


    // Calendario: Actualizar ítem por ID
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

    // Calendario: Eliminar ítem por ID
    @DELETE("rest/v1/calendar_items")
    Call<ResponseBody> deleteCalendarItem(
            @Query("id") String itemId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

}
