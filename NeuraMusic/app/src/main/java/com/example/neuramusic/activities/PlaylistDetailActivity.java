package com.example.neuramusic.activities;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.TrackAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.fragments.EditTrackDialogFragment;
import com.example.neuramusic.model.Track;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistDetailActivity extends AppCompatActivity implements TrackAdapter.OnTrackReorderListener {

    private String playlistId;
    private String playlistTitle;

    private RecyclerView rvTracks;
    private ProgressBar progressBar;
    private LinearLayout miniPlayer;
    private TextView tvCurrentTrack;
    private ImageButton btnPrev, btnPlayPause, btnNext;

    private final List<Track> trackList = new ArrayList<>();
    private TrackAdapter adapter;
    private MediaPlayer mediaPlayer;
    private int currentTrackIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        playlistId = getIntent().getStringExtra("playlist_id");
        playlistTitle = getIntent().getStringExtra("playlist_title");

        setupToolbar();

        rvTracks = findViewById(R.id.rv_tracks);
        progressBar = findViewById(R.id.progress_bar);
        miniPlayer = findViewById(R.id.mini_player);
        tvCurrentTrack = findViewById(R.id.tv_current_track);
        btnPrev = findViewById(R.id.btn_prev);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnNext = findViewById(R.id.btn_next);

        adapter = new TrackAdapter(trackList, this::playTrackAt,
                new TrackAdapter.OnTrackMenuClickListener() {
                    @Override
                    public void onEditTrack(Track track, int position) {
                        showEditTrackDialog(track, position);
                    }

                    @Override
                    public void onDeleteTrack(Track track, int position) {
                        confirmDeleteTrack(track, position);
                    }
                }, this);

        rvTracks.setLayoutManager(new LinearLayoutManager(this));
        rvTracks.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                adapter.moveItem(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(rvTracks);

        btnPrev.setOnClickListener(v -> playPrevious());
        btnNext.setOnClickListener(v -> playNext());
        btnPlayPause.setOnClickListener(v -> togglePlayback());

        loadTracks();
    }

    private void loadTracks() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);

        Map<String, String> query = new HashMap<>();
        query.put("playlist_id", "eq." + playlistId);

        api.getTracks(query, RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<List<Track>>() {
                    @Override
                    public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            trackList.clear();
                            trackList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(PlaylistDetailActivity.this, "Error al cargar canciones", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Track>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PlaylistDetailActivity.this, "Error de red al cargar", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void showEditTrackDialog(Track track, int position) {
        EditTrackDialogFragment dialog = EditTrackDialogFragment.newInstance(track, updatedTrack -> {
            if (updatedTrack.getLocalUri() == null) updatedTrack.setLocalUri("");
            updatedTrack.setPlaylistId(playlistId);
            createOrUpdateTrack(updatedTrack, position);
        });
        dialog.show(getSupportFragmentManager(), "EditTrackDialog");
    }

    private void createOrUpdateTrack(Track track, int position) {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        String token = "Bearer " + RetrofitClient.getAccessToken();

        if (track.getId() == null) {
            api.createSingleTrack(track, RetrofitClient.API_KEY, token)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String json = response.body().string();
                                    Type listType = new TypeToken<List<Track>>(){}.getType();
                                    List<Track> tracks = new Gson().fromJson(json, listType);
                                    if (!tracks.isEmpty()) {
                                        Track saved = tracks.get(0);
                                        trackList.add(saved);
                                        adapter.notifyItemInserted(trackList.size() - 1);
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(PlaylistDetailActivity.this, "Error procesando respuesta", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PlaylistDetailActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(PlaylistDetailActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", track.getTitle());
            updates.put("artist_name", track.getArtistName());
            updates.put("genre", track.getGenre());

            api.updateTable("track", "eq.id." + track.getId(), RetrofitClient.API_KEY, token, updates)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                trackList.set(position, track);
                                adapter.notifyItemChanged(position);
                            } else {
                                Toast.makeText(PlaylistDetailActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(PlaylistDetailActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void confirmDeleteTrack(Track track, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar canción")
                .setMessage("¿Seguro que quieres eliminar esta canción?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    trackList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(playlistTitle);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.inflateMenu(R.menu.menu_playlist_detail);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(this::onToolbarItemSelected);
    }

    private boolean onToolbarItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_track) {
            Track newTrack = new Track();
            newTrack.setTitle("");
            newTrack.setArtistName("");
            newTrack.setGenre("");
            newTrack.setPlaylistId(playlistId);
            showEditTrackDialog(newTrack, -1);
            return true;
        }
        return false;
    }

    private void playTrackAt(int position) {
        Track track = trackList.get(position);
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, Uri.parse(track.getLocalUri()));
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentTrackIndex = position;
            updateMiniPlayer(track);
        } catch (IOException e) {
            Toast.makeText(this, "Error al reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMiniPlayer(Track track) {
        miniPlayer.setVisibility(View.VISIBLE);
        tvCurrentTrack.setText(track.getTitle());
    }

    private void togglePlayback() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void playPrevious() {
        if (currentTrackIndex > 0) playTrackAt(currentTrackIndex - 1);
    }

    private void playNext() {
        if (currentTrackIndex < trackList.size() - 1) playTrackAt(currentTrackIndex + 1);
    }

    @Override
    public void onTrackReordered() {
        // Persistir orden si es necesario
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
