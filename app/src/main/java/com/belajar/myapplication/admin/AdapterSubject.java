package com.belajar.myapplication.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * Adapter untuk menampilkan daftar Mata Pelajaran di sisi Admin.
 * Mendukung pemuatan gambar dari URL (Storage) maupun Drawable lokal.
 */
public class AdapterSubject extends RecyclerView.Adapter<AdapterSubject.ViewHolder> {

    private final List<ModelSubject> subjectList;

    public AdapterSubject(List<ModelSubject> subjectList) {
        this.subjectList = subjectList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan item layout khusus admin
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_subject_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelSubject subject = subjectList.get(position);

        // Set nama mata pelajaran
        holder.tvName.setText(subject.getNama());

        // Set warna background dinamis dari hex kode (Firestore)
        if (subject.getColor_hex() != null && !subject.getColor_hex().isEmpty()) {
            try {
                holder.viewBg.getBackground().setTint(Color.parseColor(subject.getColor_hex()));
            } catch (Exception ignored) {}
        }

        // Tampilkan jumlah modul jika data tersedia
        if (holder.tvTotalModul != null) {
            String total = subject.getTotal_moduls() + " Modul Tersedia";
            holder.tvTotalModul.setText(total);
        }

        // Logika Pemuatan Ikon (URL atau Nama Drawable)
        String icon = subject.getIcon_name();
        if (icon != null) {
            if (icon.startsWith("http")) {
                // Gunakan Glide jika ikon berupa URL dari Firebase Storage
                Glide.with(holder.itemView.getContext()).load(icon).into(holder.ivIcon);
            } else {
                // Cari ID drawable jika ikon berupa nama file lokal
                int resId = holder.itemView.getContext().getResources().getIdentifier(
                        icon, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    holder.ivIcon.setImageResource(resId);
                } else {
                    holder.ivIcon.setImageResource(R.drawable.shared_ic_math); // Default
                }
            }
        }

        // Klik Item: Berpindah ke Fragment Detail Materi (MateriFragment)
        holder.itemView.setOnClickListener(v -> {
            MateriFragment fragment = new MateriFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", subject.getSubject_id());
            bundle.putString("subject_name", subject.getNama());
            fragment.setArguments(bundle);

            // Melakukan transaksi fragment melalui Activity
            if (holder.itemView.getContext() instanceof FragmentActivity) {
                ((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    /**
     * ViewHolder untuk memegang referensi ke tampilan item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTotalModul;
        ImageView ivIcon;
        View viewBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_subject_name);
            ivIcon = itemView.findViewById(R.id.iv_subject_icon);
            viewBg = itemView.findViewById(R.id.view_subject_bg);
            tvTotalModul = itemView.findViewById(R.id.tv_total_modul);
        }
    }
}
