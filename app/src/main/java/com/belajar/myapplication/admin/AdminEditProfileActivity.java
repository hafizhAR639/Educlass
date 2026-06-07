package com.belajar.myapplication.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class AdminEditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private TextView tvDisplayName, tvDisplayEmail;
    private ImageView ivProfilePic;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfilePic.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        etName = findViewById(R.id.et_edit_name);
        etEmail = findViewById(R.id.et_edit_email);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvDisplayEmail = findViewById(R.id.tv_display_email);
        ivProfilePic = findViewById(R.id.iv_edit_profile_pic);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
        
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("nama");
                    String email = documentSnapshot.getString("email");
                    String photoUrl = documentSnapshot.getString("photoUrl");

                    etName.setText(name);
                    etEmail.setText(email);
                    tvDisplayName.setText(name);
                    tvDisplayEmail.setText(email);

                    if (photoUrl != null && !photoUrl.isEmpty() && !isFinishing() && !isDestroyed()) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.shared_profile_pic).into(ivProfilePic);
                    }
                }
            });
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser != null) {
            if (imageUri != null) {
                uploadImageAndSaveProfile(name, email);
            } else {
                updateProfileInFirestore(name, email, null);
            }
        }
    }

    private void uploadImageAndSaveProfile(String name, String email) {
        StorageReference fileRef = storage.getReference().child("profile_pics/" + currentUser.getUid() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileInFirestore(name, email, uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal mengunggah gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileInFirestore(String name, String email, String photoUrl) {
        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("nama", name);
        updates.put("email", email);
        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        db.collection("users").document(uid).update(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
            tvDisplayName.setText(name);
            tvDisplayEmail.setText(email);
            if (photoUrl != null && !isFinishing() && !isDestroyed()) {
                Glide.with(this).load(photoUrl).placeholder(R.drawable.shared_profile_pic).into(ivProfilePic);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
