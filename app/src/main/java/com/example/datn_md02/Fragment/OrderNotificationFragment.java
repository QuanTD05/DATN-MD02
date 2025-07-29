package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.*;
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

import java.util.*;

public class OrderNotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<NotificationItem> orderList = new ArrayList<>();

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

    private void loadOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(userId);
        ref.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    NotificationItem item = child.getValue(NotificationItem.class);
                    if (item != null && "order".equals(item.type)) {
                        orderList.add(item);
                    }
                }
                Collections.reverse(orderList);
                // ✅ Sắp xếp theo timestamp giảm dần (mới nhất lên đầu)
                orderList.sort((a, b) -> {
                    long tsA = 0;
                    long tsB = 0;
                    try {
                        tsA = (a.getTimestamp() instanceof Long) ? (Long) a.getTimestamp() : 0L;
                        tsB = (b.getTimestamp() instanceof Long) ? (Long) b.getTimestamp() : 0L;
                    } catch (Exception e) {
                        // log nếu cần
                    }
                    return Long.compare(tsB, tsA); // mới nhất lên đầu
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}