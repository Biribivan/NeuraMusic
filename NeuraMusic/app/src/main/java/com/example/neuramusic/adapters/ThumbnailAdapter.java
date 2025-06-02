package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.MediaItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {
    private final List<MediaItem> mediaItems;
    private final OnThumbnailClickListener listener;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public ThumbnailAdapter(List<MediaItem> mediaItems, OnThumbnailClickListener listener) {
        this.mediaItems = mediaItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        
        Glide.with(holder.imageView.getContext())
                .load(item.getUri())
                .centerCrop()
                .into(holder.imageView);

        if (item.isVideo()) {
            holder.videoIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.videoIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThumbnailClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView videoIndicator;

        ThumbnailViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.thumbnailImage);
            videoIndicator = itemView.findViewById(R.id.videoIndicator);
        }
    }
} 