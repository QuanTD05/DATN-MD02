package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.R;

import java.util.List;

public class CartOrderAdapter extends RecyclerView.Adapter<CartOrderAdapter.CartViewHolder> {

    private final List<Cart> cartItemList;

    public CartOrderAdapter(List<Cart> cartItemList) {
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_order, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart item = cartItemList.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvVariant.setText("Màu: " + item.getVariantColor() + " - Size: " + item.getVariantSize());
        holder.tvQuantity.setText("x" + item.getQuantity());

        double totalPrice = item.getPrice() * item.getQuantity();
        holder.tvPrice.setText(String.format("₫%,.0f", totalPrice));

        Glide.with(holder.imgProduct.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_ban)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvVariant, tvQuantity, tvPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
