package com.belajar.myapplication.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * APA ITU AUTHMANAGER?
 * Ini adalah kelas "Pelayan" atau Jembatan.
 * Tugasnya: Menghubungkan Aplikasi Anda ke server Firebase.
 * Kenapa dipisah? Agar jika Anda ingin mengganti Firebase ke sistem lain, 
 * Anda cukup ubah file ini saja, tidak perlu ubah semua Activity.
 */
public class AuthManager {
    private final FirebaseAuth mAuth; // Tools untuk Cek Email & Password (Autentikasi)
    private final FirebaseFirestore db; // Tools untuk Simpan Data Tambahan seperti Nama (Database)

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Interface untuk memberi laporan ke Activity apakah proses Berhasil atau Gagal
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String message);
    }

    // ALUR LOGIN: Menghubungi Firebase Auth
    public void loginUser(String email, String pass, AuthCallback callback) {
        // STEP 2: Firebase mengecek di database mereka apakah email & pass cocok
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Jika COCOK, kirim data user kembali ke Activity
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        // Jika SALAH, kirim pesan error
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }

    // ALUR REGISTER: Membuat akun baru
    public void registerUser(String email, String pass, String nama, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Setelah akun terbuat, simpan nama lengkap ke Firestore
                            saveUserToFirestore(user.getUid(), nama, email, callback);
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Registration Failed");
                    }
                });
    }

    // ALUR DATA: Menyimpan profil tambahan ke Database (Firestore)
    private void saveUserToFirestore(String uid, String nama, String email, AuthCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("nama", nama);
        user.put("email", email);
        user.put("role", "user"); // Default role saat daftar adalah user
        user.put("isPremium", false); // Default status premium adalah false

        // Data disimpan di koleksi "users" dengan ID sesuai UID Firebase Auth
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(mAuth.getCurrentUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logout() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
