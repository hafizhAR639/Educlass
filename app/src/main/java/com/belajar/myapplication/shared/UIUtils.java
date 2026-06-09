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
        
        if (lowName.contains("matematika") || lowName.contains("math")) resId = R.drawable.user_img_header_math;
        else if (lowName.contains("fisika") || lowName.contains("phys")) resId = R.drawable.user_img_header_math; // Use math as placeholder
        else if (lowName.contains("geografi") || lowName.contains("geo")) resId = R.drawable.shared_bg_header_blue;
        else if (lowName.contains("sejarah") || lowName.contains("hist")) resId = R.drawable.shared_bg_header_blue;
        else if (lowName.contains("ekonomi") || lowName.contains("econ")) resId = R.drawable.shared_bg_header_blue;
        else if (lowName.contains("sosiologi") || lowName.contains("sos")) resId = R.drawable.shared_bg_header_blue;
        
        imageView.setImageResource(resId);
    }
}
