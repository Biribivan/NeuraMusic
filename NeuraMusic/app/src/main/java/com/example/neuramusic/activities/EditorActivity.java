package com.example.neuramusic.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.UserResponse;
import com.example.neuramusic.utils.SocialMediaValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class EditorActivity extends AppCompatActivity {

    private static final String TAG = "EditorActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private ImageView ivProfile;
    private TextInputEditText etUsername, etFullName, etArtTitle, etBio;
    private TextInputEditText etInstagram, etYoutube, etSpotify, etSoundcloud;
    private TextInputEditText etBirthDate, etCustomGender;
    private AutoCompleteTextView spinnerGender;
    private TextInputLayout tilCustomGender;
    private Button btnSave;
    private ImageButton btnBack, btnChangePhoto;
    private ChipGroup chipGroupProfessions;
    private List<String> selectedProfessions = new ArrayList<>();
    
    private SupabaseService supabaseService;
    private String userId;
    private String accessToken;
    private Uri selectedImageUri;
    private Calendar calendar;
    
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initViews();
        setupGenderSpinner();
        setupDatePicker();
        setupChips();

        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        accessToken = prefs.getString("access_token", null);

        if (userId == null || accessToken == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);
        loadUserData();

        // Registrar el launcher para el Photo Picker
        pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(ivProfile);
            }
        });

        // Cargar datos existentes si los hay
        if (getIntent().hasExtra("user_data")) {
            UserResponse userData = (UserResponse) getIntent().getSerializableExtra("user_data");
            populateFields(userData);
        }
    }

    private void initViews() {
        ivProfile = findViewById(R.id.ivProfile);
        etUsername = findViewById(R.id.etUsername);
        etFullName = findViewById(R.id.etFullName);
        etArtTitle = findViewById(R.id.etArtTitle);
        etBio = findViewById(R.id.etBio);
        etInstagram = findViewById(R.id.etInstagram);
        etYoutube = findViewById(R.id.etYoutube);
        etSpotify = findViewById(R.id.etSpotify);
        etSoundcloud = findViewById(R.id.etSoundcloud);
        spinnerGender = findViewById(R.id.spinnerGender);
        etCustomGender = findViewById(R.id.etCustomGender);
        etBirthDate = findViewById(R.id.etBirthDate);
        tilCustomGender = findViewById(R.id.tilCustomGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        chipGroupProfessions = findViewById(R.id.chipGroupProfessions);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void setupGenderSpinner() {
        String[] genders = {"Hombre", "Mujer", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genders
        );
        spinnerGender.setAdapter(adapter);
        
        spinnerGender.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGender = parent.getItemAtPosition(position).toString();
            tilCustomGender.setVisibility(
                selectedGender.equals("Otro") ? View.VISIBLE : View.GONE
            );
        });
    }

    private void setupDatePicker() {
        calendar = Calendar.getInstance();
        
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateLabel();
        };

        etBirthDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            
            // Establecer fecha máxima (18 años atrás)
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -18);
            dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            
            dialog.show();
        });
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etBirthDate.setText(sdf.format(calendar.getTime()));
    }

    private void openImagePicker() {
        // Usar el nuevo Photo Picker
        pickImage.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    private void loadUserData() {
        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + userId);

        supabaseService.getUserById(query, RetrofitClient.API_KEY, "Bearer " + accessToken)
            .enqueue(new Callback<ResponseBody>() {
                    @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            Type listType = new TypeToken<List<UserResponse>>() {}.getType();
                            List<UserResponse> users = new Gson().fromJson(json, listType);

                            if (!users.isEmpty()) {
                                UserResponse user = users.get(0);
                                populateFields(user);
                            }
                        } catch (Exception e) {
                            Toast.makeText(EditorActivity.this, 
                                "Error al cargar datos", Toast.LENGTH_SHORT).show();
                        }
                        }
                    }

                    @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(EditorActivity.this, 
                        "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateFields(UserResponse user) {
        if (user == null) {
            Log.e(TAG, "Usuario nulo, no se pueden poblar los campos");
            return;
        }
        
        Log.d(TAG, "Poblando campos con datos del usuario: " + user.toString());
        
        etUsername.setText(user.username);
        etFullName.setText(user.fullName);
        etArtTitle.setText(user.artTitle);
        etBio.setText(user.bio);
        
        // Restaurar profesiones seleccionadas
        if (user.professions != null && !user.professions.isEmpty()) {
            Log.d(TAG, "Restaurando profesiones: " + user.professions.toString());
            selectedProfessions = new ArrayList<>(user.professions);
            
            for (int i = 0; i < chipGroupProfessions.getChildCount(); i++) {
                View view = chipGroupProfessions.getChildAt(i);
                if (view instanceof Chip) {
                    Chip chip = (Chip) view;
                    boolean shouldBeChecked = selectedProfessions.contains(chip.getText().toString());
                    chip.setChecked(shouldBeChecked);
                    Log.d(TAG, "Chip " + chip.getText() + " establecido como: " + shouldBeChecked);
                }
            }
        } else {
            Log.d(TAG, "No hay profesiones para restaurar");
        }
        
        // Cargar imagen de perfil
        if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            Log.d(TAG, "Cargando imagen de perfil: " + user.profileImageUrl);
            Glide.with(this)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Error al cargar imagen de perfil: " + (e != null ? e.getMessage() : "desconocido"));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Imagen de perfil cargada exitosamente");
                        return false;
                    }
                })
                .into(ivProfile);
        } else {
            Log.d(TAG, "No hay URL de imagen de perfil");
            ivProfile.setImageResource(R.drawable.ic_user);
        }
        
        // Restaurar URLs de redes sociales
        etInstagram.setText(user.instagram);
        etYoutube.setText(user.youtube);
        etSpotify.setText(user.spotify);
        etSoundcloud.setText(user.soundcloud);
        
        // Restaurar género
        if (user.gender != null) {
            if (user.gender.equals("Hombre") || user.gender.equals("Mujer")) {
                spinnerGender.setText(user.gender, false);
                tilCustomGender.setVisibility(View.GONE);
            } else {
                spinnerGender.setText("Otro", false);
                tilCustomGender.setVisibility(View.VISIBLE);
                etCustomGender.setText(user.gender);
            }
        }
        
        // Restaurar fecha de nacimiento
        if (user.birthDate != null && !user.birthDate.isEmpty()) {
            etBirthDate.setText(user.birthDate);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(user.birthDate));
            } catch (Exception e) {
                Log.e(TAG, "Error al parsear fecha de nacimiento: " + e.getMessage());
            }
        }
    }

    private void setupChips() {
        // Limpiar selecciones previas
        selectedProfessions.clear();
        chipGroupProfessions.removeAllViews();
        
        String[] professions = getResources().getStringArray(R.array.profesiones_artisticas);
        for (String profession : professions) {
            Chip chip = new Chip(this);
            chip.setText(profession);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setChecked(false);
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedProfessions.contains(profession)) {
                        selectedProfessions.add(profession);
                        Log.d(TAG, "Profesión agregada: " + profession);
                    }
                } else {
                    selectedProfessions.remove(profession);
                    Log.d(TAG, "Profesión removida: " + profession);
                }
                Log.d(TAG, "Profesiones seleccionadas: " + selectedProfessions.toString());
            });
            
            chipGroupProfessions.addView(chip);
        }
    }

    private void validateAndSave() {
        // Obtener valores de los campos
        final String username = etUsername.getText().toString().trim();
        final String fullName = etFullName.getText().toString().trim();
        final String artTitle = etArtTitle.getText().toString().trim();
        final String bio = etBio.getText().toString().trim();
        final String instagram = etInstagram.getText().toString().trim();
        final String youtube = etYoutube.getText().toString().trim();
        final String spotify = etSpotify.getText().toString().trim();
        final String soundcloud = etSoundcloud.getText().toString().trim();
        
        // Validaciones obligatorias
        if (username.isEmpty()) {
            etUsername.setError("El nombre de usuario es obligatorio");
            return;
        }
        
        if (username.length() < 3) {
            etUsername.setError("El nombre de usuario debe tener al menos 3 caracteres");
            return;
        }
        
        if (fullName.isEmpty()) {
            etFullName.setError("El nombre completo es obligatorio");
            return;
        }

        if (artTitle.isEmpty()) {
            etArtTitle.setError("El título artístico es obligatorio");
            return;
        }

        // Validar enlaces de redes sociales
        if (!instagram.isEmpty() && !SocialMediaValidator.isValidInstagramUrl(instagram)) {
            etInstagram.setError("URL de Instagram no válida");
            return;
        }

        if (!youtube.isEmpty() && !SocialMediaValidator.isValidYoutubeUrl(youtube)) {
            etYoutube.setError("URL de YouTube no válida");
            return;
        }

        if (!spotify.isEmpty() && !SocialMediaValidator.isValidSpotifyUrl(spotify)) {
            etSpotify.setError("URL de Spotify no válida");
            return;
        }

        if (!soundcloud.isEmpty() && !SocialMediaValidator.isValidSoundcloudUrl(soundcloud)) {
            etSoundcloud.setError("URL de SoundCloud no válida");
            return;
        }

        // Preparar datos para actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("full_name", fullName);
        updates.put("art_title", artTitle);
        
        // Solo agregar campos no vacíos
        if (!bio.isEmpty()) updates.put("bio", bio);
        if (!instagram.isEmpty()) updates.put("instagram_url", instagram);
        if (!youtube.isEmpty()) updates.put("youtube_url", youtube);
        if (!spotify.isEmpty()) updates.put("spotify_url", spotify);
        if (!soundcloud.isEmpty()) updates.put("soundcloud_url", soundcloud);
        
        // Manejar género
        String gender = spinnerGender.getText().toString();
        if (!gender.isEmpty()) {
            if (gender.equals("Otro")) {
                String customGender = etCustomGender.getText().toString().trim();
                if (!customGender.isEmpty()) {
                    updates.put("gender", customGender);
                }
            } else {
                updates.put("gender", gender);
            }
        }
        
        // Manejar fecha de nacimiento
        String birthDate = etBirthDate.getText().toString().trim();
        if (!birthDate.isEmpty()) {
            updates.put("birth_date", birthDate);
        }
        
        // Manejar profesiones
        if (!selectedProfessions.isEmpty()) {
            updates.put("professions", selectedProfessions);
        }
        
        Log.d(TAG, "Datos a actualizar: " + new Gson().toJson(updates));
        
        // Si hay una nueva imagen seleccionada, subirla primero
        if (selectedImageUri != null) {
            uploadImage(updates);
        } else {
            saveProfile(updates);
        }
    }

    private void uploadImage(final Map<String, Object> updates) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            
            String filename = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, requestFile);
            
            Log.d(TAG, "Intentando subir imagen a Supabase...");
            
            supabaseService.uploadImage(filename, body, RetrofitClient.API_KEY, "Bearer " + accessToken)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String json = response.body().string();
                                Log.d(TAG, "Respuesta de subida de imagen: " + json);
                                Map<String, Object> responseMap = new Gson().fromJson(json, Map.class);
                                // Construir la URL pública de la imagen
                                String publicUrl = "https://lxoxhdmihydjotsggpco.supabase.co/storage/v1/object/public/user-images/" + filename;
                                updates.put("profile_image_url", publicUrl);
                                saveProfile(updates);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar URL de imagen: " + e.getMessage());
                                Log.e(TAG, "Stacktrace: ", e);
                                Toast.makeText(EditorActivity.this, 
                                    "Error al procesar imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? 
                                    response.errorBody().string() : "Error desconocido";
                                Log.e(TAG, "Error al subir imagen. Código: " + response.code() + 
                                    ", Error: " + errorBody);
                                Toast.makeText(EditorActivity.this, 
                                    "Error al subir imagen (" + response.code() + "): " + errorBody, 
                                    Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.e(TAG, "Error al leer respuesta de error: " + e.getMessage());
                                Toast.makeText(EditorActivity.this, 
                                    "Error al subir imagen. Código: " + response.code(), 
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error de red al subir imagen: " + t.getMessage());
                        Log.e(TAG, "Stacktrace: ", t);
                        Toast.makeText(EditorActivity.this, 
                            "Error de conexión al subir imagen: " + t.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error al preparar imagen: " + e.getMessage());
            Log.e(TAG, "Stacktrace: ", e);
            Toast.makeText(this, 
                "Error al procesar imagen: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    private void saveProfile(Map<String, Object> updates) {
        Log.d(TAG, "Guardando perfil con datos: " + updates.toString());
        
        // Agregar timestamp de actualización
        updates.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .format(new Date()));
        
        String filterId = "eq." + userId;
        supabaseService.updateUser(filterId, RetrofitClient.API_KEY, "Bearer " + accessToken, updates)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Perfil actualizado exitosamente");
                        Toast.makeText(EditorActivity.this, 
                            "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Error desconocido";
                            Log.e(TAG, "Error al actualizar perfil. Código: " + response.code() + 
                                ", Error: " + errorBody);
                            Log.e(TAG, "URL de la petición: " + call.request().url());
                            Log.e(TAG, "Headers de la petición: " + call.request().headers());
                            Log.e(TAG, "Cuerpo de la petición: " + new Gson().toJson(updates));
                            
                            // Intentar parsear el mensaje de error
                            try {
                                JSONObject errorJson = new JSONObject(errorBody);
                                String message = errorJson.optString("message", "Error al guardar cambios");
                                String code = errorJson.optString("code", "");
                                String details = errorJson.optString("details", "");
                                
                                String errorMessage = "Error: " + message;
                                if (!code.isEmpty()) errorMessage += " (Código: " + code + ")";
                                if (!details.isEmpty()) errorMessage += "\nDetalles: " + details;
                                
                                Toast.makeText(EditorActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(EditorActivity.this, 
                                    "Error al guardar cambios (" + response.code() + "): " + errorBody, 
                                    Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al leer respuesta de error: " + e.getMessage());
                            Toast.makeText(EditorActivity.this, 
                                "Error al guardar cambios. Código: " + response.code(), 
                                Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Error de red al actualizar perfil: " + t.getMessage());
                    Log.e(TAG, "Stacktrace: ", t);
                    Toast.makeText(EditorActivity.this, 
                        "Error de conexión: " + t.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    }
                });
    }
}
