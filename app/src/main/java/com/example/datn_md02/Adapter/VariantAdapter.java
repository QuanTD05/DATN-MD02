package com.example.datn_md02.Adapter;

import android.content.Context;
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
    private final Context context;
    private final OnVariantClickListener listener;

    // ✅ Giao diện callback
    public interface OnVariantClickListener {
        void onVariantClick(VariantDisplay variant);
    }

    // ✅ Constructor có listener
    public VariantAdapter(Context context, List<VariantDisplay> variantList, OnVariantClickListener listener) {
        this.context = context;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_variant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VariantDisplay item = variantList.get(position);

        holder.tvSizeColor.setText(item.size + " - " + item.color);
        holder.tvVariantPrice.setText(String.format(Locale.getDefault(), "Giá: %,dđ", (long) item.price));
        holder.tvVariantQty.setText("SL: " + item.quantity);

        Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.haha)
                .into(holder.imgVariant);

        // ✅ Gọi callback khi click vào biến thể
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
