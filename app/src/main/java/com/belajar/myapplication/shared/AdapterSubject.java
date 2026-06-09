package com.belajar.myapplication.shared;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.bumptech.glide.Glide;
import java.util.List;

public class AdapterSubject extends RecyclerView.Adapter<AdapterSubject.ViewHolder> {

    public interface OnSubjectClickListener {
        void onSubjectClick(ModelSubject subject, View view);
    }

    // Warna pastel per posisi — sesuai desain
    private static final String[] DEFAULT_COLORS = {
            "#D1E9FF", // Mathematics  — biru muda
            "#CEF7FF", // Chemistry    — cyan muda
            "#D1FFD1", // Biology      — hijau muda
            "#E9D1FF"  // Physics      — ungu muda
    };

    private final List<ModelSubject> subjects;
    private final int layoutRes;
    private final OnSubjectClickListener listener;

    public AdapterSubject(List<ModelSubject> subjects, int layoutRes, OnSubjectClickListener listener) {
        this.subjects = subjects;
        this.layoutRes = layoutRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelSubject subject = subjects.get(position);

        // Set nama
        holder.tvNama.setText(subject.getNama());

        // Set warna CardView secara langsung (bukan tint drawable)
        String colorHex = subject.getColor_hex();
        if (colorHex == null || colorHex.isEmpty()) {
            colorHex = DEFAULT_COLORS[position % DEFAULT_COLORS.length];
        }
        try {
            holder.cardBg.setCardBackgroundColor(Color.parseColor(colorHex));
        } catch (Exception ignored) {
            holder.cardBg.setCardBackgroundColor(Color.parseColor(DEFAULT_COLORS[0]));
        }

        // Set icon
        String icon = subject.getIcon_name();
        if (icon != null && icon.startsWith("http")) {
            Glide.with(holder.itemView.getContext()).load(icon).into(holder.ivIcon);
        } else {
            int resId = resolveIconRes(holder, subject, icon);
            holder.ivIcon.setImageResource(resId);
        }

        // Total modul (opsional, bisa null di layout ini)
        if (holder.tvTotalModul != null) {
            holder.tvTotalModul.setText(subject.getTotal_moduls() + " Modul");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSubjectClick(subject, v);
        });
    }

    private int resolveIconRes(ViewHolder holder, ModelSubject subject, String iconName) {
        // Coba resolve dari nama drawable dulu
        if (iconName != null && !iconName.isEmpty()) {
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    iconName, "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) return resId;
        }
        // Fallback: cocokkan dari nama subject
        if (subject.getNama() != null) {
            String lower = subject.getNama().toLowerCase();
            if (lower.contains("chem") || lower.contains("kimia"))  return R.drawable.shared_ic_chem;
            if (lower.contains("bio"))                               return R.drawable.shared_ic_bio;
            if (lower.contains("phys") || lower.contains("fisika")) return R.drawable.shared_ic_phys;
            if (lower.contains("geografi") || lower.contains("geo")) return R.drawable.shared_ic_degree;
            if (lower.contains("ekonomi") || lower.contains("econ")) return R.drawable.shared_ic_stats;
            if (lower.contains("sejarah") || lower.contains("hist")) return R.drawable.shared_ic_book;
            if (lower.contains("sosiologi") || lower.contains("sos")) return R.drawable.shared_ic_people;
        }
        return R.drawable.shared_ic_math; // default
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardBg;        // ID: layout_subject_bg
        ImageView ivIcon;       // ID: iv_subject_icon
        TextView tvNama;        // ID: tv_subject_name
        TextView tvTotalModul;  // ID: tv_total_modul (opsional)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBg       = itemView.findViewById(R.id.layout_subject_bg);
            ivIcon       = itemView.findViewById(R.id.iv_subject_icon);
            tvNama       = itemView.findViewById(R.id.tv_subject_name);
            tvTotalModul = itemView.findViewById(R.id.tv_total_modul); // nullable, aman
        }
    }
}