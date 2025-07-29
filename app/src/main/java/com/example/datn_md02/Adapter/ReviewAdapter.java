package com.example.datn_md02.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    public void setData(List<Review> list) {
        this.reviewList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        if (review == null) return;

        // Avatar người dùng
        Glide.with(context)
                .load(review.getUserAvatar())
                .placeholder(R.drawable.ic_user)
                .into(holder.imgAvatar);

        // Tên người đánh giá
        holder.tvReviewerName.setText(review.getUserName());

        // Nội dung đánh giá
        holder.tvReviewContent.setText(review.getComment());

        // Số sao (★★★★★)
        holder.tvRating.setText(getStarText((int) review.getRating()));

        // Ngày đánh giá
        holder.tvReviewTime.setText(
                new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        .format(new Date(review.getTimestamp()))
        );

        // Tên sản phẩm + ảnh
        holder.tvProductName.setText(review.getProductName());
        Glide.with(context)
                .load(review.getProductImage())
                .placeholder(R.drawable.sample)
                .into(holder.imgProductSmall);

        // Biến thể: Màu - Size
        String color = review.getVariantColor() != null ? review.getVariantColor() : "";
        String size = review.getVariantSize() != null ? review.getVariantSize() : "";

        String variantText = "";
        if (!color.isEmpty()) variantText += "Màu: " + color;
        if (!size.isEmpty()) {
            if (!variantText.isEmpty()) variantText += " - ";
            variantText += "Size: " + size;
        }

        holder.tvVariant.setText("Phân loại: " + (variantText.isEmpty() ? "Không có" : variantText));

        // Ảnh đính kèm nếu có
        if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
            holder.recyclerImages.setVisibility(View.VISIBLE);
            ReviewImageAdapter imageAdapter = new ReviewImageAdapter(context, review.getImageUrls());
            holder.recyclerImages.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
            holder.recyclerImages.setAdapter(imageAdapter);
        } else {
            holder.recyclerImages.setVisibility(View.GONE);
        }
    }

    private String getStarText(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgProductSmall;
        TextView tvReviewerName, tvRating, tvReviewContent, tvProductName, tvReviewTime, tvVariant;
        RecyclerView recyclerImages;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            recyclerImages = itemView.findViewById(R.id.recyclerImages);
            imgProductSmall = itemView.findViewById(R.id.imgProductSmall);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvVariant = itemView.findViewById(R.id.tvVariant); // mới thêm
        }
    }
}
