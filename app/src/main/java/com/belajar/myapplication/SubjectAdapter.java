package com.belajar.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.models.Subject;
import com.bumptech.glide.Glide;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private List<Subject> subjects;
    private boolean isHorizontal;

    public SubjectAdapter(List<Subject> subjects, boolean isHorizontal) {
        this.subjects = subjects;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isHorizontal ? R.layout.item_subject_home : R.layout.item_subject_modul;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        holder.tvNama.setText(subject.getNama());
        
        // Handle Background Color
        try {
            if (holder.layoutBg != null && subject.getColor_hex() != null) {
                holder.layoutBg.getBackground().setTint(Color.parseColor(subject.getColor_hex()));
            }
        } catch (Exception e) {
            // Fallback if color string is invalid
        }

        // Handle Icon (using icon_name from database)
        if (subject.getIcon_name() != null) {
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    subject.getIcon_name(), "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                holder.ivIcon.setImageResource(resId);
            } else {
                // Fallback to default if icon_name not found in drawables
                holder.ivIcon.setImageResource(R.drawable.ic_math);
            }
        } else {
            // Default icons mapping if icon_name is null
            int resId = R.drawable.ic_math; 
            if (subject.getNama() != null) {
                if (subject.getNama().toLowerCase().contains("fisika")) resId = R.drawable.ic_phys;
                else if (subject.getNama().toLowerCase().contains("kimia")) resId = R.drawable.ic_chem;
                else if (subject.getNama().toLowerCase().contains("biologi")) resId = R.drawable.ic_bio;
            }
            holder.ivIcon.setImageResource(resId);
        }

        if (holder.tvTotalModul != null) {
            holder.tvTotalModul.setText(subject.getTotal_moduls() + " Modul Tersedia");
        }

        holder.itemView.setOnClickListener(v -> {
            MateriFragment fragment = new MateriFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", subject.getSubject_id());
            bundle.putString("subject_name", subject.getNama());
            fragment.setArguments(bundle);

            ((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTotalModul;
        ImageView ivIcon;
        LinearLayout layoutBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_subject_name);
            ivIcon = itemView.findViewById(R.id.iv_subject_icon);
            layoutBg = itemView.findViewById(R.id.layout_subject_bg);
            tvTotalModul = itemView.findViewById(R.id.tv_total_modul);
        }
    }
}
