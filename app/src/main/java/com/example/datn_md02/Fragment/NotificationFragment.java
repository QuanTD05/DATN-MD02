package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02.Adapter.NotificationPagerAdapter;
import com.example.datn_md02.Cart.CartActivity;
import com.example.datn_md02.Product.ProductDetailActivity;
import com.example.datn_md02.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class NotificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutNoti);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerNoti);

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

        return view;
    }
}