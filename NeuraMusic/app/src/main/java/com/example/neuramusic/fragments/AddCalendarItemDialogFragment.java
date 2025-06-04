package com.example.neuramusic.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.CalendarItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCalendarItemDialogFragment extends DialogFragment {

    private EditText etTitle, etDescription, etIcon, etLink;
    private Spinner spinnerType, spinnerColor;
    private TextView tvDate, tvTime;
    private CheckBox checkboxImportant, checkboxRepeat;
    private Button btnSave;

    private String selectedDate = "";
    private String selectedTime = "";

    private final String apiKey;
    private final String authToken;
    private final String userId;
    private final Runnable onItemSavedCallback;

    public AddCalendarItemDialogFragment(String apiKey, String authToken, String userId, Runnable onItemSavedCallback) {
        this.apiKey = apiKey;
        this.authToken = authToken;
        this.userId = userId;
        this.onItemSavedCallback = onItemSavedCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_calendar_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etIcon = view.findViewById(R.id.et_icon);
        etLink = view.findViewById(R.id.et_link);
        spinnerType = view.findViewById(R.id.spinner_type);
        spinnerColor = view.findViewById(R.id.spinner_label_color);
        tvDate = view.findViewById(R.id.tv_date);
        tvTime = view.findViewById(R.id.tv_time);
        checkboxImportant = view.findViewById(R.id.checkbox_important);
        checkboxRepeat = view.findViewById(R.id.checkbox_repeat);
        btnSave = view.findViewById(R.id.btn_save);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"evento", "tarea", "ensayo", "reunión", "nota"}
        );
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"azul", "rojo", "verde", "amarillo", "morado"}
        );
        spinnerColor.setAdapter(colorAdapter);

        tvDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                selectedDate = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
                tvDate.setText(selectedDate);
            }, 2025, 5, 4);
            picker.show();
        });

        tvTime.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(requireContext(), (view12, hour, minute) -> {
                selectedTime = String.format("%02d:%02d", hour, minute);
                tvTime.setText(selectedTime);
            }, 18, 30, true);
            picker.show();
        });

        btnSave.setOnClickListener(v -> guardarCalendarItem());
    }

    private void guardarCalendarItem() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String icon = etIcon.getText().toString().trim();
        String link = etLink.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        boolean isImportant = checkboxImportant.isChecked();
        boolean isRepeat = checkboxRepeat.isChecked();

        if (title.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(getContext(), "El título y la fecha son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarItem item = new CalendarItem();
        item.setUserId(userId);
        item.setTitle(title);
        item.setDescription(description);
        item.setType(type);
        item.setLabelColor(color);
        item.setIcon(icon);
        item.setLink(link);
        item.setIsImportant(isImportant);
        item.setRepeat(isRepeat ? "semanal" : "nunca");
        item.setIsCompleted(false);
        item.setEventDate(selectedDate);
        item.setEventTime(selectedTime.isEmpty() ? null : selectedTime);

        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Call<List<CalendarItem>> call = api.createCalendarItem(item, apiKey, "Bearer " + authToken);


        call.enqueue(new Callback<List<CalendarItem>>() {
            @Override
            public void onResponse(Call<List<CalendarItem>> call, Response<List<CalendarItem>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show();

                    if (onItemSavedCallback != null) {
                        onItemSavedCallback.run();
                    }

                    Bundle result = new Bundle();
                    result.putBoolean("calendar_item_added", true);
                    getParentFragmentManager().setFragmentResult("calendar_item_result", result);

                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al guardar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CalendarItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
