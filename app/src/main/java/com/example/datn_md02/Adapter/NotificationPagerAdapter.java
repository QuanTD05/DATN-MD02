package com.example.datn_md02.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.datn_md02.Fragment.OrderNotificationFragment;
import com.example.datn_md02.Fragment.PromotionNotificationFragment;

public class NotificationPagerAdapter extends FragmentStateAdapter {

    public NotificationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new PromotionNotificationFragment();
        else return new OrderNotificationFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
