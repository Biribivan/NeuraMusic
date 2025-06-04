package com.example.neuramusic.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.FeedAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.FeedPost;
import com.example.neuramusic.model.MediaPost;
import com.example.neuramusic.model.TextPost;
import com.example.neuramusic.model.UserResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_feed);
        progressBar = view.findViewById(R.id.progress_loading);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                            FeedPost fp = new FeedPost(null, tp.getContent(), null, tp.getCreatedAt(), false);
                            fp.user = new UserResponse();
                            fp.user.uid = tp.getUserId();
                            feedPosts.add(fp);
                        }
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
                            FeedPost fp = new FeedPost(null, mp.getCaption(), mp.getMediaUrls(), mp.getCreatedAt(), true);
                            fp.user = new UserResponse();
                            fp.user.uid = mp.getUserId();
                            feedPosts.add(fp);
                        }
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

        final int[] remaining = {feedPosts.size()};
        for (FeedPost post : feedPosts) {
            Map<String, String> q = new HashMap<>();
            q.put("id", "eq." + post.user.uid);
            supabaseService.getUserById(q, RetrofitClient.API_KEY, "Bearer " + token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String json = response.body().string();
                                Type listType = new TypeToken<List<UserResponse>>(){}.getType();
                                List<UserResponse> users = new Gson().fromJson(json, listType);
                                if (!users.isEmpty()) {
                                    post.user = users.get(0);
                                }
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Error parseando usuario", e);
                            }
                        }
                        if (--remaining[0] == 0) updateFeed();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("HomeFragment", "Error obteniendo usuario", t);
                        if (--remaining[0] == 0) updateFeed();
                    }
                });
        }
    }

    private void updateFeed() {
        Collections.sort(feedPosts, (a, b) -> b.createdAt.compareToIgnoreCase(a.createdAt));
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Error al cargar feed", Toast.LENGTH_SHORT).show();
    }
}
