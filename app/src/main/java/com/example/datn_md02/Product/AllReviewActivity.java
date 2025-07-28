package com.example.datn_md02.Product;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.ReviewAdapter;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerReviews = findViewById(R.id.recyclerReviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerReviews.setAdapter(reviewAdapter);

        loadReviews();
    }

    private void loadReviews() {
        FirebaseDatabase.getInstance().getReference("reviews")
                .child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Review review = snap.getValue(Review.class);
                            if (review != null) {
                                reviewList.add(review);
                            }
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AllReviewActivity.this, "Lỗi khi tải đánh giá", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
