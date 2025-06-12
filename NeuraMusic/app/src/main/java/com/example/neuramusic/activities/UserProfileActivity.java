package com.example.neuramusic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.neuramusic.R;
import com.example.neuramusic.adapters.UserPostAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.FeedPost;
import com.example.neuramusic.model.MediaPost;
import com.example.neuramusic.model.TextPost;
import com.example.neuramusic.model.UserResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvName, tvProfession, tvBio;
    private ImageView ivProfile;
    private ImageButton btnBack, btnInstagram, btnYoutube, btnSpotify, btnSoundcloud;
    private WebView webViewYoutube;
    private ImageButton btnCloseWebView;
    private RecyclerView rvUserPosts;
    private FloatingActionButton fabAdd;
    private SupabaseService supabaseService;
    private SharedPreferences prefs;
    private List<FeedPost> userPostList;
    private UserPostAdapter userPostAdapter;
    private UserResponse currentUser;

    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        String accessToken = prefs.getString("access_token", null);

        if (userId == null || accessToken == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);

        initViews();
        setupRecyclerView();
        loadUserProfile();
        loadUserPosts();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvName = findViewById(R.id.tvName);
        tvProfession = findViewById(R.id.tvProfession);
        tvBio = findViewById(R.id.tvBio);
        ivProfile = findViewById(R.id.ivProfile);
        btnBack = findViewById(R.id.btnBack);
        btnInstagram = findViewById(R.id.btnInstagram);
        btnYoutube = findViewById(R.id.btnYoutube);
        btnSpotify = findViewById(R.id.btnSpotify);
        btnSoundcloud = findViewById(R.id.btnSoundcloud);
        webViewYoutube = findViewById(R.id.webViewYoutube);
        btnCloseWebView = findViewById(R.id.btnCloseWebView);
        fabAdd = findViewById(R.id.fabAdd);

        btnBack.setOnClickListener(v -> onBackPressed());

        ImageButton btnOptions = findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(this::showOptionsMenu);

        btnCloseWebView.setOnClickListener(v -> {
            webViewYoutube.setVisibility(View.GONE);
            btnCloseWebView.setVisibility(View.GONE);
            webViewYoutube.loadUrl("about:blank");
        });

        ImageButton fabAddText = findViewById(R.id.fabAddText);
        ImageButton fabAddMedia = findViewById(R.id.fabAddMedia);
        ImageButton fabAddAudio = findViewById(R.id.fabAddAudio);
        LinearLayout layoutFanButtons = findViewById(R.id.layoutFanButtons);

        fabAdd.setOnClickListener(v -> {
            if (layoutFanButtons.getVisibility() == View.GONE) {
                layoutFanButtons.setVisibility(View.VISIBLE);
            } else {
                layoutFanButtons.setVisibility(View.GONE);
            }
        });

        fabAddText.setOnClickListener(v -> startActivity(new Intent(this, AddTextActivity.class)));
        fabAddMedia.setOnClickListener(v -> startActivity(new Intent(this, AddMediaActivity.class)));
        //fabAddAudio.setOnClickListener(v -> startActivity(new Intent(this, AddAudioActivity.class)));

    }

    private void setupRecyclerView() {
        rvUserPosts = findViewById(R.id.rvUserPosts);
        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        userPostList = new ArrayList<>();
        userPostAdapter = new UserPostAdapter(this, userPostList, prefs.getString("access_token", ""));
        rvUserPosts.setAdapter(userPostAdapter);
    }

    private void loadUserProfile() {
        String userId = prefs.getString("user_id", null);
        String accessToken = prefs.getString("access_token", null);

        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + userId);

        supabaseService.getUserById(query, RetrofitClient.API_KEY, "Bearer " + accessToken)
                .enqueue(new Callback<List<UserResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            populateUserInfo(response.body().get(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                        Toast.makeText(UserProfileActivity.this, "Error cargando perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateUserInfo(UserResponse user) {
        this.currentUser = user;

        tvUsername.setText("@" + user.username);
        tvName.setText(user.fullName != null ? user.fullName : user.username);

        if (user.professions != null && !user.professions.isEmpty()) {
            tvProfession.setText(TextUtils.join(" · ", user.professions));
            tvProfession.setVisibility(View.VISIBLE);
        } else {
            tvProfession.setVisibility(View.GONE);
        }

        tvBio.setText(user.bio != null ? user.bio : "");

        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(user.profileImageUrl)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_user).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_user);
        }

        setupSocialLink(btnInstagram, user.instagram);
        setupSocialLink(btnYoutube, user.youtube);
        setupSocialLink(btnSpotify, user.spotify);
        setupSocialLink(btnSoundcloud, user.soundcloud);

        if (user.youtube != null && !user.youtube.isEmpty()) {
            btnYoutube.setVisibility(View.VISIBLE);
            btnYoutube.setOnClickListener(v -> {
                try {
                    String youtubeUrl = user.youtube;
                    if (youtubeUrl.contains("youtu.be/")) {
                        youtubeUrl = "https://www.youtube.com/embed/" + youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
                    } else if (youtubeUrl.contains("watch?v=")) {
                        String videoId = youtubeUrl.split("watch\\?v=")[1].split("&")[0];
                        youtubeUrl = "https://www.youtube.com/embed/" + videoId;
                    }
                    webViewYoutube.setVisibility(View.VISIBLE);
                    btnCloseWebView.setVisibility(View.VISIBLE);
                    webViewYoutube.getSettings().setJavaScriptEnabled(true);
                    webViewYoutube.loadUrl(youtubeUrl);
                } catch (Exception e) {
                    Toast.makeText(this, "No se pudo cargar el video", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            btnYoutube.setVisibility(View.GONE);
        }
    }

    private void setupSocialLink(ImageButton button, String url) {
        if (url != null && !url.isEmpty()) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void loadUserPosts() {
        userPostList.clear();

        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("access_token", null);

        Map<String, String> query = new HashMap<>();
        query.put("user_id", "eq." + userId);

        supabaseService.getTextPosts(query, RetrofitClient.API_KEY, "Bearer " + token)
                .enqueue(new Callback<List<TextPost>>() {
                    @Override
                    public void onResponse(Call<List<TextPost>> call, Response<List<TextPost>> response) {
                        if (response.isSuccessful() && response.body() != null && currentUser != null) {
                            for (TextPost post : response.body()) {
                                userPostList.add(new FeedPost(
                                        currentUser,
                                        post.getId(),
                                        new ArrayList<>(),
                                        post.getContent(),
                                        false,
                                        post.getCreatedAt()
                                ));
                            }
                            userPostAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TextPost>> call, Throwable t) {
                        Log.e(TAG, "Error al cargar texto", t);
                    }
                });

        supabaseService.getMediaPosts(query, RetrofitClient.API_KEY, "Bearer " + token)
                .enqueue(new Callback<List<MediaPost>>() {
                    @Override
                    public void onResponse(Call<List<MediaPost>> call, Response<List<MediaPost>> response) {
                        if (response.isSuccessful() && response.body() != null && currentUser != null) {
                            for (MediaPost post : response.body()) {
                                userPostList.add(new FeedPost(
                                        currentUser,
                                        post.getId(),
                                        post.getMediaUrls() != null ? post.getMediaUrls() : new ArrayList<>(),
                                        post.getCaption(),
                                        true,
                                        post.getCreatedAt()
                                ));
                            }
                            userPostAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MediaPost>> call, Throwable t) {
                        Log.e(TAG, "Error al cargar multimedia", t);
                    }
                });
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.profile_menu);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit_profile) {
                startActivity(new Intent(this, EditorActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_logout) {
                prefs.edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadUserPosts();
    }
}
