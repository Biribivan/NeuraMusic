package com.example.neuramusic.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.example.neuramusic.R;

@UnstableApi
public class VisualizadorMediaActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ImageView imageView;
    private ExoPlayer player;
    private ImageButton btnClose;
    private ImageButton btnPip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizador_media);

        // Inicializar vistas
        playerView = findViewById(R.id.playerView);
        imageView = findViewById(R.id.imageView);
        btnClose = findViewById(R.id.btnClose);
        btnPip = findViewById(R.id.btnPip);

        // Configurar ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Configurar botones
        btnClose.setOnClickListener(v -> finish());
        btnPip.setOnClickListener(v -> entrarModoPip());

        // Obtener y reproducir el medio
        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String mediaType = getIntent().getStringExtra("mediaType");

        if (mediaUrl != null) {
            if ("video".equals(mediaType)) {
                mostrarVideo(mediaUrl);
            } else if ("image".equals(mediaType)) {
                mostrarImagen(mediaUrl);
            }
        }
    }

    private void mostrarVideo(String url) {
        playerView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        btnPip.setVisibility(View.VISIBLE);

        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void mostrarImagen(String url) {
        playerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        btnPip.setVisibility(View.GONE);
        // Cargar imagen con Glide
        Glide.with(this).load(url).into(imageView);
    }

    private void entrarModoPip() {
        // Implementar modo Picture-in-Picture aquí
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }
} 