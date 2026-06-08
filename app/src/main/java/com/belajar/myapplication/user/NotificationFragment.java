package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelNotification;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_notification, container, false);

        view.findViewById(R.id.btn_back_notif).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ModelNotification> list = new ArrayList<>();
        // Dummy data for now
        list.add(new ModelNotification("Selamat Datang!", "Terima kasih telah bergabung dengan EduClass.", "user", Timestamp.now()));
        list.add(new ModelNotification("Materi Baru", "Materi Fisika: Hukum Newton 3 sudah tersedia.", "modul", Timestamp.now()));
        list.add(new ModelNotification("Promo Premium", "Dapatkan diskon 50% untuk upgrade hari ini.", "promo", Timestamp.now()));

        rv.setAdapter(new NotificationAdapter(list));

        return view;
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final List<ModelNotification> notifications;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        public NotificationAdapter(List<ModelNotification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_notification, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModelNotification n = notifications.get(position);
            holder.tvTitle.setText(n.getTitle());
            holder.tvDesc.setText(n.getDescription());
            if (n.getTimestamp() != null) {
                holder.tvTime.setText(dateFormat.format(n.getTimestamp().toDate()));
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvTime;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_notif_title);
                tvDesc = v.findViewById(R.id.tv_notif_desc);
                tvTime = v.findViewById(R.id.tv_notif_time);
            }
        }
    }
}
