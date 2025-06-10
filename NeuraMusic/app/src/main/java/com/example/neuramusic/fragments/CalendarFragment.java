package com.example.neuramusic.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.CalendarItemAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.CalendarItem;
import com.example.neuramusic.model.CalendarNote;

import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerView;
    private LinearLayout notesContainer;
    private ImageButton btnAdd, btnAddEvent, btnAddNote;

    private CalendarItemAdapter adapter;
    private List<CalendarItem> itemList = new ArrayList<>();

    private String selectedDate = "";
    private final String apiKey = RetrofitClient.API_KEY;
    private String authToken;
    private String userId;

    private boolean isFabExpanded = false;

    public CalendarFragment(String authToken, String userId) {
        this.authToken = authToken;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        recyclerView = view.findViewById(R.id.recycler_day_items);
        notesContainer = view.findViewById(R.id.notes_container);
        btnAdd = view.findViewById(R.id.btn_add_calendar_item);
        btnAddEvent = view.findViewById(R.id.btn_add_event);
        btnAddNote = view.findViewById(R.id.btn_add_note);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalendarItemAdapter(getContext(), itemList, this::actualizarEstadoTarea);
        recyclerView.setAdapter(adapter);

        long dateMillis = calendarView.getDate();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(dateMillis));
        tvSelectedDate.setText("Eventos de " + selectedDate);

        cargarItemsDelDia();
        cargarNotasDelDia();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("Eventos de " + selectedDate);
            cargarItemsDelDia();
            cargarNotasDelDia();
        });

        btnAdd.setOnClickListener(v -> toggleFabMenu());

        btnAddEvent.setOnClickListener(v -> {
            toggleFabMenu();
            AddCalendarItemDialogFragment dialog = new AddCalendarItemDialogFragment(apiKey, authToken, userId, this::cargarItemsDelDia);
            dialog.show(getParentFragmentManager(), "AddCalendarItemDialog");
        });

        btnAddNote.setOnClickListener(v -> {
            toggleFabMenu();
            mostrarDialogoNota();
        });
    }

    private void toggleFabMenu() {
        isFabExpanded = !isFabExpanded;

        if (isFabExpanded) {
            btnAddEvent.setVisibility(View.VISIBLE);
            btnAddNote.setVisibility(View.VISIBLE);

            ObjectAnimator.ofFloat(btnAddEvent, "translationX", -150f).setDuration(200).start();
            ObjectAnimator.ofFloat(btnAddNote, "translationX", -300f).setDuration(200).start();
        } else {
            ObjectAnimator.ofFloat(btnAddEvent, "translationX", 0f).setDuration(200).start();
            ObjectAnimator.ofFloat(btnAddNote, "translationX", 0f).setDuration(200).start();

            btnAddEvent.postDelayed(() -> btnAddEvent.setVisibility(View.GONE), 200);
            btnAddNote.postDelayed(() -> btnAddNote.setVisibility(View.GONE), 200);
        }
    }

    private void mostrarDialogoNota() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_text_block, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);

        new AlertDialog.Builder(getContext())
                .setTitle("Añadir nota")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (!title.isEmpty() && !content.isEmpty()) {
                        guardarNotaEnSupabase(title, content);
                    } else {
                        Toast.makeText(getContext(), "Campos vacíos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarNotaEnSupabase(String title, String content) {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);

        CalendarNote note = new CalendarNote();
        note.setUserId(userId);
        note.setNoteDate(selectedDate);
        note.setTitle(title);
        note.setContent(content);

        Call<List<CalendarNote>> call = api.createCalendarNote(note, apiKey, "Bearer " + authToken);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<CalendarNote>> call, Response<List<CalendarNote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    añadirNotaVisual(response.body().get(0));
                } else {
                    Toast.makeText(getContext(), "Error al guardar nota", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CalendarNote>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void añadirNotaVisual(CalendarNote note) {
        View card = LayoutInflater.from(getContext()).inflate(R.layout.item_note, notesContainer, false);
        TextView tvTitle = card.findViewById(R.id.note_title);
        TextView tvContent = card.findViewById(R.id.note_content);
        ImageButton btnDelete = card.findViewById(R.id.btn_delete_note);

        tvTitle.setText(note.getTitle());
        tvContent.setText(note.getContent());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Eliminar nota")
                    .setMessage("¿Estás seguro de que quieres eliminar esta nota?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarNotaDeSupabase(note.getId(), card))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        notesContainer.addView(card);
    }

    private void eliminarNotaDeSupabase(String noteId, View cardView) {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        String filter = "eq." + noteId;

        Call<ResponseBody> call = api.deleteCalendarNote(filter, apiKey, "Bearer " + authToken);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    notesContainer.removeView(cardView);
                    Toast.makeText(getContext(), "Nota eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al eliminar nota", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarItemsDelDia() {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Map<String, String> query = new HashMap<>();
        query.put("user_id", "eq." + userId);
        query.put("event_date", "eq." + selectedDate);

        Call<List<CalendarItem>> call = api.getCalendarItems(query, apiKey, "Bearer " + authToken);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<CalendarItem>> call, Response<List<CalendarItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CalendarItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarNotasDelDia() {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Map<String, String> query = new HashMap<>();
        query.put("user_id", "eq." + userId);
        query.put("note_date", "eq." + selectedDate);

        Call<List<CalendarNote>> call = api.getCalendarNotes(query, apiKey, "Bearer " + authToken);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<CalendarNote>> call, Response<List<CalendarNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notesContainer.removeAllViews();
                    for (CalendarNote note : response.body()) {
                        añadirNotaVisual(note);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar notas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CalendarNote>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEstadoTarea(CalendarItem item, boolean isChecked) {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_completed", isChecked);

        Call<ResponseBody> call = api.updateCalendarItem(item.getId(), apiKey, "Bearer " + authToken, updates);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
