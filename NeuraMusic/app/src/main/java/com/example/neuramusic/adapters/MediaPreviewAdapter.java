package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.widget.MediaController;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.neuramusic.R;
import com.example.neuramusic.activities.VisualizadorMediaActivity;
import com.example.neuramusic.model.MediaItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.MediaViewHolder> {
    private final List<MediaItem> mediaItems;
    private final OnAddMoreClickListener addMoreListener;
    private static final float[] ASPECT_RATIOS = {1.0f, 0.8f, 0.5625f}; // 1:1, 4:5, 9:16
    private static final String[] ASPECT_RATIO_LABELS = {"1:1", "4:5", "9:16"};

    public interface OnAddMoreClickListener {
        void onAddMoreClick();
    }

    public MediaPreviewAdapter(List<MediaItem> mediaItems, OnAddMoreClickListener addMoreListener) {
        this.mediaItems = mediaItems;
        this.addMoreListener = addMoreListener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_preview, parent, false);
        
        // Asegurar que el elemento ocupe todo el espacio del ViewPager2
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        view.setLayoutParams(layoutParams);
        
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        
        if (item.isVideo()) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            
            try {
                // Configurar MediaController
                MediaController mediaController = new MediaController(holder.videoView.getContext());
                mediaController.setAnchorView(holder.videoView);
                holder.videoView.setMediaController(mediaController);
                
                // Ajustar las dimensiones del VideoView según la relación de aspecto
                ViewGroup.LayoutParams params = holder.videoView.getLayoutParams();
                if (params instanceof ConstraintLayout.LayoutParams) {
                    ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) params;
                    float aspectRatio = item.getAspectRatio();
                    if (aspectRatio > 0) {
                        constraintParams.dimensionRatio = String.format("H,%f:1", aspectRatio);
                        holder.videoView.setLayoutParams(constraintParams);
                    }
                }
                
                holder.videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    holder.videoView.start();
                });
                holder.videoView.setVideoURI(item.getUri());

                // Añadir click listener para abrir el visualizador
                holder.videoView.setOnClickListener(v -> {
                    if (v.getContext() instanceof AppCompatActivity) {
                        VisualizadorMediaActivity.launch(
                            (AppCompatActivity) v.getContext(),
                            item.getUri(),
                            true
                        );
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            
            // Ajustar las dimensiones del ImageView según la relación de aspecto
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            if (params instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) params;
                float aspectRatio = item.getAspectRatio();
                if (aspectRatio > 0) {
                    constraintParams.dimensionRatio = String.format("H,%f:1", aspectRatio);
                    holder.imageView.setLayoutParams(constraintParams);
                }
            }

            Glide.with(holder.imageView.getContext())
                    .load(item.getUri())
                    .apply(new RequestOptions()
                            .fitCenter()
                            .dontTransform())
                    .into(holder.imageView);

            // Añadir click listener para abrir el visualizador
            holder.imageView.setOnClickListener(v -> {
                if (v.getContext() instanceof AppCompatActivity) {
                    VisualizadorMediaActivity.launch(
                        (AppCompatActivity) v.getContext(),
                        item.getUri(),
                        false
                    );
                }
            });
        }

        // Configurar el botón de relación de aspecto
        holder.btnAspectRatio.setText(ASPECT_RATIO_LABELS[item.getAspectRatioIndex()]);
        holder.btnAspectRatio.setOnClickListener(v -> {
            int nextIndex = (item.getAspectRatioIndex() + 1) % ASPECT_RATIOS.length;
            item.setAspectRatioIndex(nextIndex);
            item.setAspectRatio(ASPECT_RATIOS[nextIndex]);
            holder.btnAspectRatio.setText(ASPECT_RATIO_LABELS[nextIndex]);
            notifyItemChanged(position);
        });

        // Configurar el botón de añadir más
        holder.fabAddMore.setOnClickListener(v -> {
            if (addMoreListener != null) {
                addMoreListener.onAddMoreClick();
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.videoView.getVisibility() == View.VISIBLE) {
            holder.videoView.start();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediaViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.videoView.isPlaying()) {
            holder.videoView.pause();
        }
    }

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.videoView.isPlaying()) {
            holder.videoView.stopPlayback();
        }
        holder.videoView.setOnPreparedListener(null);
        Glide.with(holder.imageView.getContext()).clear(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageView;
        VideoView videoView;
        MaterialButton btnAspectRatio;
        FloatingActionButton fabAddMore;

        MediaViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoView = itemView.findViewById(R.id.videoView);
            btnAspectRatio = itemView.findViewById(R.id.btnAspectRatio);
            fabAddMore = itemView.findViewById(R.id.fabAddMore);
        }
    }
} 