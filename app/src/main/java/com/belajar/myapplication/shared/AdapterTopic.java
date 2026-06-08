package com.belajar.myapplication.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import java.util.List;
import java.util.Locale;

public class AdapterTopic extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(ModelTopic topic, boolean isLocked);
        default void onEditClick(View view, ModelTopic topic) {}
    }

    private final List<ModelTopic> topics;
    private final boolean isAdmin;
    private final boolean isPremium;
    private final OnTopicClickListener listener;

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_LOCKED = 1;
    private static final int TYPE_ADMIN = 2;
    public static final int TYPE_POPULAR = 3;

    private final int viewTypeOverride;

    public AdapterTopic(List<ModelTopic> topics, boolean isAdmin, boolean isPremium, OnTopicClickListener listener) {
        this(topics, isAdmin, isPremium, -1, listener);
    }

    public AdapterTopic(List<ModelTopic> topics, boolean isAdmin, boolean isPremium, int viewTypeOverride, OnTopicClickListener listener) {
        this.topics = topics;
        this.isAdmin = isAdmin;
        this.isPremium = isPremium;
        this.viewTypeOverride = viewTypeOverride;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (viewTypeOverride != -1) return viewTypeOverride;
        if (isAdmin) return TYPE_ADMIN;
        ModelTopic topic = topics.get(position);
        if (!isPremium && (topic.isPremium() || topic.getOrder() >= 3)) {
            return TYPE_LOCKED;
        }
        return TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADMIN) {
            return new AdminViewHolder(inflater.inflate(R.layout.admin_item_topic, parent, false));
        } else if (viewType == TYPE_LOCKED) {
            return new LockedViewHolder(inflater.inflate(R.layout.user_item_subject_modul_locked, parent, false));
        } else if (viewType == TYPE_POPULAR) {
            return new PopularViewHolder(inflater.inflate(R.layout.user_item_topic_popular, parent, false));
        } else {
            return new NormalViewHolder(inflater.inflate(R.layout.user_item_topic, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModelTopic topic = topics.get(position);

        if (holder instanceof AdminViewHolder) {
            AdminViewHolder h = (AdminViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            h.tvIndex.setText(String.valueOf(position + 1));
            h.tvDesc.setText(topic.getDeskripsi() != null ? topic.getDeskripsi() : "Kelola materi");
            h.tvTime.setText(topic.getDurasi() != null ? "⏱ " + topic.getDurasi() : "⏱ -");
            h.progressBar.setProgress(topic.getProgress());
            h.tvProgress.setText(String.format(Locale.getDefault(), "%d%%", topic.getProgress()));
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, false));
            if (h.btnEdit != null) {
                h.btnEdit.setOnClickListener(v -> listener.onEditClick(h.btnEdit, topic));
            }
        } else if (holder instanceof NormalViewHolder) {
            NormalViewHolder h = (NormalViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            h.tvIndex.setText(String.valueOf(position + 1));
            
            // Bind Deskripsi
            if (h.tvDesc != null) {
                String desc = topic.getDeskripsi();
                h.tvDesc.setText(desc != null && !desc.isEmpty() ? desc : "Pelajari materi ini untuk memahami konsep lebih dalam.");
            }
            
            // Bind Durasi
            if (h.tvDuration != null) {
                String dur = topic.getDurasi();
                h.tvDuration.setText(dur != null && !dur.isEmpty() ? dur : "45 menit");
            }
            
            // Bind Progress
            int progress = topic.getProgress();
            if (h.tvProgress != null) {
                h.tvProgress.setText(String.format(Locale.getDefault(), "%d%%", progress));
            }
            if (h.progressBar != null) {
                h.progressBar.setProgress(progress);
            }
            
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, false));
        } else if (holder instanceof LockedViewHolder) {
            LockedViewHolder h = (LockedViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            if (h.tvDesc != null) h.tvDesc.setText(topic.getDeskripsi());
            if (h.tvTime != null) h.tvTime.setText(topic.getDurasi());
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, true));
        } else if (holder instanceof PopularViewHolder) {
            PopularViewHolder h = (PopularViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            h.tvViews.setText(topic.getViews_count());
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, false));
            
            int resId = R.drawable.user_img_topic_aljabar;
            if (topic.getJudul() != null && topic.getJudul().toLowerCase().contains("pertidaksamaan")) {
                resId = R.drawable.user_img_topic_pertidaksamaan;
            }
            h.ivThumb.setImageResource(resId);
        }
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIndex, tvDesc, tvDuration, tvProgress;
        SeekBar progressBar;
        NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_judul);
            tvIndex = itemView.findViewById(R.id.tv_topic_index);
            tvDesc = itemView.findViewById(R.id.tv_topic_desc);
            tvDuration = itemView.findViewById(R.id.tv_topic_duration);
            tvProgress = itemView.findViewById(R.id.tv_topic_progress_text);
            progressBar = itemView.findViewById(R.id.pb_topic);
        }
    }

    static class LockedViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvDesc, tvTime;
        LockedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_judul);
            tvDesc = itemView.findViewById(R.id.tv_topic_desc);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIndex, tvDesc, tvTime, tvProgress;
        ProgressBar progressBar;
        ImageView btnEdit;
        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_judul);
            tvIndex = itemView.findViewById(R.id.tv_topic_index);
            tvDesc = itemView.findViewById(R.id.tv_topic_desc);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnEdit = itemView.findViewById(R.id.btn_edit_topic);
        }
    }

    static class PopularViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvViews;
        ImageView ivThumb;
        PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_title);
            tvViews = itemView.findViewById(R.id.tv_views_count);
            ivThumb = itemView.findViewById(R.id.iv_topic_thumb);
        }
    }
}
