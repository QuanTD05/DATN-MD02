package com.example.datn_md02.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.datn_md02.Fragment.NotReviewedFragment;
import com.example.datn_md02.Fragment.ReviewedFragment;

public class MyReviewPagerAdapter extends FragmentStateAdapter {

    public MyReviewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new NotReviewedFragment();
        else return new ReviewedFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
