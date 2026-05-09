package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import java.util.List;

public class AdapterTopic extends RecyclerView.Adapter<AdapterTopic.ViewHolder> {

    private final List<ModelTopic> topics;

    public AdapterTopic(List<ModelTopic> topics) {
        this.topics = topics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelTopic topic = topics.get(position);
        holder.tvJudul.setText(topic.getJudul());
        holder.tvIndex.setText(String.valueOf(position + 1));

        holder.itemView.setOnClickListener(v -> {
            PageContentFragment fragment = new PageContentFragment();
            Bundle bundle = new Bundle();
            bundle.putString("topic_id", topic.getTopic_id());
            bundle.putString("topic_judul", topic.getJudul());
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
        return topics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIndex;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tv_topic_judul);
            tvIndex = itemView.findViewById(R.id.tv_topic_index);
        }
    }
}
