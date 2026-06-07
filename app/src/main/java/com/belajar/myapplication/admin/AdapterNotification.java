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
import com.belajar.myapplication.data.models.ModelNotification;
import java.util.List;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.ViewHolder> {

    private final List<ModelNotification> notificationList;

    public AdapterNotification(List<ModelNotification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelNotification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDesc.setText(notification.getDescription());
        
        if (notification.getTimestamp() != null) {
            long time = notification.getTimestamp().toDate().getTime();
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.tvTime.setText(timeAgo);
        }

        holder.viewUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        holder.cardContainer.setCardBackgroundColor(notification.isRead() ? 0xFFFFFFFF : 0xFFF0F7FF);

        // Set icon and background color based on type
        if ("user".equals(notification.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_people);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_card_white_r20_xml);
            holder.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF347EFF));
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
        } else if ("modul".equals(notification.getType()) || "modul_new".equals(notification.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_book);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_card_white_r20_xml);
            holder.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCEF7FF));
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000));
        } else if ("progress".equals(notification.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_stats);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_card_white_r20_xml);
            holder.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFD1FFBD));
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000));
        } else if ("user_active".equals(notification.getType())) {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_people);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_card_white_r20_xml);
            holder.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFCEF7FF));
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF347EFF));
        } else {
            holder.ivIcon.setImageResource(R.drawable.shared_ic_bell);
            holder.ivIcon.setBackgroundResource(R.drawable.bg_card_white_r20_xml);
            holder.ivIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE5D1FF));
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000));
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardContainer;
        ImageView ivIcon;
        TextView tvTitle, tvDesc, tvTime;
        View viewUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_notif_container);
            ivIcon = itemView.findViewById(R.id.iv_notif_icon);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvDesc = itemView.findViewById(R.id.tv_notif_desc);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            viewUnread = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
