package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.StaffAdapter;
import com.example.datn_md02.ChatActivity;
import com.example.datn_md02.Model.Message;
import com.example.datn_md02.Model.User;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactFragment extends Fragment {

    private RecyclerView rvStaff;
    private EditText etSearch;
    private StaffAdapter adapter;
    private final List<User> staffList = new ArrayList<>();
    private final String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    private DatabaseReference staffRef, chatRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        rvStaff = view.findViewById(R.id.rvStaff);
        etSearch = view.findViewById(R.id.etSearch);
        rvStaff.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffAdapter(staffList, staff -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("staff_name", staff.getName());
            intent.putExtra("staff_email", staff.getEmail());
            startActivity(intent);
        });
        rvStaff.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchByName(s.toString());
            }
        });

        staffRef = FirebaseDatabase.getInstance().getReference("users");
        chatRef = FirebaseDatabase.getInstance().getReference("chats");

        loadStaffFromFirebase();

        // Lắng nghe realtime chat thay đổi để cập nhật danh sách staff
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Khi có tin nhắn thay đổi, load lại danh sách staff và cập nhật số tin nhắn chưa đọc + timestamp
                updateStaffWithChatData(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to listen chat changes", error.toException());
            }
        });

        return view;
    }

    private void loadStaffFromFirebase() {
        staffRef.orderByChild("role").equalTo("staff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                staffList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && !user.getEmail().equals(currentEmail)) {
                        // Khởi tạo user với timestamp = 0 và unread = 0 trước
                        user.setTimestamp(0);
                        user.setUnreadCount(0);
                        user.setHasUnread(false);
                        staffList.add(user);
                    }
                }
                adapter.updateList(staffList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load staff", error.toException());
            }
        });
    }

    private void updateStaffWithChatData(DataSnapshot chatSnapshot) {
        // Mỗi lần có thay đổi tin nhắn thì reset dữ liệu về staffList
        // Giữ nguyên staffList hiện tại, cập nhật timestamp và unreadCount

        for (User user : staffList) {
            long latestTime = 0;
            int unreadMessages = 0;

            for (DataSnapshot chat : chatSnapshot.getChildren()) {
                Message msg = chat.getValue(Message.class);
                if (msg == null) continue;

                boolean isBetween = (msg.getSender().equals(currentEmail) && msg.getReceiver().equals(user.getEmail()))
                        || (msg.getSender().equals(user.getEmail()) && msg.getReceiver().equals(currentEmail));

                if (isBetween) {
                    if (msg.getTimestamp() > latestTime) {
                        latestTime = msg.getTimestamp();
                    }
                    if (msg.getSender().equals(user.getEmail()) && !msg.isSeen()) {
                        unreadMessages++;
                    }
                }
            }

            user.setTimestamp(latestTime);
            user.setUnreadCount(unreadMessages);
            user.setHasUnread(unreadMessages > 0);
        }

        // Sắp xếp lại theo timestamp giảm dần
        Collections.sort(staffList, (u1, u2) -> Long.compare(u2.getTimestamp(), u1.getTimestamp()));

        // Cập nhật adapter để refresh giao diện
        adapter.updateList(staffList);
    }
}

