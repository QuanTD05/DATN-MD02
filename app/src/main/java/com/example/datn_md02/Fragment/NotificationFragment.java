package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02.Adapter.NotificationPagerAdapter;
import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationFragment extends Fragment {

    private TextView tvCartBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutNoti);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerNoti);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);

        NotificationPagerAdapter adapter = new NotificationPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        ImageView ivCart = view.findViewById(R.id.ivCartIcon);
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Khuyến mãi");
            else tab.setText("Đơn hàng");
        }).attach();

        // 🔹 Gọi hàm load số lượng giỏ hàng
        loadCartCount();

        return view;
    }

    private void loadCartCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int itemCount = (int) snapshot.getChildrenCount(); // 🔹 chỉ đếm số mục

                if (itemCount > 0) {
                    tvCartBadge.setVisibility(View.VISIBLE);
                    tvCartBadge.setText(String.valueOf(itemCount));
                } else {
                    tvCartBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

}
