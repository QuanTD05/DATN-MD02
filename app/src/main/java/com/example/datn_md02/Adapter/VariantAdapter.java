package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.VariantDisplay;
import com.example.datn_md02.R;

import java.util.List;
import java.util.Locale;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    private final List<VariantDisplay> variantList;
    private final OnVariantClickListener listener;

    // ✅ Giao diện callback khi click vào biến thể
    public interface OnVariantClickListener {
        void onVariantClick(VariantDisplay variant);
    }

    // ✅ Constructor
    public VariantAdapter(List<VariantDisplay> variantList, OnVariantClickListener listener) {
        this.variantList = variantList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVariant;
        TextView tvSizeColor, tvVariantPrice, tvVariantQty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVariant = itemView.findViewById(R.id.imgVariant);
            tvSizeColor = itemView.findViewById(R.id.tvSizeColor);
            tvVariantPrice = itemView.findViewById(R.id.tvVariantPrice);
            tvVariantQty = itemView.findViewById(R.id.tvVariantQty);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_variant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VariantDisplay item = variantList.get(position);

        // ✅ Gộp Size và Color
        String size = item.getSize() != null ? item.getSize() : "N/A";
        String color = item.getColor() != null ? item.getColor() : "N/A";
        holder.tvSizeColor.setText(size + " - " + color);

        // ✅ Hiển thị giá và tồn kho
        holder.tvVariantPrice.setText(String.format(Locale.getDefault(), "Giá: %,d₫", (long) item.getPrice()));
        holder.tvVariantQty.setText("SL: " + item.getQuantity());

        // ✅ Load ảnh
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.haha)
                .into(holder.imgVariant);

        // ✅ Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVariantClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return variantList != null ? variantList.size() : 0;
    }
}
