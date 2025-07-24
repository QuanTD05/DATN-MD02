package com.example.datn_md02.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.Cart;
import com.example.datn_md02.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartActionListener {
        void onIncrease(Cart item);
        void onDecrease(Cart item);
        void onDelete(Cart item);
        void onItemCheckedChanged(Cart item, boolean isChecked);
    }

    private final Context context;
    private final List<Cart> cartList;
    private final OnCartActionListener listener;

    public CartAdapter(Context context, List<Cart> cartList, OnCartActionListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart item = cartList.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvPrice.setText(String.format("%,.0f₫", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.checkboxItem.setChecked(item.isSelected());

        // Hiển thị ảnh sản phẩm
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.sample)
                .into(holder.imgProduct);

        // Hiển thị biến thể nếu có
        if ((item.getVariantSize() != null && !item.getVariantSize().isEmpty()) ||
                (item.getVariantColor() != null && !item.getVariantColor().isEmpty())) {
            String size = item.getVariantSize() != null ? item.getVariantSize() : "-";
            String color = item.getVariantColor() != null ? item.getVariantColor() : "-";
            holder.tvVariant.setText(String.format("Phân loại: Size %s, Màu %s", size, color));
            holder.tvVariant.setVisibility(View.VISIBLE);
        } else {
            holder.tvVariant.setVisibility(View.GONE);
        }

        // Xử lý sự kiện
        holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(item));
        holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onItemCheckedChanged(item, isChecked));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnDelete,btnIncrease, btnDecrease;
        TextView tvName, tvPrice, tvQuantity, tvVariant;
        CheckBox checkboxItem;


        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvVariant = itemView.findViewById(R.id.tvVariant);
        }
    }
}
