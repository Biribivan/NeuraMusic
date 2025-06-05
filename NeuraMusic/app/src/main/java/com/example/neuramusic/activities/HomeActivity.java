package com.example.neuramusic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.fragments.CalendarFragment;
import com.example.neuramusic.fragments.HomeFragment;
import com.example.neuramusic.fragments.LibraryFragment;
import com.example.neuramusic.fragments.SearchFragment;
import com.example.neuramusic.model.UserResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main activity displayed after login. Hosts the feed and other sections
 * using a BottomNavigationView similar to Instagram.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private BottomNavigationView bottomNavigation;
    private ImageView ivProfile, ivChat;
    private TextView tvUsername;
    private SupabaseService supabaseService;

    private String uid;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        ivProfile = findViewById(R.id.ivUserProfile);
        tvUsername = findViewById(R.id.tvUsername);
        ivChat = findViewById(R.id.ivChat);

        ivChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        ivProfile.setOnClickListener(v ->
                startActivity(new Intent(this, UserProfileActivity.class)));

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);

        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        uid = prefs.getString("user_id", null);
        token = prefs.getString("access_token", null);

        if (uid != null && token != null) {
            loadUserData(uid, token);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.nav_search) {
                loadFragment(new SearchFragment());
            } else if (id == R.id.nav_library) {
                loadFragment(new LibraryFragment());
            } else if (id == R.id.nav_calendar) {
                if (uid != null && token != null) {
                    loadFragment(new CalendarFragment(token, uid));
                } else {
                    Toast.makeText(this, "No se ha iniciado sesión correctamente", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadUserData(String userId, String token) {
        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + userId);

        supabaseService.getUserById(query, RetrofitClient.API_KEY, "Bearer " + token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String json = response.body().string();
                                Type listType = new TypeToken<List<UserResponse>>(){}.getType();
                                List<UserResponse> users = new Gson().fromJson(json, listType);
                                if (!users.isEmpty()) {
                                    updateUI(users.get(0));
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear respuesta", e);
                                Toast.makeText(HomeActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error en la respuesta: " + response.code());
                            Toast.makeText(HomeActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error de red", t);
                        Toast.makeText(HomeActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(UserResponse user) {
        tvUsername.setText(user.username);

        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.ic_user);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_artist_toolbar, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
