package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.User;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public interface OnStaffClickListener {
        void onStaffClick(User user);
    }

    private List<User> originalList;
    private List<User> filteredList;
    private OnStaffClickListener listener;

    public StaffAdapter(List<User> userList, OnStaffClickListener listener) {
        this.originalList = new ArrayList<>(userList);
        this.filteredList = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = filteredList.get(position);
        String displayName = user.getFullName() != null ? user.getFullName() : user.getName();
        holder.tvName.setText(displayName);

        long timestamp = user.getLastMessageTimestamp();
        holder.tvTimestamp.setText(timestamp > 0 ? formatTimestamp(timestamp) : "");

        holder.itemView.setOnClickListener(v -> listener.onStaffClick(user));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void searchByName(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (User user : originalList) {
                String fullName = user.getFullName() != null ? user.getFullName() : "";
                String name = user.getName() != null ? user.getName() : "";
                if (fullName.toLowerCase().contains(lowerQuery) || name.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<User> newList) {
        originalList.clear();
        originalList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(newList);
        notifyDataSetChanged();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTimestamp;
        ImageView imgAvatar;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
