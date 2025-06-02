package com.example.neuramusic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.neuramusic.R;
import com.example.neuramusic.adapters.BlockAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.ProfileBlock;
import com.example.neuramusic.model.UserResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    
    private TextView tvUsername, tvName, tvProfession, tvBio;
    private ImageView ivProfile;
    private ImageButton btnBack;
    private ImageButton btnInstagram, btnYoutube, btnSpotify, btnSoundcloud;
    private FloatingActionButton fabAdd, fabAddText, fabAddMedia, fabAddAudio;
    private View layoutFanButtons;
    private boolean isFanButtonsVisible = false;
    private RecyclerView rvBlocks;
    private BlockAdapter blockAdapter;
    private List<ProfileBlock> blockList;
    private SupabaseService supabaseService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        initViews();
        setupRecyclerView();
        
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
        loadUserProfile();
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
        fabAdd = findViewById(R.id.fabAdd);
        fabAddText = findViewById(R.id.fabAddText);
        fabAddMedia = findViewById(R.id.fabAddMedia);
        fabAddAudio = findViewById(R.id.fabAddAudio);
        layoutFanButtons = findViewById(R.id.layoutFanButtons);
        
        btnBack.setOnClickListener(v -> onBackPressed());
        
        // Configurar menú de opciones
        ImageButton btnOptions = findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(v -> showOptionsMenu(v));
        
        // Configurar botones flotantes
        fabAdd.setOnClickListener(v -> toggleFanButtons());
        fabAddText.setOnClickListener(v -> {
            hideFanButtons();
            startActivity(new Intent(this, AddTextActivity.class));
        });
        fabAddMedia.setOnClickListener(v -> {
            hideFanButtons();
            startActivity(new Intent(this, AddMediaActivity.class));
        });
        fabAddAudio.setOnClickListener(v -> {
            hideFanButtons();
            // TODO: Implementar AddAudioActivity
            Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        rvBlocks = findViewById(R.id.rvBlocks);
        blockList = new ArrayList<>();
        blockAdapter = new BlockAdapter(blockList);
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvBlocks.setLayoutManager(layoutManager);
        rvBlocks.setAdapter(blockAdapter);
    }

    private void loadUserProfile() {
        Log.d(TAG, "Cargando perfil de usuario");
        
        String userId = prefs.getString("user_id", null);
        String accessToken = prefs.getString("access_token", null);
        
        if (userId == null || accessToken == null) {
            Log.e(TAG, "ID de usuario o token nulos");
            showError("Sesión expirada");
            return;
        }
        
        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + userId);

        supabaseService.getUserById(query, RetrofitClient.API_KEY, "Bearer " + accessToken)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            Type listType = new TypeToken<List<UserResponse>>() {}.getType();
                            List<UserResponse> users = new Gson().fromJson(json, listType);

                            if (!users.isEmpty()) {
                                UserResponse user = users.get(0);
                                populateUserInfo(user);
                                Log.d(TAG, "Perfil cargado exitosamente");
                            } else {
                                Log.e(TAG, "Lista de usuarios vacía");
                                showError("No se encontró el perfil");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());
                            showError("Error al cargar el perfil");
                        }
                    } else {
                        Log.e(TAG, "Error en la respuesta: " + response.code());
                        showError("Error al cargar el perfil");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Error de conexión: " + t.getMessage());
                    showError("Error de conexión");
                }
            });
    }

    private void populateUserInfo(UserResponse user) {
        if (user == null) {
            Log.e(TAG, "Usuario nulo, no se puede poblar la información");
            return;
        }
        
        Log.d(TAG, "Poblando información del usuario: " + user.toString());
        
        // Manejo del username y nombre
        tvUsername.setText("@" + user.username);
        tvName.setText(user.fullName != null ? user.fullName : user.username);
        
        // Manejo de profesiones
        if (user.professions != null && !user.professions.isEmpty()) {
            tvProfession.setText(TextUtils.join(" · ", user.professions));
            tvProfession.setVisibility(View.VISIBLE);
        } else {
            tvProfession.setVisibility(View.GONE);
            tvProfession.setText("");
        }
        
        // Manejo de la biografía
        tvBio.setText(user.bio != null ? user.bio : "");

        // Manejo de la imagen de perfil con Glide
        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop();

            Glide.with(this)
                .load(user.profileImageUrl)
                .apply(options)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Error al cargar imagen: " + (e != null ? e.getMessage() : "desconocido"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Imagen cargada exitosamente");
                        return false;
                    }
                })
                .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_user);
        }

        // Configuración de redes sociales
        setupSocialLink(btnInstagram, user.instagram, "Instagram");
        setupSocialLink(btnYoutube, user.youtube, "YouTube");
        setupSocialLink(btnSpotify, user.spotify, "Spotify");
        setupSocialLink(btnSoundcloud, user.soundcloud, "SoundCloud");
    }

    private void setupSocialLink(ImageButton button, String url, String platform) {
        if (url != null && !url.isEmpty()) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error al abrir enlace de " + platform + ": " + e.getMessage());
                    showError("No se pudo abrir el enlace de " + platform);
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.profile_menu);
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                startActivity(new Intent(this, EditorActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void toggleFanButtons() {
        if (isFanButtonsVisible) {
            hideFanButtons();
        } else {
            showFanButtons();
        }
    }

    private void showFanButtons() {
        layoutFanButtons.setVisibility(View.VISIBLE);
        isFanButtonsVisible = true;
        fabAdd.animate().rotation(45f);
    }

    private void hideFanButtons() {
        layoutFanButtons.setVisibility(View.GONE);
        isFanButtonsVisible = false;
        fabAdd.animate().rotation(0f);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
