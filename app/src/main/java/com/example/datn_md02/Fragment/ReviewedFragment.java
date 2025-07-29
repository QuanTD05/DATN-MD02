package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.ReviewAdapter;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReviewedFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyReviewed;

    private ReviewAdapter adapter;
    private List<Review> reviewedList = new ArrayList<>();

    private FirebaseUser firebaseUser;
    private DatabaseReference reviewRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviewed, container, false);

        recyclerView = view.findViewById(R.id.recyclerReviewed);
        tvEmptyReviewed = view.findViewById(R.id.tvEmptyReviewed);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReviewAdapter(getContext(), reviewedList);
        recyclerView.setAdapter(adapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        loadMyReviews();

        return view;
    }

    private void loadMyReviews() {
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewedList.clear();

                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    for (DataSnapshot reviewSnap : productSnap.getChildren()) {
                        try {
                            Object value = reviewSnap.getValue();
                            if (value instanceof java.util.Map) {
                                Review review = reviewSnap.getValue(Review.class);
                                if (review != null && firebaseUser.getUid().equals(review.getUserId())) {
                                    reviewedList.add(review);
                                }
                            } else {
                                Log.w("ReviewedFragment", "Bỏ qua dữ liệu không đúng kiểu: " + value);
                            }
                        } catch (Exception e) {
                            Log.e("ReviewedFragment", "Lỗi convert dữ liệu: " + e.getMessage());
                        }
                    }
                }

                // 🔽 Sắp xếp đánh giá mới nhất lên đầu
                reviewedList.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                adapter.setData(reviewedList);
                tvEmptyReviewed.setVisibility(reviewedList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ReviewedFragment", "onCancelled: " + error.getMessage());
            }
        });
    }

}

