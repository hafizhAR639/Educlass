package com.belajar.myapplication.shared;

import android.widget.ImageView;
import com.belajar.myapplication.R;

public class UIUtils {
    
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
