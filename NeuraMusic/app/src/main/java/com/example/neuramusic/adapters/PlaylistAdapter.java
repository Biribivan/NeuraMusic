package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final List<Playlist> allPlaylists;
    private final List<Playlist> visiblePlaylists = new ArrayList<>();
    private final OnPlaylistClickListener clickListener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public PlaylistAdapter(List<Playlist> playlists, OnPlaylistClickListener clickListener) {
        this.allPlaylists = playlists;
        this.clickListener = clickListener;
        updateVisiblePlaylists(null);
    }

    private void updateVisiblePlaylists(String parentId) {
        visiblePlaylists.clear();
        addChildrenRecursively(parentId, 0);
        notifyDataSetChanged();
    }

    private void addChildrenRecursively(String parentId, int depth) {
        for (Playlist p : allPlaylists) {
            if ((parentId == null && p.getParentId() == null) ||
                    (parentId != null && parentId.equals(p.getParentId()))) {
                p.setDepth(depth);
                visiblePlaylists.add(p);
                if (p.isExpanded()) {
                    addChildrenRecursively(p.getId(), depth + 1);
                }
            }
        }
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = visiblePlaylists.get(position);
        holder.tvTitle.setText(playlist.getTitle());

        // Icono según tipo
        if ("folder".equals(playlist.getType())) {
            holder.ivIcon.setImageResource(R.drawable.ic_folder);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_playlist);
        }

        // Flecha para expandir/collapse
        if (hasChildren(playlist)) {
            holder.ivArrow.setVisibility(View.VISIBLE);
            holder.ivArrow.setImageResource(
                    playlist.isExpanded() ? R.drawable.ic_chevron_up : R.drawable.ic_chevron
            );
            holder.ivArrow.setOnClickListener(v -> {
                playlist.setExpanded(!playlist.isExpanded());
                updateVisiblePlaylists(null);
            });
        } else {
            holder.ivArrow.setVisibility(View.INVISIBLE);
        }

        // Margen por profundidad
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        int leftMargin = 24 * playlist.getDepth();
        params.setMargins(leftMargin, params.topMargin, params.rightMargin, params.bottomMargin);
        holder.itemView.setLayoutParams(params);

        // Click en playlist (solo si no es folder)
        holder.itemView.setOnClickListener(v -> {
            if (!"folder".equals(playlist.getType())) {
                clickListener.onPlaylistClick(playlist);
            }
        });
    }

    private boolean hasChildren(Playlist playlist) {
        for (Playlist p : allPlaylists) {
            if (playlist.getId().equals(p.getParentId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return visiblePlaylists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivArrow;
        TextView tvTitle;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            tvTitle = itemView.findViewById(R.id.tv_playlist_title);
        }
    }

    public void setPlaylistList(List<Playlist> playlists) {
        this.allPlaylists.clear();
        this.allPlaylists.addAll(playlists);
        updateVisiblePlaylists(null);
    }
}
