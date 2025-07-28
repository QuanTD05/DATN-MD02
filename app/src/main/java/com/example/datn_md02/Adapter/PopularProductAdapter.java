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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class PopularProductAdapter extends RecyclerView.Adapter<PopularProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final ProductAdapter.OnProductClickListener listener;

    public PopularProductAdapter(Context context, List<Product> productList, ProductAdapter.OnProductClickListener listener) {
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f₫", product.getPrice()));

        Glide.with(context)
                .load(product.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.haha)
                .into(holder.imageProduct);

        // ✅ Gọi hàm load rating
        loadAverageRatingFromFirebase(holder, product.getProductId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView tvName, tvPrice, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
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
