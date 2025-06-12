package com.example.neuramusic.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.FeedAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.FeedPost;
import com.example.neuramusic.model.MediaPost;
import com.example.neuramusic.model.TextPost;
import com.example.neuramusic.model.UserResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FeedAdapter adapter;
    private final List<FeedPost> feedPosts = new ArrayList<>();
    private SupabaseService supabaseService;
    private String token;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmpty;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        tvEmpty = view.findViewById(R.id.tv_empty);

        swipeRefreshLayout.setOnRefreshListener(() -> loadFeed());


        recyclerView = view.findViewById(R.id.recycler_feed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        progressBar = view.findViewById(R.id.progress_loading);

        adapter = new FeedAdapter(feedPosts);
        recyclerView.setAdapter(adapter);

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("NeuraPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("access_token", null);

        if (token != null) {
            loadFeed();
        } else {
            Toast.makeText(getContext(), "No se encontró token de acceso", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFeed() {
        progressBar.setVisibility(View.VISIBLE);
        feedPosts.clear();

        Map<String, String> query = new HashMap<>();
        query.put("order", "created_at.desc");

        supabaseService.getTextPosts(query, RetrofitClient.API_KEY, "Bearer " + token)
                .enqueue(new Callback<List<TextPost>>() {
                    @Override
                    public void onResponse(Call<List<TextPost>> call, Response<List<TextPost>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (TextPost tp : response.body()) {
                                FeedPost fp = new FeedPost(
                                        new UserResponse(tp.getUserId()), // se crea un UserResponse con solo el uid
                                        tp.getId(),
                                        new ArrayList<>(),           // no hay media
                                        tp.getContent(),             // texto como caption
                                        false,
                                        tp.getCreatedAt()
                                );
                                feedPosts.add(fp);
                            }
                            Log.d("HomeFragment", "Text posts cargados: " + feedPosts.size());
                            loadMediaPosts();
                        } else {
                            showError();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TextPost>> call, Throwable t) {
                        Log.e("HomeFragment", "Error cargando posts de texto", t);
                        showError();
                    }
                });
    }

private void loadMediaPosts() {
    Map<String, String> query = new HashMap<>();
    query.put("order", "created_at.desc");

    supabaseService.getMediaPosts(query, RetrofitClient.API_KEY, "Bearer " + token)
            .enqueue(new Callback<List<MediaPost>>() {
                @Override
                public void onResponse(Call<List<MediaPost>> call, Response<List<MediaPost>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (MediaPost mp : response.body()) {
                            FeedPost fp = new FeedPost(
                                    new UserResponse(mp.getUserId()),                  // usuario mínimo con solo uid
                                    mp.getId(),
                                    mp.getMediaUrls() != null ? mp.getMediaUrls() : new ArrayList<>(),
                                    mp.getCaption() != null ? mp.getCaption() : "",
                                    true,
                                    mp.getCreatedAt()
                            );
                            feedPosts.add(fp);
                        }
                        Log.d("HomeFragment", "Media posts cargados: " + response.body().size());
                        fetchUsers();
                    } else {
                        showError();
                    }
                }

                @Override
                public void onFailure(Call<List<MediaPost>> call, Throwable t) {
                    Log.e("HomeFragment", "Error cargando posts multimedia", t);
                    showError();
                }
            });
}


private void fetchUsers() {
        if (feedPosts.isEmpty()) {
            updateFeed();
            return;
        }

        AtomicInteger remaining = new AtomicInteger(feedPosts.size());
        for (FeedPost post : feedPosts) {
            Map<String, String> q = new HashMap<>();
            q.put("id", "eq." + post.user.uid);

            supabaseService.getUserById(q, RetrofitClient.API_KEY, "Bearer " + token)
                    .enqueue(new Callback<List<UserResponse>>() {
                        @Override
                        public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                post.user = response.body().get(0);
                                Log.d("UsuarioCargado", post.user.toString());
                            } else {
                                Log.w("UsuarioCargado", "Usuario no encontrado para uid=" + post.user.uid);
                            }

                            if (remaining.decrementAndGet() == 0) updateFeed();
                        }

                        @Override
                        public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                            Log.e("HomeFragment", "Error obteniendo usuario con id=" + post.user.uid, t);
                            if (remaining.decrementAndGet() == 0) updateFeed();
                        }
                    });
        }
    }

    private void updateFeed() {
        Log.d("HomeFragment", "Actualizando feed con " + feedPosts.size() + " posts");
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);

        if (feedPosts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }

        Collections.sort(feedPosts, (a, b) -> {
            if (a.createdAt == null) return 1;
            if (b.createdAt == null) return -1;
            return b.createdAt.compareToIgnoreCase(a.createdAt);
        });

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);

        if (feedPosts.isEmpty()) {
            Toast.makeText(getContext(), "No hay publicaciones disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Error al cargar feed", Toast.LENGTH_SHORT).show();
    }
}
