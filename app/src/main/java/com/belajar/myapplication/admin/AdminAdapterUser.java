package com.belajar.myapplication.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAdapterUser extends RecyclerView.Adapter<AdminAdapterUser.ViewHolder> {

    private List<ModelUser> userList;
    private List<ModelUser> userListFull;
    private String selectedFilter = "Semua";
    private String searchQuery = "";

    public AdminAdapterUser(List<ModelUser> userList) {
        this.userList = new ArrayList<>(userList);
        this.userListFull = new ArrayList<>(userList);
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
        
        if (nama != null && nama.length() >= 2) {
            holder.tvInitials.setText(nama.substring(0, 2).toUpperCase());
        } else if (nama != null && nama.length() == 1) {
            holder.tvInitials.setText(nama.toUpperCase());
        } else {
            holder.tvInitials.setText("??");
        }

        if (user.isActive()) {
            holder.tvStatus.setText("Aktif");
            holder.tvStatus.setTextColor(Color.parseColor("#22C55E"));
            holder.tvStatus.getBackground().setTint(Color.parseColor("#E8F9EF"));
        } else {
            holder.tvStatus.setText("Tidak Aktif");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.tvStatus.getBackground().setTint(Color.parseColor("#FEE2E2"));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<ModelUser> newList) {
        this.userListFull = new ArrayList<>(newList);
        applyFilters();
    }

    public void setFilter(String filter) {
        this.selectedFilter = filter;
        applyFilters();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase(Locale.ROOT).trim();
        applyFilters();
    }

    private void applyFilters() {
        List<ModelUser> filteredList = new ArrayList<>();
        for (ModelUser user : userListFull) {
            boolean matchesFilter = true;
            if (selectedFilter.equals("Aktif")) {
                matchesFilter = user.isActive();
            } else if (selectedFilter.equals("Tidak Aktif")) {
                matchesFilter = !user.isActive();
            }

            boolean matchesSearch = true;
            if (!searchQuery.isEmpty()) {
                String name = user.getNama() != null ? user.getNama().toLowerCase(Locale.ROOT) : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.ROOT) : "";
                matchesSearch = name.contains(searchQuery) || email.contains(searchQuery);
            }

            if (matchesFilter && matchesSearch) {
                filteredList.add(user);
            }
        }
        this.userList = filteredList;
        notifyDataSetChanged();
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
