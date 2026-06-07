package com.belajar.myapplication.shared;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.bumptech.glide.Glide;
import java.util.List;

public class AdapterSubject extends RecyclerView.Adapter<AdapterSubject.ViewHolder> {

    public interface OnSubjectClickListener {
        void onSubjectClick(ModelSubject subject, View view);
    }

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
        holder.tvNama.setText(subject.getNama());
        
        // Background color logic to match design
        View bgView = holder.viewBg != null ? holder.viewBg : holder.itemView.findViewById(R.id.layout_subject_bg);
        if (bgView == null) bgView = holder.itemView.findViewById(R.id.view_subject_bg);
        
        if (bgView != null) {
            String colorHex = subject.getColor_hex();
            if (colorHex == null || colorHex.isEmpty()) {
                // Predefined light colors from design: Blue, Purple, Teal, Green, Pink, Red
                String[] colors = {"#D1E9FF", "#E9D1FF", "#CEF7FF", "#D1FFD1", "#FFD1E9", "#FFD1D1"};
                colorHex = colors[position % colors.length];
            }
            try {
                Drawable background = bgView.getBackground();
                if (background != null) {
                    Drawable wrapped = DrawableCompat.wrap(background.mutate());
                    DrawableCompat.setTint(wrapped, Color.parseColor(colorHex));
                }
            } catch (Exception ignored) {}
        }

        // Icon logic
        String icon = subject.getIcon_name();
        if (icon != null) {
            if (icon.startsWith("http")) {
                Glide.with(holder.itemView.getContext()).load(icon).into(holder.ivIcon);
            } else {
                int resId = holder.itemView.getContext().getResources().getIdentifier(
                        icon, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    holder.ivIcon.setImageResource(resId);
                } else {
                    holder.ivIcon.setImageResource(R.drawable.shared_ic_math);
                }
            }
        } else {
            int resId = R.drawable.shared_ic_math; 
            if (subject.getNama() != null) {
                String lowName = subject.getNama().toLowerCase();
                if (lowName.contains("fisika")) resId = R.drawable.shared_ic_phys;
                else if (lowName.contains("kimia")) resId = R.drawable.shared_ic_chem;
                else if (lowName.contains("biologi")) resId = R.drawable.shared_ic_bio;
            }
            holder.ivIcon.setImageResource(resId);
        }

        if (holder.tvTotalModul != null) {
            holder.tvTotalModul.setText(subject.getTotal_moduls() + " Modul Tersedia");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSubjectClick(subject, v);
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTotalModul;
        ImageView ivIcon;
        View viewBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_subject_name);
            ivIcon = itemView.findViewById(R.id.iv_subject_icon);
            tvTotalModul = itemView.findViewById(R.id.tv_total_modul);
            // Try to find background view with multiple common IDs
            viewBg = itemView.findViewById(R.id.layout_subject_bg);
            if (viewBg == null) viewBg = itemView.findViewById(R.id.view_subject_bg);
        }
    }
}
