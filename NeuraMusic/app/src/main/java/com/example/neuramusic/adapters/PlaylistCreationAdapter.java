package com.example.neuramusic.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.Playlist;

import java.util.List;

public class PlaylistCreationAdapter extends RecyclerView.Adapter<PlaylistCreationAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlistList;
    private final String colorHex;
    private final OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void onDeleteRequested(int position);
    }

    public PlaylistCreationAdapter(List<Playlist> playlistList, String colorHex, OnDeleteListener onDeleteListener) {
        this.playlistList = playlistList;
        this.colorHex = colorHex;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_new_playlist_input, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);

        // Avoid infinite loops by removing old watcher
        holder.etPlaylistTitle.setText(playlist.getTitle());
        holder.etPlaylistTitle.setTag(position); // avoid re-entry

        holder.etPlaylistTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    playlistList.get(holder.getAdapterPosition()).setTitle(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteListener != null) {
                onDeleteListener.onDeleteRequested(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        EditText etPlaylistTitle;
        ImageButton btnDelete;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            etPlaylistTitle = itemView.findViewById(R.id.et_playlist_title);
            btnDelete = itemView.findViewById(R.id.btn_delete_playlist);
        }
    }
}
