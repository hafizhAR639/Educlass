package com.belajar.myapplication.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelUser;
import java.util.List;

public class AdapterUserAdmin extends RecyclerView.Adapter<AdapterUserAdmin.ViewHolder> {

    private final List<ModelUser> userList;

    public AdapterUserAdmin(List<ModelUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelUser user = userList.get(position);
        
        String nama = user.getNama();
        holder.tvName.setText(nama != null ? nama : "No Name");
        holder.tvEmail.setText(user.getEmail());
        
        // Get initials (first two letters)
        if (nama != null && nama.length() >= 2) {
            holder.tvInitials.setText(nama.substring(0, 2).toUpperCase());
        } else if (nama != null && nama.length() == 1) {
            holder.tvInitials.setText(nama.toUpperCase());
        } else {
            holder.tvInitials.setText("??");
        }

        // Status always "Aktif" for now as requested
        holder.tvStatus.setText("Aktif");
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvInitials, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
        }
    }
}
