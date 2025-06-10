package com.example.neuramusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.neuramusic.R;
import com.example.neuramusic.model.Track;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;

public class EditTrackDialogFragment extends DialogFragment {

    private static final String ARG_TRACK = "arg_track";
    private Track track;
    private Consumer<Track> onTrackUpdated;

    public static EditTrackDialogFragment newInstance(Track track, Consumer<Track> callback) {
        EditTrackDialogFragment fragment = new EditTrackDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRACK, (Serializable) track);
        fragment.setArguments(args);
        fragment.setOnTrackUpdated(callback);
        return fragment;
    }

    public void setOnTrackUpdated(Consumer<Track> callback) {
        this.onTrackUpdated = callback;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setDimAmount(0.5f); // fondo ligeramente oscurecido
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(STYLE_NORMAL, R.style.Dialog_Centered_TransparentBackground);

        track = (Track) getArguments().getSerializable(ARG_TRACK);
        if (track == null) track = new Track(); // Para soporte de creación

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_track, null);

        EditText etTitle = view.findViewById(R.id.track_title);
        EditText etArtist = view.findViewById(R.id.track_artist);
        EditText etGenre = view.findViewById(R.id.track_genre);

        etTitle.setText(track.getTitle());
        etArtist.setText(track.getArtistName());
        etGenre.setText(track.getGenre());

        return new AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom) // personalizado
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    track.setTitle(etTitle.getText().toString());
                    track.setArtistName(etArtist.getText().toString());
                    track.setGenre(etGenre.getText().toString());
                    if (onTrackUpdated != null) onTrackUpdated.accept(track);
                })
                .setNegativeButton("Cancelar", null)
                .create();
    }

}
