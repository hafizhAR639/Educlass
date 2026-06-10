package com.belajar.myapplication.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminAddTopicActivity extends AppCompatActivity {

    private EditText etTitle, etDesc;
    private ChipGroup chipGroup;
    private View layoutVisual, layoutAudio, layoutKin;
    private EditText etVisualExp, etAudioExp, etKinExp;
    private EditText etVisualYoutube, etAudioYoutube, etKinYoutube;
    private TextView tvVisualFile, tvAudioFile, tvKinFile;
    
    private Uri visualUri, audioUri, kinUri;
    private String subjectId;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private android.app.ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout_topic_form);

        subjectId = getIntent().getStringExtra("subject_id");
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_topic_title);
        etDesc = findViewById(R.id.et_topic_desc);
        chipGroup = findViewById(R.id.chip_group_learning_styles);
        
        layoutVisual = findViewById(R.id.layout_visual_content);
        layoutAudio = findViewById(R.id.layout_audio_content);
        layoutKin = findViewById(R.id.layout_kin_content);
        
        etVisualExp = findViewById(R.id.et_visual_explanation);
        etAudioExp = findViewById(R.id.et_audio_explanation);
        etKinExp = findViewById(R.id.et_kin_explanation);

        etVisualYoutube = findViewById(R.id.et_visual_youtube_url);
        etAudioYoutube = findViewById(R.id.et_audio_youtube_url);
        etKinYoutube = findViewById(R.id.et_kin_youtube_url);
        
        tvVisualFile = findViewById(R.id.section_visual_upload).findViewById(R.id.tv_selected_filename);
        tvAudioFile = findViewById(R.id.section_audio_upload).findViewById(R.id.tv_selected_filename);
        tvKinFile = findViewById(R.id.section_kin_upload).findViewById(R.id.tv_selected_filename);

        ((TextView)findViewById(R.id.section_visual_upload).findViewById(R.id.tv_upload_label)).setText("Upload Video");
        ((TextView)findViewById(R.id.section_audio_upload).findViewById(R.id.tv_upload_label)).setText("Upload Audio");
        ((TextView)findViewById(R.id.section_kin_upload).findViewById(R.id.tv_upload_label)).setText("Upload Dokumen/Aktivitas");
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? -1 : checkedIds.get(0);
            
            layoutVisual.setVisibility(View.GONE);
            layoutAudio.setVisibility(View.GONE);
            layoutKin.setVisibility(View.GONE);
            
            if (checkedId == R.id.chip_visual) {
                layoutVisual.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chip_audio) {
                layoutAudio.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chip_kinestetik) {
                layoutKin.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.chip_semua) {
                layoutVisual.setVisibility(View.VISIBLE);
                layoutAudio.setVisibility(View.VISIBLE);
                layoutKin.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.section_visual_upload).findViewById(R.id.btn_action_select).setOnClickListener(v -> pickFile("video/*", visualLauncher));
        findViewById(R.id.section_audio_upload).findViewById(R.id.btn_action_select).setOnClickListener(v -> pickFile("audio/*", audioLauncher));
        findViewById(R.id.section_kin_upload).findViewById(R.id.btn_action_select).setOnClickListener(v -> pickFile("*/*", kinLauncher));

        findViewById(R.id.btn_save_topic).setOnClickListener(v -> validateAndSave());
    }

    private void pickFile(String type, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        launcher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> visualLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            visualUri = result.getData().getData();
            tvVisualFile.setVisibility(View.VISIBLE);
            tvVisualFile.setText("File terpilih: " + visualUri.getLastPathSegment());
        }
    });

    private final ActivityResultLauncher<Intent> audioLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            audioUri = result.getData().getData();
            tvAudioFile.setVisibility(View.VISIBLE);
            tvAudioFile.setText("File terpilih: " + audioUri.getLastPathSegment());
        }
    });

    private final ActivityResultLauncher<Intent> kinLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            kinUri = result.getData().getData();
            tvKinFile.setVisibility(View.VISIBLE);
            tvKinFile.setText("File terpilih: " + kinUri.getLastPathSegment());
        }
    });

    private void validateAndSave() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId == -1) {
            Toast.makeText(this, "Pilih gaya belajar", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> styles = new ArrayList<>();
        if (checkedId == R.id.chip_semua) {
            styles.add("visual");
            styles.add("audio");
            styles.add("kinestetik");
        } else if (checkedId == R.id.chip_visual) {
            styles.add("visual");
        } else if (checkedId == R.id.chip_audio) {
            styles.add("audio");
        } else if (checkedId == R.id.chip_kinestetik) {
            styles.add("kinestetik");
        }

        saveWithUploads(title, desc, styles);
    }

    private void saveWithUploads(String title, String desc, List<String> styles) {
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Menyimpan materi & mengunggah file...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        Map<String, Object> topicData = new HashMap<>();
        topicData.put("judul", title);
        topicData.put("deskripsi", desc);
        topicData.put("subject_id", subjectId);
        topicData.put("learning_styles", styles);
        topicData.put("order", System.currentTimeMillis());

        db.collection("topics").add(topicData).addOnSuccessListener(docRef -> {
            String topicId = docRef.getId();
            docRef.update("topic_id", topicId);
            
            uploadFilesAndSaveContent(topicId, title, desc, styles);
        });
    }

    private void uploadFilesAndSaveContent(String topicId, String title, String desc, List<String> styles) {
        Map<String, Object> content = new HashMap<>();
        content.put("topic_id", topicId);
        content.put("title", title);
        content.put("description", desc);
        content.put("learning_styles", styles);

        // Upload flow: Visual -> Audio -> Kinestetik -> Save
        uploadVisual(topicId, content, styles);
    }

    private void uploadVisual(String topicId, Map<String, Object> content, List<String> styles) {
        if (styles.contains("visual") && visualUri != null) {
            StorageReference ref = storage.getReference().child("content/" + topicId + "/visual_" + UUID.randomUUID().toString());
            ref.putFile(visualUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                Map<String, String> visualMap = new HashMap<>();
                visualMap.put("text_content", etVisualExp.getText().toString());
                visualMap.put("youtube_url", etVisualYoutube.getText().toString());
                visualMap.put("video_url", uri.toString());
                content.put("visual", visualMap);
                uploadAudio(topicId, content, styles);
            })).addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                uploadAudio(topicId, content, styles);
            });
        } else {
            if (styles.contains("visual")) {
                Map<String, String> visualMap = new HashMap<>();
                visualMap.put("text_content", etVisualExp.getText().toString());
                visualMap.put("youtube_url", etVisualYoutube.getText().toString());
                content.put("visual", visualMap);
            }
            uploadAudio(topicId, content, styles);
        }
    }

    private void uploadAudio(String topicId, Map<String, Object> content, List<String> styles) {
        if (styles.contains("audio") && audioUri != null) {
            StorageReference ref = storage.getReference().child("content/" + topicId + "/audio_" + UUID.randomUUID().toString());
            ref.putFile(audioUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                Map<String, String> audioMap = new HashMap<>();
                audioMap.put("description", etAudioExp.getText().toString());
                audioMap.put("youtube_url", etAudioYoutube.getText().toString());
                audioMap.put("audio_url", uri.toString());
                content.put("audio", audioMap);
                uploadKin(topicId, content, styles);
            })).addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal upload audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                uploadKin(topicId, content, styles);
            });
        } else {
            if (styles.contains("audio")) {
                Map<String, String> audioMap = new HashMap<>();
                audioMap.put("description", etAudioExp.getText().toString());
                audioMap.put("youtube_url", etAudioYoutube.getText().toString());
                content.put("audio", audioMap);
            }
            uploadKin(topicId, content, styles);
        }
    }

    private void uploadKin(String topicId, Map<String, Object> content, List<String> styles) {
        if (styles.contains("kinestetik") && kinUri != null) {
            StorageReference ref = storage.getReference().child("content/" + topicId + "/kin_" + UUID.randomUUID().toString());
            ref.putFile(kinUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                Map<String, String> kinMap = new HashMap<>();
                kinMap.put("description", etKinExp.getText().toString());
                kinMap.put("youtube_url", etKinYoutube.getText().toString());
                kinMap.put("file_url", uri.toString());
                content.put("kinestetik", kinMap);
                saveFinalContent(topicId, content);
            })).addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                saveFinalContent(topicId, content);
            });
        } else {
            if (styles.contains("kinestetik")) {
                Map<String, String> kinMap = new HashMap<>();
                kinMap.put("description", etKinExp.getText().toString());
                kinMap.put("youtube_url", etKinYoutube.getText().toString());
                content.put("kinestetik", kinMap);
            }
            saveFinalContent(topicId, content);
        }
    }

    private void saveFinalContent(String topicId, Map<String, Object> content) {
        db.collection("content").document(topicId).set(content).addOnSuccessListener(aVoid -> {
            if (progressDialog != null) progressDialog.dismiss();
            Toast.makeText(this, "Materi berhasil disimpan", Toast.LENGTH_SHORT).show();
            // Go back to the topics list immediately
            finish();
        }).addOnFailureListener(e -> {
            if (progressDialog != null) progressDialog.dismiss();
            Toast.makeText(this, "Gagal menyimpan content", Toast.LENGTH_SHORT).show();
        });
    }
}
