package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.NotReviewedAdapter;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.Order;
import com.example.datn_md02.Model.Review;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class NotReviewedFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FirebaseUser firebaseUser;
    private DatabaseReference orderRef, reviewRef;

    private final List<CartItem> notReviewedItems = new ArrayList<>();
    private NotReviewedAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_not_reviewed, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotReviewed);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(firebaseUser.getUid());
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotReviewedAdapter(getContext(), notReviewedItems);
        recyclerView.setAdapter(adapter);

        loadCompletedOrders();
        return view;
    }

    private void loadCompletedOrders() {
        orderRef.orderByChild("status").equalTo("completed")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notReviewedItems.clear();
                        if (!snapshot.exists()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order order = orderSnap.getValue(Order.class);
                            if (order == null || order.getItems() == null) continue;

                            for (CartItem item : order.getItems()) {
                                checkIfReviewed(item);
                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        tvEmpty.setText("Lỗi khi tải dữ liệu.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkIfReviewed(CartItem item) {
        if (item.getProductId() == null) return;

        reviewRef.child(item.getProductId())
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean reviewed = false;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Review review = snap.getValue(Review.class);
                            if (review != null && review.getProductName().equals(item.getProductName())) {
                                reviewed = true;
                                break;
                            }
                        }

                        if (!reviewed) {
                            notReviewedItems.add(item);
                            adapter.notifyDataSetChanged();
                        }

                        tvEmpty.setVisibility(notReviewedItems.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
