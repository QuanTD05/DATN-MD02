package com.example.datn_md02.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.Model.Variant;
import com.example.datn_md02.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_popular, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        // Tên sản phẩm
        holder.tvName.setText(product.getName());

        // Giá
        double price = getFirstVariantPrice(product);
        holder.tvPrice.setText(price > 0
                ? String.format(Locale.getDefault(), "%,.0f₫", price)
                : "Chưa có");

        // Thời gian
        holder.tvTime.setText(getTimeAgo(product.getCreated()));

        // Ảnh sản phẩm
        Glide.with(context)
                .load(product.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.haha)
                .error(R.drawable.haha)
                .into(holder.imageProduct);

        // ✅ Đánh giá trung bình từ Firebase
        loadAverageRatingFromFirebase(holder, product.getProductId());

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView tvName, tvPrice, tvRating, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private double getFirstVariantPrice(Product product) {
        if (product == null || product.getVariants() == null) return 0.0;

        for (Map<String, Variant> colorMap : product.getVariants().values()) {
            for (Variant variant : colorMap.values()) {
                if (variant != null && variant.getPrice() > 0) {
                    return variant.getPrice();
                }
            }
        }
        return 0.0;
    }

    private String getTimeAgo(Date created) {
        if (created == null) return "";
        long now = System.currentTimeMillis();
        long diff = now - created.getTime();

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "Vừa xong";
        else if (minutes < 60) return minutes + " phút trước";
        else if (hours < 24) return hours + " giờ trước";
        else return days + " ngày trước";
    }

    private void loadAverageRatingFromFirebase(ViewHolder holder, String productId) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .child(productId);

        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float total = 0f;
                int count = 0;

                for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                    Review review = reviewSnap.getValue(Review.class);
                    if (review != null && review.getRating() > 0) {
                        total += review.getRating();
                        count++;
                    }
                }

                if (count > 0) {
                    float avg = total / count;
                    holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f★ (%d đánh giá)", avg, count));
                } else {
                    holder.tvRating.setText("Chưa có đánh giá");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.tvRating.setText("Lỗi đánh giá");
            }
        });
    }
}
