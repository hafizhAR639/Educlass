package com.belajar.myapplication.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import java.util.List;

/**
 * Adapter untuk menampilkan daftar Topik (Materi) dalam bentuk Grid di panel Admin.
 */
public class AdapterTopic extends RecyclerView.Adapter<AdapterTopic.ViewHolder> {

    private final List<ModelTopic> topics;

    public AdapterTopic(List<ModelTopic> topics) {
        this.topics = topics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelTopic topic = topics.get(position);

        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvJudul.setText(topic.getJudul());

        // Desc - fallback kalau null
        String desc = topic.getDeskripsi() != null ? topic.getDeskripsi() : "Kelola materi di sini";
        holder.tvDesc.setText(desc);

        // Progress
        int progress = topic.getProgress(); // sesuaikan dengan field di ModelTopic
        holder.progressBar.setProgress(progress);
        holder.tvProgress.setText(progress + "%");

        // Durasi
        String durasi = topic.getDurasi() != null ? "⏱ " + topic.getDurasi() : "⏱ -";
        holder.tvTime.setText(durasi);

        // Tombol edit
        holder.btnEdit.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Edit: " + topic.getJudul(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIndex, tvDesc, tvTime, tvProgress;
        ProgressBar progressBar;
        ImageView btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul     = itemView.findViewById(R.id.tv_topic_judul);
            tvIndex     = itemView.findViewById(R.id.tv_topic_index);
            tvDesc      = itemView.findViewById(R.id.tv_topic_desc);
            tvTime      = itemView.findViewById(R.id.tv_time);
            tvProgress  = itemView.findViewById(R.id.tv_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnEdit     = itemView.findViewById(R.id.btn_edit_topic);
        }
    }
}