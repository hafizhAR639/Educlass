package com.belajar.myapplication.shared;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.belajar.myapplication.R;

public class UIUtils {

    /**
     * Menampilkan Toast kustom dengan logo Educlass.
     */
    public static void showCustomToast(Context context, String message) {
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.shared_layout_toast, null);

        TextView text = layout.findViewById(R.id.tv_toast_message);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    
    /**
     * Mengatur gambar header secara dinamis berdasarkan nama mata pelajaran.
     */
    public static void setHeaderImage(String subjectName, ImageView imageView) {
        if (subjectName == null || imageView == null) return;
        
        int resId = R.drawable.shared_bg_header_blue; // Default
        String lowName = subjectName.toLowerCase();
        
        if (lowName.contains("matematika")) resId = R.drawable.user_img_header_math;
        // Tambahkan kondisi lain di sini (Fisika, Kimia, dll) jika sudah ada asetnya
        
        imageView.setImageResource(resId);
    }
}
