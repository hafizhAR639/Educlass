package com.belajar.myapplication.admin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.belajar.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddTopicActivity extends AppCompatActivity {

    private EditText etTitle, etDesc;
    private TextView btnVisual, btnAudio, btnKinestetik;
    private String selectedStyle = "Visual";
    private String subjectId;
    private FirebaseFirestore db;
    private Uri selectedFileUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedFileUri = result.getData().getData();
                    String fileName = selectedFileUri.getLastPathSegment();
                    Toast.makeText(this, "File terpilih: " + (fileName != null ? fileName : "file"), Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_add_topic);

        subjectId = getIntent().getStringExtra("subject_id");
        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.et_topic_title);
        etDesc = findViewById(R.id.et_topic_desc);
        btnVisual = findViewById(R.id.btn_style_visual);
        btnAudio = findViewById(R.id.btn_style_audio);
        btnKinestetik = findViewById(R.id.btn_style_kinestetik);

        // Header Title
        View header = findViewById(R.id.header_add_topic);
        if (header != null) {
            TextView tvHeader = header.findViewById(R.id.tv_shared_header_title);
            if (tvHeader != null) tvHeader.setText("Tambah Materi Baru");

            View btnBack = header.findViewById(R.id.btn_back);
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        }

        setupStyleSelection();
        findViewById(R.id.btn_save_topic).setOnClickListener(v -> saveTopic());

        // File Picker
        findViewById(R.id.btn_upload_topic).setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void setupStyleSelection() {
        btnVisual.setOnClickListener(v -> selectStyle("Visual", btnVisual));
        btnAudio.setOnClickListener(v -> selectStyle("Audio", btnAudio));
        btnKinestetik.setOnClickListener(v -> selectStyle("Kinestetik", btnKinestetik));
    }

    private void selectStyle(String style, TextView selectedBtn) {
        selectedStyle = style;
        
        // Reset styles for all
        resetButtonStyle(btnVisual);
        resetButtonStyle(btnAudio);
        resetButtonStyle(btnKinestetik);

        // Set active style to selected
        selectedBtn.setBackgroundResource(R.drawable.shared_bg_nav_indicator);
        selectedBtn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        selectedBtn.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetButtonStyle(TextView btn) {
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setTextColor(Color.parseColor("#667085"));
        btn.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void saveTopic() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> topic = new HashMap<>();
        topic.put("judul", title);
        topic.put("desc", desc);
        topic.put("subject_id", subjectId);
        topic.put("gaya_belajar", selectedStyle);
        topic.put("views_count", 0);
        topic.put("order", System.currentTimeMillis()); // simple ordering

        db.collection("topics").add(topic).addOnSuccessListener(documentReference -> {
            // Log Activity
            Map<String, Object> log = new HashMap<>();
            log.put("description", "Admin menambah materi: " + title);
            log.put("type", "add");
            log.put("timestamp", com.google.firebase.Timestamp.now());
            db.collection("admin_activities").add(log);

            Toast.makeText(this, "Materi berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal menambahkan materi", Toast.LENGTH_SHORT).show();
        });
    }
}
