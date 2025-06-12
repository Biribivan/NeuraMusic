package com.example.neuramusic.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.MediaPreviewAdapter;
import com.example.neuramusic.adapters.ThumbnailAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.MediaItem;
import com.example.neuramusic.model.MediaPost;
import com.example.neuramusic.model.SupabaseSignupResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMediaActivity extends AppCompatActivity {
    private static final String TAG = "AddMediaActivity";
    private static final int MAX_MEDIA_ITEMS = 5;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };
    private static final String BUCKET_NAME = "post-media";
    private static final String STORAGE_URL = "https://lxoxhdmihydjotsggpco.supabase.co/storage/v1/object/public/" + BUCKET_NAME + "/";

    private View placeholderView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private RecyclerView rvThumbnails;
    private MaterialButton btnPublish;
    private TextInputEditText etCaption;
    private FloatingActionButton fabAddMore;

    private List<MediaItem> selectedMedia;
    private MediaPreviewAdapter previewAdapter;
    private ThumbnailAdapter thumbnailAdapter;
    private SupabaseService supabaseService;

    private final ActivityResultLauncher<String[]> mediaPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    handleSelectedMedia(uris);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_media);

        initViews();
        setupAdapters();
        setupListeners();
        checkPermissions();

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (viewPager != null && !selectedMedia.isEmpty()) {
            viewPager.setCurrentItem(viewPager.getCurrentItem());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager != null && viewPager.getAdapter() != null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem >= 0 && currentItem < selectedMedia.size()) {
                MediaItem item = selectedMedia.get(currentItem);
                if (item.isVideo()) {
                    View view = viewPager.getChildAt(0);
                    if (view != null) {
                        VideoView videoView = view.findViewById(R.id.videoView);
                        if (videoView != null && !videoView.isPlaying()) {
                            videoView.start();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewPager != null) {
            View view = viewPager.getChildAt(0);
            if (view != null) {
                VideoView videoView = view.findViewById(R.id.videoView);
                if (videoView != null && videoView.isPlaying()) {
                    videoView.pause();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (viewPager != null) {
            View view = viewPager.getChildAt(0);
            if (view != null) {
                VideoView videoView = view.findViewById(R.id.videoView);
                if (videoView != null) {
                    videoView.stopPlayback();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        rvThumbnails = findViewById(R.id.rvThumbnails);
        btnPublish = findViewById(R.id.btnPublish);
        etCaption = findViewById(R.id.etCaption);
        placeholderView = findViewById(R.id.placeholderView);
        fabAddMore = findViewById(R.id.fabAddMore);

        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer((page, position) -> {
            page.setTranslationX(-position * page.getWidth());
            page.setAlpha(1.0f);
            page.setScaleX(1.0f);
            page.setScaleY(1.0f);
        });

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        placeholderView.setOnClickListener(v -> openMediaPicker());
        fabAddMore.setOnClickListener(v -> openMediaPicker());
    }

    private void setupAdapters() {
        selectedMedia = new ArrayList<>();
        previewAdapter = new MediaPreviewAdapter(selectedMedia, this::openMediaPicker);
        viewPager.setAdapter(previewAdapter);
        viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        thumbnailAdapter = new ThumbnailAdapter(selectedMedia, position -> {
            viewPager.setCurrentItem(position, true);
        });
        rvThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvThumbnails.setAdapter(thumbnailAdapter);

        updateViewsVisibility();
    }

    private void setupListeners() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                rvThumbnails.smoothScrollToPosition(position);
            }
        });

        etCaption.setOnFocusChangeListener((v, hasFocus) -> {
            View parent = (View) v.getParent().getParent();
            parent.animate().translationZ(hasFocus ? 8f : 0f).setDuration(200).start();
        });

        btnPublish.setOnClickListener(v -> publishPost());

    }

    private void openMediaPicker() {
        if (selectedMedia.size() < MAX_MEDIA_ITEMS) {
            mediaPickerLauncher.launch(new String[]{"image/*", "video/*"});
        } else {
            Toast.makeText(this, R.string.media_limit_reached, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateViewsVisibility() {
        boolean hasMedia = !selectedMedia.isEmpty();
        placeholderView.setVisibility(hasMedia ? View.GONE : View.VISIBLE);
        viewPager.setVisibility(hasMedia ? View.VISIBLE : View.GONE);
        tabLayout.setVisibility(hasMedia && selectedMedia.size() > 1 ? View.VISIBLE : View.GONE);
        rvThumbnails.setVisibility(hasMedia && selectedMedia.size() > 1 ? View.VISIBLE : View.GONE);
        fabAddMore.setVisibility(View.VISIBLE);
        btnPublish.setEnabled(hasMedia);
    }

    private void handleSelectedMedia(List<Uri> uris) {
        int remainingSlots = MAX_MEDIA_ITEMS - selectedMedia.size();
        int itemsToAdd = Math.min(uris.size(), remainingSlots);

        for (int i = 0; i < itemsToAdd; i++) {
            Uri uri = uris.get(i);
            String mimeType = getContentResolver().getType(uri);
            boolean isVideo = mimeType != null && mimeType.startsWith("video/");

            MediaItem mediaItem = new MediaItem(uri, isVideo);
            if (isVideo) setVideoAspectRatio(mediaItem);
            else setImageAspectRatio(mediaItem);

            selectedMedia.add(mediaItem);
        }

        previewAdapter.notifyDataSetChanged();
        thumbnailAdapter.notifyDataSetChanged();
        updateViewsVisibility();

        if (!selectedMedia.isEmpty()) {
            int newPosition = selectedMedia.size() - 1;
            viewPager.setCurrentItem(newPosition, true);
            rvThumbnails.smoothScrollToPosition(newPosition);
        }
    }

    private void setVideoAspectRatio(MediaItem mediaItem) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, mediaItem.getUri());
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (width != null && height != null) {
                float aspectRatio = Float.parseFloat(width) / Float.parseFloat(height);
                mediaItem.setAspectRatio(aspectRatio);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageAspectRatio(MediaItem mediaItem) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mediaItem.getUri());
            float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
            mediaItem.setAspectRatio(aspectRatio);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void publishPost() {
        btnPublish.setEnabled(false);
        String caption = etCaption.getText().toString();

        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String accessToken = prefs.getString("access_token", null);

        if (userId == null || accessToken == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Publicando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> mediaUrls = new ArrayList<>();
        final int[] uploadedCount = {0};

        for (MediaItem mediaItem : selectedMedia) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(mediaItem.getUri());
                byte[] bytes = IOUtils.toByteArray(inputStream);

                String extension = mediaItem.isVideo() ? ".mp4" : ".jpg";
                String filename = "post_" + userId + "_" + System.currentTimeMillis() + extension;
                String mimeType = mediaItem.isVideo() ? "video/*" : "image/jpeg";

                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, requestFile);

                supabaseService.uploadPostMedia(filename, body, RetrofitClient.API_KEY, "Bearer " + accessToken)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (!response.isSuccessful() || response.body() == null) {
                                    handleError(new Exception("Error al subir medio"), progressDialog);
                                    return;
                                }

                                String publicUrl = STORAGE_URL + filename;
                                mediaUrls.add(publicUrl);
                                uploadedCount[0]++;

                                if (uploadedCount[0] == selectedMedia.size()) {
                                    createMediaPost(userId, accessToken, caption, mediaUrls, progressDialog);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                handleError(t, progressDialog);
                            }
                        });

            } catch (Exception e) {
                handleError(e, progressDialog);
                return;
            }
        }
    }

    private void createMediaPost(String userId, String accessToken, String caption,
                                 List<String> mediaUrls, ProgressDialog progressDialog) {
        MediaPost post = new MediaPost(userId, caption, mediaUrls);

        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", RetrofitClient.API_KEY);
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json");
        headers.put("Prefer", "return=minimal");

        supabaseService.createMediaPost(headers, post).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(AddMediaActivity.this, "Publicado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnPublish.setEnabled(true);
                    Toast.makeText(AddMediaActivity.this, "Error al crear el post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                btnPublish.setEnabled(true);
                Toast.makeText(AddMediaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleError(Throwable t, ProgressDialog progressDialog) {
        progressDialog.dismiss();
        btnPublish.setEnabled(true);

        if (t.getMessage() != null && t.getMessage().contains("jwt expired")) {
            refreshToken();
            return;
        }

        Toast.makeText(this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error detallado: ", t);
    }

    private void refreshToken() {
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        String refreshToken = prefs.getString("refresh_token", null);

        if (refreshToken == null) {
            redirectToLogin();
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", RetrofitClient.API_KEY);
        headers.put("Content-Type", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        SupabaseService authService = RetrofitClient.getSupabaseAuthService();
        authService.refreshAccessToken(headers, body).enqueue(new Callback<SupabaseSignupResponse>() {
            @Override
            public void onResponse(Call<SupabaseSignupResponse> call, Response<SupabaseSignupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SupabaseSignupResponse signupResponse = response.body();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("access_token", signupResponse.access_token);
                    editor.putString("refresh_token", signupResponse.refresh_token);
                    editor.apply();

                    publishPost();
                } else {
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<SupabaseSignupResponse> call, Throwable t) {
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void checkPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            Toast.makeText(this, "Se necesitan permisos para acceder a los medios", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
