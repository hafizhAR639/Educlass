package com.belajar.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.models.Topic;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    private List<Topic> topics;

    public TopicAdapter(List<Topic> topics) {
        this.topics = topics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.tvJudul.setText(topic.getJudul());
        holder.tvIndex.setText(String.valueOf(position + 1));
        
        // Custom background for the index circle or header if needed
        // For example, if you want different colors for different subjects

        holder.itemView.setOnClickListener(v -> {
            VisualFragment fragment = new VisualFragment();
            Bundle bundle = new Bundle();
            bundle.putString("topic_id", topic.getTopic_id());
            bundle.putString("topic_judul", topic.getJudul());
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
