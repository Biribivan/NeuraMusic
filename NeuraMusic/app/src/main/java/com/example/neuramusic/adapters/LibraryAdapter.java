package com.example.neuramusic.adapters;

import android.content.Context;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.Playlist;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {

    private final Context context;
    private final List<Playlist> visiblePlaylists;
    private final FolderClickListener folderClickListener;
    private final FolderLongClickListener folderLongClickListener;
    private final OnPlaylistClickListener playlistClickListener;

    // === Interfaces ===
    public interface FolderClickListener {
        void onFolderClick(int position);
    }

    public interface FolderLongClickListener {
        void onFolderLongClick(Playlist folder);
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    // === Constructor ===
    public LibraryAdapter(Context context,
                          List<Playlist> visiblePlaylists,
                          FolderClickListener folderClickListener,
                          FolderLongClickListener folderLongClickListener,
                          OnPlaylistClickListener playlistClickListener) {
        this.context = context;
        this.visiblePlaylists = visiblePlaylists;
        this.folderClickListener = folderClickListener;
        this.folderLongClickListener = folderLongClickListener;
        this.playlistClickListener = playlistClickListener; // ✅ ASIGNACIÓN
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Playlist item = visiblePlaylists.get(position);
        holder.title.setText(item.getTitle());

        // Sangría visual
        int paddingStart = 40 + (item.getDepth() * 30);
        holder.itemView.setPadding(paddingStart, holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());

        boolean isFolder = "folder".equals(item.getType());
        holder.arrow.setVisibility(isFolder ? View.VISIBLE : View.GONE);
        holder.icon.setImageResource(isFolder ? R.drawable.ic_folder : R.drawable.ic_playlist);

        if (isFolder) {
            holder.arrow.setRotation(item.isExpanded() ? 90f : 0f);

            holder.itemView.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
                folderClickListener.onFolderClick(holder.getAdapterPosition());
            });

            holder.itemView.setOnLongClickListener(v -> {
                folderLongClickListener.onFolderLongClick(item);
                return true;
            });
        } else {
            //  Playlist real: lanzar listener de navegación
            holder.itemView.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
                playlistClickListener.onPlaylistClick(item);
            });

            // Puedes agregar long click para editar o eliminar si lo deseas
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return visiblePlaylists.size();
    }

    // == ViewHolder ===
    static class LibraryViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, arrow;
        TextView title;

        public LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            arrow = itemView.findViewById(R.id.iv_arrow);
            title = itemView.findViewById(R.id.tv_playlist_title);
        }
    }
}
