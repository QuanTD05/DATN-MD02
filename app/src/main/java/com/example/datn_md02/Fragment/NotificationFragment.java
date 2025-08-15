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
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationFragment extends Fragment implements OnNotificationCountChangeListener {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private int promoCount = 0;
    private int orderCount = 0;
    private TextView tvCartBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Bind views
        tabLayout = view.findViewById(R.id.tabLayoutNoti);
        viewPager = view.findViewById(R.id.viewPagerNoti);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);

        // Setup adapter
        viewPager.setAdapter(new NotificationPagerAdapter(requireActivity(), this));

        // Cart icon click
        ImageView ivCart = view.findViewById(R.id.ivCartIcon);
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        // Tab titles
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getPromoTabTitle());
            } else if (position == 1) {
                tab.setText(getOrderTabTitle());
            }
        }).attach();

        // Mark all notifications as read
        markAllNotificationsAsRead();

        // Load cart count
        loadCartCount();

        return view;
    }

    private String getPromoTabTitle() {
        return "Khuyến mãi (" + promoCount + ")";
    }

    private String getOrderTabTitle() {
        return "Đơn hàng (" + orderCount + ")";
    }

    @Override
    public void onCountChanged(int position, int count) {
        if (position == 0) {
            promoCount = count;
        } else if (position == 1) {
            orderCount = count;
        }

        if (tabLayout.getTabCount() >= 2) {
            if (position == 0 && tabLayout.getTabAt(0) != null) {
                tabLayout.getTabAt(0).setText(getPromoTabTitle());
            } else if (position == 1 && tabLayout.getTabAt(1) != null) {
                tabLayout.getTabAt(1).setText(getOrderTabTitle());
            }
        }
    }

    private void markAllNotificationsAsRead() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Mark read for personal notifications
        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot notiSnap : snapshot.getChildren()) {
                        notiSnap.getRef().child("read").setValue(true);
                    }
                });

        // Mark read for broadcast notifications
        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.hasChild("title")) { // broadcast
                            child.getRef()
                                    .child("read_by")
                                    .child(uid)
                                    .setValue(true);
                        }
                    }
                });
    }

    private void loadCartCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int itemCount = (int) snapshot.getChildrenCount(); // count distinct items

                if (itemCount > 0) {
                    tvCartBadge.setVisibility(View.VISIBLE);
                    tvCartBadge.setText(String.valueOf(itemCount));
                } else {
                    tvCartBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private static class NotificationPagerAdapter extends FragmentStateAdapter {
        private final OnNotificationCountChangeListener listener;

        public NotificationPagerAdapter(@NonNull FragmentActivity fa,
                                        OnNotificationCountChangeListener listener) {
            super(fa);
            this.listener = listener;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return PromotionNotificationFragment.newInstance(listener);
            } else {
                return OrderNotificationFragment.newInstance(listener);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
