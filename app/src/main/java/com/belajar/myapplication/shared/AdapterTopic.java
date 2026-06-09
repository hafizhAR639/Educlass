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
    private String learningStyle = "Visual";
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

    public void setLearningStyle(String style) {
        this.learningStyle = style;
        notifyDataSetChanged();
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
            
            // Dynamic Tags based on Learning Style
            if (h.layoutTags != null) {
                h.layoutTags.removeAllViews();
                if (learningStyle.equalsIgnoreCase("Visual")) {
                    addTag(h.layoutTags, "🎥 Video", 0xFFFFF1F2, 0xFFE11D48);
                    addTag(h.layoutTags, "📖 Bacaan", 0xFFEFF6FF, 0xFF2563EB);
                } else if (learningStyle.equalsIgnoreCase("Audio")) {
                    addTag(h.layoutTags, "🎧 Audio", 0xFFFDF4FF, 0xFFA21CAF);
                    addTag(h.layoutTags, "📖 Bacaan", 0xFFEFF6FF, 0xFF2563EB);
                } else if (learningStyle.equalsIgnoreCase("Kinestetik")) {
                    addTag(h.layoutTags, "🎥 Video", 0xFFFFF1F2, 0xFFE11D48);
                    addTag(h.layoutTags, "📖 Bacaan", 0xFFEFF6FF, 0xFF2563EB);
                    addTag(h.layoutTags, "✍️ Kuis", 0xFFFFF7ED, 0xFFEA580C);
                }
            }
        } else if (holder instanceof LockedViewHolder) {
            LockedViewHolder h = (LockedViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            if (h.tvDesc != null) h.tvDesc.setText(topic.getDeskripsi());
            if (h.tvTime != null) h.tvTime.setText(topic.getDurasi());
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, true));
        } else if (holder instanceof PopularViewHolder) {
            PopularViewHolder h = (PopularViewHolder) holder;
            h.tvJudul.setText(topic.getJudul());
            h.tvViews.setText(topic.getViews_count() + " views");
            h.itemView.setOnClickListener(v -> listener.onTopicClick(topic, false));
            
            int resId = R.drawable.user_img_topic_aljabar; // Default
            String judul = topic.getJudul() != null ? topic.getJudul().toLowerCase() : "";
            
            if (judul.contains("pertidaksamaan")) {
                resId = R.drawable.user_img_topic_pertidaksamaan;
            } else if (judul.contains("geograf") || judul.contains("letak") || judul.contains("peta")) {
                // Use a generic IPS/Geography image if available, or stay consistent with colored circles if no specific image
                resId = R.drawable.shared_bg_grad_blue; 
            } else if (judul.contains("ekonom") || judul.contains("pasar") || judul.contains("uang")) {
                resId = R.drawable.shared_bg_grad_orange;
            } else if (judul.contains("sejarah") || judul.contains("kerajaan") || judul.contains("praaksara")) {
                resId = R.drawable.shared_bg_grad_pink;
            }
            
            h.ivThumb.setImageResource(resId);
            h.ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void addTag(ViewGroup parent, String text, int bgColor, int textColor) {
        TextView tv = new TextView(parent.getContext());
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, (int) (10 * parent.getContext().getResources().getDisplayMetrics().density), 0);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(12);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding((int) (12 * parent.getContext().getResources().getDisplayMetrics().density),
                (int) (5 * parent.getContext().getResources().getDisplayMetrics().density),
                (int) (12 * parent.getContext().getResources().getDisplayMetrics().density),
                (int) (5 * parent.getContext().getResources().getDisplayMetrics().density));
        tv.setBackgroundResource(R.drawable.shared_bg_tag_ipa);
        tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));
        parent.addView(tv);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIndex, tvDesc, tvDuration, tvProgress;
        SeekBar progressBar;
        ViewGroup layoutTags;
        NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_judul);
            tvIndex = itemView.findViewById(R.id.tv_topic_index);
            tvDesc = itemView.findViewById(R.id.tv_topic_desc);
            tvDuration = itemView.findViewById(R.id.tv_topic_duration);
            tvProgress = itemView.findViewById(R.id.tv_topic_progress_text);
            progressBar = itemView.findViewById(R.id.pb_topic);
            layoutTags = itemView.findViewById(R.id.layout_tags);
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
