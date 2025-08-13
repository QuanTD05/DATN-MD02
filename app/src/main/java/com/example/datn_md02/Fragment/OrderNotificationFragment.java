package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.NotificationAdapter;
import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderNotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<NotificationItem> orderList = new ArrayList<>();
    private ValueEventListener listener;

    private OnNotificationCountChangeListener countListener;
    private int tabPosition = 1; // Tab Đơn hàng

    public static OrderNotificationFragment newInstance(OnNotificationCountChangeListener listener) {
        OrderNotificationFragment fragment = new OrderNotificationFragment();
        fragment.countListener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), orderList);
        recyclerView.setAdapter(adapter);

        loadOrders();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            FirebaseDatabase.getInstance().getReference("notifications").removeEventListener(listener);
        }
    }

    private void loadOrders() {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("notifications");
        String uid = FirebaseAuth.getInstance().getUid();

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                if (uid != null && snapshot.hasChild(uid)) {
                    for (DataSnapshot n : snapshot.child(uid).getChildren()) {
                        NotificationItem item = n.getValue(NotificationItem.class);
                        if (item != null && isOrderNotification(item)) {
                            orderList.add(item);
                        }
                    }
                }

                Collections.reverse(orderList);
                adapter.notifyDataSetChanged();

                if (countListener != null) {
                    countListener.onCountChanged(tabPosition, orderList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải thông báo đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }
        };

        root.addValueEventListener(listener);
    }

    private boolean isOrderNotification(NotificationItem item) {
        if (item.type != null) {
            String t = item.type.toLowerCase();
            return t.contains("order") || t.contains("đơn hàng");
        }
        return false;
    }
}
