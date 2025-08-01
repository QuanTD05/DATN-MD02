package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.Promotion;
import com.example.datn_md02.R;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromoViewHolder> {

    public interface OnPromoClickListener {
        void onClick(Promotion promotion);
    }

    private final List<Promotion> list;
    private final OnPromoClickListener listener;

    public PromotionAdapter(List<Promotion> list, OnPromoClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promotion promo = list.get(position);
        holder.tvCode.setText(promo.getCode());
        holder.tvDescription.setText(promo.getDescription());
        holder.tvDiscount.setText("-" + promo.getDiscount() + "%");

        holder.itemView.setOnClickListener(v -> listener.onClick(promo));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDescription, tvDiscount;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvPromoCode);
            tvDescription = itemView.findViewById(R.id.tvPromoDescription);
            tvDiscount = itemView.findViewById(R.id.tvPromoDiscount);
        }
    }
}
