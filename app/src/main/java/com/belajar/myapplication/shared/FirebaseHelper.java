package com.belajar.myapplication.shared;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;

public class FirebaseHelper {
    public static void fetchSubjects(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore.getInstance().collection("subjects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                listener.onComplete(task);
            } else {
                FirebaseFirestore.getInstance().collection("subject").get().addOnCompleteListener(listener);
            }
        });
    }

    public static void fetchTopics(String subjectId, OnCompleteListener<QuerySnapshot> listener) {
        if (subjectId == null) return;
        FirebaseFirestore.getInstance().collection("topics")
                .whereEqualTo("subject_id", subjectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        listener.onComplete(task);
                    } else {
                        FirebaseFirestore.getInstance().collection("topic")
                                .whereEqualTo("subject_id", subjectId)
                                .get()
                                .addOnCompleteListener(listener);
                    }
                });
    }
}
