package com.example.neuramusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.neuramusic.R;

public class AddNoteDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final var view = inflater.inflate(R.layout.dialog_add_text_block, null);
        final EditText etTitle = view.findViewById(R.id.etTitle);
        final EditText etContent = view.findViewById(R.id.etContent);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Nueva nota")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (!title.isEmpty() && !content.isEmpty()) {
                        Bundle result = new Bundle();
                        result.putString("note_title", title);
                        result.putString("note_content", content);
                        getParentFragmentManager().setFragmentResult("note_added", result);
                    } else {
                        Toast.makeText(getContext(), "Título y contenido requeridos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
    }
}
