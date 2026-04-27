package com.belajar.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Authenticate {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Authenticate() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String message);
    }

    public void loginUser(String email, String pass, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }

    public void registerUser(String email, String pass, String nama, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), nama, email, callback);
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Registration Failed");
                    }
                });
    }

    private void saveUserToFirestore(String uid, String nama, String email, AuthCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("nama", nama);
        user.put("email", email);

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
