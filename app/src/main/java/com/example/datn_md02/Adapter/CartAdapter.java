package com.example.datn_md02.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.R;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
        void onItemCheckedChanged(CartItem item, boolean isChecked);
    }

    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartActionListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartActionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        if (item == null) return;

        holder.tvProductName.setText(item.getName());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.checkboxItem.setChecked(item.isSelected());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.sample)
                .error(R.drawable.sample)
                .into(holder.imgProduct);

        // Sự kiện nút
        holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(item));
        holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));

        // Sự kiện checkbox chọn sản phẩm
        holder.checkboxItem.setOnCheckedChangeListener(null); // Reset listener tránh lặp lại
        holder.checkboxItem.setChecked(item.isSelected());
        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            listener.onItemCheckedChanged(item, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnDelete;
        TextView tvProductName, tvProductPrice, tvQuantity;
        Button btnIncrease, btnDecrease;
        CheckBox checkboxItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
        }
    }
}
