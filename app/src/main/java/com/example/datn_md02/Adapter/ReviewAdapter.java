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
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Tên người đánh giá
        holder.tvReviewerName.setText(review.getUserName());

        // Nội dung đánh giá
        holder.tvReviewContent.setText(review.getComment());

        // Rating dưới dạng sao
        holder.tvRating.setText(getStars(review.getRating()));

        // Thời gian đánh giá
        if (review.getCreatedAt() != null) {
            holder.tvReviewTime.setText(formatTime(review.getCreatedAt()));
        } else {
            holder.tvReviewTime.setText("Không rõ thời gian");
        }

        // Avatar người dùng
        Glide.with(context)
                .load(review.getUserAvatar())
                .placeholder(R.drawable.haha)
                .circleCrop()
                .into(holder.imgAvatar);

        // Tên sản phẩm và ảnh nhỏ (nếu có)
        holder.tvProductName.setText(review.getProductName());
        Glide.with(context)
                .load(review.getProductImage())
                .placeholder(R.drawable.sample)
                .centerCrop()
                .into(holder.imgProductSmall);

        // TODO: Nếu có ảnh đính kèm, bạn có thể xử lý ở đây
        // holder.recyclerImages.setAdapter(...)
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgProductSmall;
        TextView tvReviewerName, tvReviewContent, tvRating, tvReviewTime, tvProductName;
        RecyclerView recyclerImages;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgProductSmall = itemView.findViewById(R.id.imgProductSmall);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            recyclerImages = itemView.findViewById(R.id.recyclerImages);
        }
    }

    private String getStars(double rating) {
        int fullStars = (int) rating;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) stars.append("★");
        for (int i = fullStars; i < 5; i++) stars.append("☆");
        return stars.toString();
    }

    private String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
