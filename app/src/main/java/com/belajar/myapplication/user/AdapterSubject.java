package com.belajar.myapplication.user;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.bumptech.glide.Glide;
import java.util.List;

public class AdapterSubject extends RecyclerView.Adapter<AdapterSubject.ViewHolder> {

    private final List<ModelSubject> subjects;
    private final boolean isHorizontal;

    public AdapterSubject(List<ModelSubject> subjects, boolean isHorizontal) {
        this.subjects = subjects;
        this.isHorizontal = isHorizontal;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = isHorizontal ? R.layout.user_item_subject_home : R.layout.user_item_subject_modul;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelSubject subject = subjects.get(position);
        holder.tvNama.setText(subject.getNama());
        
        try {
            if (holder.layoutBg != null && subject.getColor_hex() != null) {
                holder.layoutBg.getBackground().setTint(Color.parseColor(subject.getColor_hex()));
            }
        } catch (Exception e) {
            // Fallback
        }

        if (subject.getIcon_name() != null) {
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    subject.getIcon_name(), "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                holder.ivIcon.setImageResource(resId);
            } else {
                holder.ivIcon.setImageResource(R.drawable.shared_ic_math);
            }
        } else {
            int resId = R.drawable.shared_ic_math; 
            if (subject.getNama() != null) {
                if (subject.getNama().toLowerCase().contains("fisika")) resId = R.drawable.shared_ic_phys;
                else if (subject.getNama().toLowerCase().contains("kimia")) resId = R.drawable.shared_ic_chem;
                else if (subject.getNama().toLowerCase().contains("biologi")) resId = R.drawable.shared_ic_bio;
            }
            holder.ivIcon.setImageResource(resId);
        }

        if (holder.tvTotalModul != null) {
            holder.tvTotalModul.setText(subject.getTotal_moduls() + " Modul Tersedia");
        }

        holder.itemView.setOnClickListener(v -> {
            PageMateriFragment fragment = new PageMateriFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", subject.getSubject_id());
            bundle.putString("subject_name", subject.getNama());
            fragment.setArguments(bundle);

            ((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_fragment_container, fragment)
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
