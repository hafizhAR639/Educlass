package com.belajar.myapplication.admin;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelActivity;
import java.util.List;

public class AdminAdapterActivity extends RecyclerView.Adapter<AdminAdapterActivity.ViewHolder> {

    private final List<ModelActivity> activityList;

    public AdminAdapterActivity(List<ModelActivity> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelActivity activity = activityList.get(position);
        holder.tvDesc.setText(activity.getDescription());
        
        if (activity.getTimestamp() != null) {
            long time = activity.getTimestamp().toDate().getTime();
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.tvTime.setText(timeAgo);
        }

        // Set icon based on type
        if ("add".equals(activity.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_check);
        } else if ("delete".equals(activity.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_eye_off);
        } else {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_time);
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDesc, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvDesc = itemView.findViewById(R.id.tv_activity_desc);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
        }
    }
}
