package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.datn_md02.R;

public class NotificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        LinearLayout notificationContainer = view.findViewById(R.id.container_notification);

        // Tạo 10 thông báo fix cứng
        for (int i = 1; i <= 10; i++) {
            View itemView = inflater.inflate(R.layout.item_notification, notificationContainer, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_title);
            TextView tvDescription = itemView.findViewById(R.id.tv_description);
            TextView tvTime = itemView.findViewById(R.id.tv_time);

            // Gán nội dung tùy ý
            tvTitle.setText("Thông báo #" + i);
            tvDescription.setText("Đây là nội dung thông báo số " + i);
            tvTime.setText(i + " phút trước");

            notificationContainer.addView(itemView);
        }

        return view;
    }
}
