package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.R;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private List<CartItem> items;

    public CartItemAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_simple, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText("x" + item.getQuantity());
        holder.tvPrice.setText(String.format("%,.0f VND", item.getPrice() * item.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getProductImage())
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvQuantity, tvPrice;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
