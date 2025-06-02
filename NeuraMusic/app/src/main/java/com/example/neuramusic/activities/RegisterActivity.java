// 👇 CAMBIOS REALIZADOS:
// - Foto de perfil ya no es obligatoria
// - Manejamos error "al registrarse" con más detalle
// - Si no hay imagen, no se sube nada y se manda profile_image_url vacío

package com.example.neuramusic.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.AuthRequest;
import com.example.neuramusic.model.SupabaseSignupResponse;
import com.example.neuramusic.model.UserRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etEmail, etEmailConfirm, etPassword, etUsername, etBio, etDOB;
    private EditText etInstagram, etSoundcloud, etSpotify, etYoutube;
    private Button btnRegister;
    private ImageButton btnTogglePassword;
    private ImageView ivProfileImage;

    private boolean isPasswordVisible = false;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri = null;

    private SupabaseService supabaseService;

    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4b3hoZG1paHlkam90c2dncGNvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNzk3MjYsImV4cCI6MjA2Mzk1NTcyNn0.Cg4fm9x0NqlkSxtMTvMMFZJ-MNDoN1-u4ymKr7NdzR0";
    private static final String SUPABASE_BUCKET_URL = "https://tu-proyecto.supabase.co/storage/v1/object/public/user-images/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);

        etEmail = findViewById(R.id.etEmail);
        etEmailConfirm = findViewById(R.id.etEmailConfirm);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        etDOB = findViewById(R.id.etDOB);
        etInstagram = findViewById(R.id.etInstagram);
        etSoundcloud = findViewById(R.id.etSoundcloud);
        etSpotify = findViewById(R.id.etSpotify);
        etYoutube = findViewById(R.id.etYoutube);
        btnRegister = findViewById(R.id.btnRegister);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        etDOB.setOnClickListener(v -> showDatePicker());
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnRegister.setOnClickListener(v -> attemptRegister());
        ivProfileImage.setOnClickListener(v -> openImagePicker());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDOB.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Establecer fecha máxima (18 años atrás)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfileImage.setImageURI(selectedImageUri);
        }
    }

    private void attemptRegister() {
        String email = etEmail.getText().toString().trim();
        String emailConfirm = etEmailConfirm.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            return;
        }

        if (!email.equals(emailConfirm)) {
            etEmailConfirm.setError("Los emails no coinciden");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Contraseña demasiado corta");
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("El nombre de usuario es obligatorio");
            return;
        }

        if (username.length() < 3) {
            etUsername.setError("El nombre de usuario debe tener al menos 3 caracteres");
            return;
        }

        AuthRequest authRequest = new AuthRequest(email, password);

        supabaseService.signUp(authRequest, SUPABASE_API_KEY).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        SupabaseSignupResponse signupResponse = SupabaseSignupResponse.fromJson(json);
                        String uid = signupResponse.user.id;
                        String accessToken = signupResponse.access_token;

                        if (selectedImageUri != null) {
                            uploadImage(uid, email, accessToken);
                        } else {
                            insertUserData(uid, email, accessToken, ""); // ✅ sin imagen
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Error procesando respuesta");
                    }
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        showToast(obj.optString("msg", "Error al registrarse"));
                    } catch (Exception e) {
                        showToast("Error inesperado");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToast("Fallo de red al registrar");
            }
        });
    }

    private void uploadImage(String uid, String email, String accessToken) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            
            String filename = "profile_" + uid + "_" + System.currentTimeMillis() + ".jpg";
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, requestFile);
            
            supabaseService.uploadImage(filename, body, SUPABASE_API_KEY, "Bearer " + accessToken)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String json = response.body().string();
                                // Construir la URL pública de la imagen
                                String publicUrl = "https://lxoxhdmihydjotsggpco.supabase.co/storage/v1/object/public/user-images/" + filename;
                                insertUserData(uid, email, accessToken, publicUrl);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar URL de imagen: " + e.getMessage());
                                showToast("Error al procesar imagen");
                            }
                        } else {
                            Log.e(TAG, "Error al subir imagen: " + response.code());
                            showToast("Error al subir imagen");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Error de red al subir imagen: " + t.getMessage());
                        showToast("Error de conexión");
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error al preparar imagen: " + e.getMessage());
            showToast("Error al procesar imagen");
        }
    }

    private void insertUserData(String uid, String email, String accessToken, String imageUrl) {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String instagram = etInstagram.getText().toString().trim();
        String soundcloud = etSoundcloud.getText().toString().trim();
        String spotify = etSpotify.getText().toString().trim();
        String youtube = etYoutube.getText().toString().trim();

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", uid);
        userData.put("email", email);
        userData.put("username", username);
        userData.put("role", "artista");
        userData.put("bio", bio);
        userData.put("birth_date", dob);
        userData.put("instagram_url", instagram);
        userData.put("soundcloud_url", soundcloud);
        userData.put("spotify_url", spotify);
        userData.put("youtube_url", youtube);
        userData.put("is_blocked", false);
        userData.put("is_approved", true);
        userData.put("profile_image_url", imageUrl);
        userData.put("professions", new ArrayList<String>()); // Inicializar lista vacía de profesiones

        supabaseService.updateUser(uid, SUPABASE_API_KEY, "Bearer " + accessToken, userData)
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        showToast("Usuario registrado correctamente");
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        showToast("Error al insertar datos del usuario");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showToast("Error de conexión al insertar datos");
                }
            });
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(proj[0]);
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
