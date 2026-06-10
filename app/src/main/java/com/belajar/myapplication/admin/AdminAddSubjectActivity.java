package com.belajar.myapplication.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aktivitas untuk menambahkan Mata Pelajaran baru.
 * Prinsip KISS: Logika dibuat sederhana, berurutan, dan mudah dipahami.
 */
public class AdminAddSubjectActivity extends AppCompatActivity {

    // UI Elements
    private TextInputEditText etName;
    private AutoCompleteTextView autoCompleteCategory;
    private ImageView ivPreview;
    private View[] colorViews;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    
    // Data Pemilihan
    private String selectedColorHex = "#DBEAFE"; // Default Biru Muda
    private Uri imageUri;

    // Launcher untuk mengambil gambar dari galeri (Cara modern Android)
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivPreview.setImageURI(imageUri);
                    ivPreview.setPadding(0, 0, 0, 0); // Hilangkan padding agar gambar penuh
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_subject);

        // 1. Inisialisasi Firebase & UI
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        
        etName = findViewById(R.id.et_subject_name);
        autoCompleteCategory = findViewById(R.id.auto_complete_category);
        ivPreview = findViewById(R.id.iv_preview_icon);

        // 2. Setup Komponen Input
        setupCategoryDropdown();
        setupColorSelection();

        // 3. Setup Klik Listener
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> validateAndSave());
        
        // Area upload gambar
        findViewById(R.id.layout_upload_icon).setOnClickListener(v -> openGallery());
        findViewById(R.id.btn_select_file).setOnClickListener(v -> openGallery());
    }

    /**
     * Membuka galeri hp untuk memilih file gambar
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Menyiapkan menu dropdown untuk kategori (IPA/IPS)
     */
    private void setupCategoryDropdown() {
        String[] categories = {"IPA", "IPS"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(adapter);
        autoCompleteCategory.setText(categories[0], false); // Default ke pilihan pertama
    }

    /**
     * Menyiapkan pilihan warna tema (Grid Klik)
     */
    private void setupColorSelection() {
        colorViews = new View[]{
                findViewById(R.id.color_1), findViewById(R.id.color_2), findViewById(R.id.color_3),
                findViewById(R.id.color_4), findViewById(R.id.color_5), findViewById(R.id.color_6)
        };

        // Daftar hex sesuai desain (nuansa biru & netral)
        String[] hexCodes = {"#DBEAFE", "#CEFAFF", "#E0EAFF", "#F2F4F7", "#E4E7EC", "#D1FADF"};

        for (int i = 0; i < colorViews.length; i++) {
            final int index = i;
            colorViews[i].setOnClickListener(v -> {
                selectedColorHex = hexCodes[index];
                updateColorSelectionUI(index);
            });
        }
        updateColorSelectionUI(0); // Set pilihan awal
    }

    /**
     * Memperbarui tampilan visual warna yang sedang dipilih
     */
    private void updateColorSelectionUI(int selectedIndex) {
        for (int i = 0; i < colorViews.length; i++) {
            // Berikan efek transparansi pada yang tidak dipilih agar yang dipilih menonjol
            colorViews[i].setAlpha(i == selectedIndex ? 1.0f : 0.3f);
        }
    }

    /**
     * Validasi input sebelum proses upload dan simpan
     */
    private void validateAndSave() {
        String name = (etName.getText() != null) ? etName.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nama tidak boleh kosong");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Silakan pilih ikon terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndSave(name);
    }

    /**
     * Mengunggah gambar ke Firebase Storage dengan kompresi WebP agar hemat kuota.
     */
    private void uploadImageAndSave(String name) {
        try {
            Toast.makeText(this, "Sedang mengompres & mengunggah...", Toast.LENGTH_SHORT).show();

            // 1. Ambil Stream dari Uri
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 2. Kompres ke WebP (Kualitas 75%) agar ukuran file sangat kecil tapi tetap tajam
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75, baos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 75, baos);
            }
            byte[] data = baos.toByteArray();

            // 3. Upload data byte array ke Firebase
            String fileName = UUID.randomUUID().toString() + ".webp";
            StorageReference ref = storage.getReference().child("subject_icons/" + fileName);

            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        fetchNextOrderAndSave(name, uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal Upload: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mendapatkan nomor urut (order) otomatis (Urutan terakhir + 1)
     */
    private void fetchNextOrderAndSave(String name, String iconUrl) {
        db.collection("subjects")
                .orderBy("order", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long nextOrder = 1;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Long lastOrder = queryDocumentSnapshots.getDocuments().get(0).getLong("order");
                        nextOrder = (lastOrder != null ? lastOrder : 0) + 1;
                    }
                    saveDataToFirestore(name, nextOrder, iconUrl);
                })
                .addOnFailureListener(e -> saveDataToFirestore(name, 1, iconUrl));
    }

    /**
     * Tahap akhir: Menyimpan semua data objek Mata Pelajaran ke koleksi Firestore
     */
    private void saveDataToFirestore(String name, long order, String iconUrl) {
        String category = autoCompleteCategory.getText().toString().toLowerCase();

        Map<String, Object> subject = new HashMap<>();
        subject.put("nama", name);
        subject.put("jurusan", category);
        subject.put("color_hex", selectedColorHex);
        subject.put("icon_name", iconUrl); // URL dari Firebase Storage
        subject.put("order", order);
        subject.put("total_moduls", 0);
        subject.put("access_count", 0);
        subject.put("created_at", com.google.firebase.Timestamp.now());

        db.collection("subjects")
                .add(subject)
                .addOnSuccessListener(documentReference -> {
                    // Log Activity
                    Map<String, Object> log = new HashMap<>();
                    log.put("description", "Admin menambah mapel: " + name);
                    log.put("type", "add");
                    log.put("timestamp", com.google.firebase.Timestamp.now());
                    db.collection("admin_activities").add(log);

                    showSuccessDialog();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal Simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Menampilkan Pop-up Berhasil sesuai desain (KISS)
     */
    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.shared_dialog_success, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Membuat background dialog transparan agar sudut rounded terlihat
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnClose = dialogView.findViewById(R.id.btn_dialog_close);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Tutup activity setelah dialog ditutup
        });

        dialog.show();
    }
}
