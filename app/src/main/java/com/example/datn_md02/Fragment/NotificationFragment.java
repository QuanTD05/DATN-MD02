package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationFragment extends Fragment implements OnNotificationCountChangeListener {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private int promoCount = 0;
    private int orderCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        tabLayout = view.findViewById(R.id.tabLayoutNoti);
        viewPager = view.findViewById(R.id.viewPagerNoti);

        viewPager.setAdapter(new NotificationPagerAdapter(requireActivity(), this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(getPromoTabTitle());
            } else if (position == 1) {
                tab.setText(getOrderTabTitle());
            }
        }).attach();

        // Khi mở màn hình thông báo → đánh dấu tất cả là đã đọc
        markAllNotificationsAsRead();

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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Mark read cho thông báo cá nhân
        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot notiSnap : snapshot.getChildren()) {
                        notiSnap.getRef().child("read").setValue(true);
                    }
                });

        // Mark read cho thông báo broadcast
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
