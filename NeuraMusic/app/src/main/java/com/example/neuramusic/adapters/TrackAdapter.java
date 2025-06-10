package com.example.neuramusic.adapters;

import android.annotation.SuppressLint;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.Track;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<Track> tracks;
    private final OnTrackClickListener trackClickListener;
    private final OnTrackMenuClickListener menuClickListener;
    private final OnTrackReorderListener reorderListener;

    // === Interfaces ===
    public interface OnTrackClickListener {
        void onTrackClick(int position);
    }

    public interface OnTrackMenuClickListener {
        void onEditTrack(Track track, int position);
        void onDeleteTrack(Track track, int position);
    }

    public interface OnTrackReorderListener {
        void onTrackReordered(); // se notificará tras mover
    }

    // === Constructor ===
    public TrackAdapter(List<Track> tracks,
                        OnTrackClickListener trackClickListener,
                        OnTrackMenuClickListener menuClickListener,
                        OnTrackReorderListener reorderListener) {
        this.tracks = tracks;
        this.trackClickListener = trackClickListener;
        this.menuClickListener = menuClickListener;
        this.reorderListener = reorderListener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track_card, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);

        holder.tvTitle.setText(track.getTitle());
        holder.tvArtist.setText(track.getArtistName() != null ? track.getArtistName() : "Artista desconocido");

      //  holder.itemView.setBackgroundResource(
       //         track.isPlaying() ? R.drawable.bg_track_playing : R.drawable.bg_track_normal
       // );

        holder.itemView.setOnClickListener(v -> {
            if (trackClickListener != null) {
                trackClickListener.onTrackClick(holder.getAdapterPosition());
            }
        });

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, track, holder.getAdapterPosition()));
    }

    @SuppressLint("NonConstantResourceId")
    private void showPopupMenu(View anchor, Track track, int position) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.inflate(R.menu.menu_track_item);

        // Forzar íconos a mostrarse si usas Android 11 o inferior
        try {
            Field mFieldPopup = PopupMenu.class.getDeclaredField("mPopup");
            mFieldPopup.setAccessible(true);
            Object mPopup = mFieldPopup.get(popup);
            mPopup.getClass()
                    .getDeclaredMethod("setForceShowIcon", boolean.class)
                    .invoke(mPopup, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_track) {
                menuClickListener.onEditTrack(track, position);
                return true;
            } else if (id == R.id.action_delete_track) {
                menuClickListener.onDeleteTrack(track, position);
                return true;
            }
            return false;
        });


        popup.show();
    }


    // ✅ Soporte para drag & drop
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tracks, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tracks, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);

        // 🔔 Notificar a la Activity que hay que persistir el cambio
        if (reorderListener != null) {
            reorderListener.onTrackReordered();
        }
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    // === ViewHolder ===
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        ImageButton btnMore;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_track_title);
            tvArtist = itemView.findViewById(R.id.tv_track_artist);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}
