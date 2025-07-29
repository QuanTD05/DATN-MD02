package com.example.datn_md02.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.R;
import com.example.datn_md02.ReviewActivity;

import java.util.ArrayList;
import java.util.List;

public class NotReviewedAdapter extends RecyclerView.Adapter<NotReviewedAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> list;

    public NotReviewedAdapter(Context context, List<CartItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotReviewedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_not_reviewed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotReviewedAdapter.ViewHolder holder, int position) {
        CartItem item = list.get(position);

        Glide.with(context).load(item.getProductImage()).into(holder.imgProduct);
        holder.tvProductName.setText(item.getProductName());

        if (item.getVariant() != null && !item.getVariant().isEmpty()) {
            holder.tvVariant.setText("Phân loại: " + item.getVariant());
            holder.tvVariant.setVisibility(View.VISIBLE);
        } else {
            holder.tvVariant.setVisibility(View.GONE);
        }

        holder.btnReview.setOnClickListener(v -> {
            ArrayList<CartItem> items = new ArrayList<>();
            items.add(item);

            Intent intent = new Intent(context, ReviewActivity.class);
            intent.putExtra("items", items);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvVariant;
        Button btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}
