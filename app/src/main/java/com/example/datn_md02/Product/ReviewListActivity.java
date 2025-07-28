package com.example.datn_md02.Product;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.ReviewAdapter;
import com.example.datn_md02.Model.Product;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ReviewListActivity extends AppCompatActivity {

    private RecyclerView recyclerReviews;
    private TextView tvReviewTitle;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        initView();

        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            tvReviewTitle.setText("Đánh giá cho: " + product.getName());
            loadReviewsFromFirebase(product.getProductId());
        }
    }

    private void initView() {
        recyclerReviews = findViewById(R.id.recyclerReviews);
        tvReviewTitle = findViewById(R.id.tvReviewTitle);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(this, reviewList);
        recyclerReviews.setAdapter(adapter);
    }

    private void loadReviewsFromFirebase(String productId) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(productId);
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                    Review review = reviewSnap.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvReviewTitle.setText("Không tải được đánh giá");
            }
        });
    }
}
