package com.belajar.myapplication.user;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

public class UserEditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private TextView tvPreviewName, tvPreviewEmail;
    private ImageView ivProfile;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private String currentPhotoUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfile.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        etName = findViewById(R.id.et_edit_name);
        etEmail = findViewById(R.id.et_edit_email);
        tvPreviewName = findViewById(R.id.tv_edit_name);
        tvPreviewEmail = findViewById(R.id.tv_edit_email);
        ivProfile = findViewById(R.id.iv_edit_profile);
        
        // Header
        TextView tvTitle = findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Edit Profile");
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadData();

        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvPreviewName != null) tvPreviewName.setText(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        if (user != null) {
            etEmail.setText(user.getEmail());
            if (tvPreviewEmail != null) tvPreviewEmail.setText(user.getEmail());
            
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("nama");
                    etName.setText(name);
                    if (tvPreviewName != null) tvPreviewName.setText(name);
                    
                    currentPhotoUrl = doc.getString("photoUrl");
                    if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
                        Glide.with(this).load(currentPhotoUrl).placeholder(R.drawable.user_ic_profile).into(ivProfile);
                    }
                }
            });
        }
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            etName.setError("Nama tidak boleh kosong");
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSave(newName);
        } else {
            updateFirestore(newName, currentPhotoUrl);
        }
    }

    private void uploadImageAndSave(String name) {
        Toast.makeText(this, "Mengunggah foto...", Toast.LENGTH_SHORT).show();
        StorageReference ref = storage.getReference().child("profile_pics/" + user.getUid() + ".jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateFirestore(name, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengunggah foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateFirestore(name, currentPhotoUrl);
                });
    }

    private void updateFirestore(String name, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nama", name);
        if (photoUrl != null) updates.put("photoUrl", photoUrl);

        db.collection("users").document(user.getUid()).update(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show());
    }
}
