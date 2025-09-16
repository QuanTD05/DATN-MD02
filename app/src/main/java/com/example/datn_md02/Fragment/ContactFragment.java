package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactFragment extends Fragment {

    public interface OnMessageUnreadCountListener {
        void onUnreadMessages(int count);
    }

    private RecyclerView rvStaff;
    private StaffAdapter adapter;
    private final List<User> staffList = new ArrayList<>();
    private final String currentEmail = FirebaseAuth.getInstance()
            .getCurrentUser().getEmail();

    private DatabaseReference usersRef, chatsRef, userChatsRef, staffChatsRef;
    private DataSnapshot lastChatSnapshot = null;
    private OnMessageUnreadCountListener unreadListener;

    public static ContactFragment newInstance(OnMessageUnreadCountListener listener) {
        ContactFragment f = new ContactFragment();
        f.unreadListener = listener;
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        rvStaff = view.findViewById(R.id.rvStaff);
        rvStaff.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffAdapter(staffList, getContext(), this::onStaffClick);
        rvStaff.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        userChatsRef = FirebaseDatabase.getInstance().getReference("userChats");
        staffChatsRef = FirebaseDatabase.getInstance().getReference("staffChats");

        loadStaffRealtime();

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                lastChatSnapshot = snap;
                updateWithChatData(snap);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {
                Log.e("ContactFragment", "Chat listener failed", e.toException());
            }
        });

        return view;
    }

    /** Xử lý khi user click chọn 1 staff */
    private void onStaffClick(User staff) {
        String userKey = sanitizeEmail(currentEmail);
        String staffKey = sanitizeEmail(staff.getEmail());

        userChatsRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String assignedStaff = snapshot.getValue(String.class);

                if (assignedStaff == null) {
                    // Chưa gán -> kiểm tra staff này đã đủ 5 user chưa
                    staffChatsRef.child(staffKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snap) {
                            long count = snap.getChildrenCount();
                            if (count >= 5) {
                                Toast.makeText(getContext(),
                                        "Nhân viên này đang hỗ trợ tối đa khách hàng, vui lòng chọn nhân viên khác.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                // Gán staff cho user
                                userChatsRef.child(userKey).setValue(staff.getEmail());
                                staffChatsRef.child(staffKey).child(userKey).setValue(true);
                                openChat(staff);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });

                } else {
                    // Đã gán -> chỉ cho chat với staff đó
                    if (assignedStaff.equalsIgnoreCase(staff.getEmail())) {
                        openChat(staff);
                    } else {
                        Toast.makeText(getContext(),
                                "Bạn đã được gán cho nhân viên khác để hỗ trợ.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStaffRealtime() {
        usersRef.orderByChild("role").equalTo("staff")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        staffList.clear();

                        for (DataSnapshot ds : snap.getChildren()) {
                            User u = ds.getValue(User.class);
                            if (u == null) continue;

                            Object statusVal = ds.child("status").getValue();
                            boolean isOnline = false;
                            if (statusVal instanceof String) {
                                isOnline = "online".equalsIgnoreCase((String) statusVal);
                            } else if (statusVal instanceof Boolean) {
                                isOnline = (Boolean) statusVal;
                            }
                            u.setOnline(isOnline);

                            u.setLastMessageText("Chưa có tin nhắn");
                            u.setLastMessageTimestamp(0);
                            u.setUnreadCount(0);
                            u.setHasUnread(false);

                            staffList.add(u);
                        }

                        if (lastChatSnapshot != null) {
                            updateWithChatData(lastChatSnapshot);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        Log.e("ContactFragment", "Users load failed", e.toException());
                    }
                });
    }

    private void updateWithChatData(DataSnapshot snap) {
        int totalUnread = 0;
        for (User u : staffList) {
            long lastTs = 0;
            int unread = 0;
            Message lastMsg = null;

            for (DataSnapshot c : snap.getChildren()) {
                Message m = c.getValue(Message.class);
                if (m == null) continue;

                boolean between =
                        (m.getSender().equalsIgnoreCase(currentEmail)
                                && m.getReceiver().equalsIgnoreCase(u.getEmail()))
                                || (m.getSender().equalsIgnoreCase(u.getEmail())
                                && m.getReceiver().equalsIgnoreCase(currentEmail));
                if (!between) continue;

                if (m.getTimestamp() > lastTs) {
                    lastTs = m.getTimestamp();
                    lastMsg = m;
                }
                if (m.getSender().equalsIgnoreCase(u.getEmail()) && !m.isSeen()) {
                    unread++;
                }
            }

            u.setLastMessageTimestamp(lastTs);
            u.setUnreadCount(unread);
            u.setHasUnread(unread > 0);
            if (lastMsg != null) {
                String prefix = lastMsg.getSender()
                        .equalsIgnoreCase(currentEmail)
                        ? "Bạn: " : "";
                u.setLastMessageText(prefix + lastMsg.getContent());
            }
            totalUnread += unread;
        }

        if (unreadListener != null) {
            unreadListener.onUnreadMessages(totalUnread);
        }

        Collections.sort(staffList, (a, b) ->
                Long.compare(b.getLastMessageTimestamp(),
                        a.getLastMessageTimestamp()));

        adapter.notifyDataSetChanged();
    }

    private void openChat(User staff) {
        // Đánh dấu tin nhắn đã đọc trước khi mở
        chatsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot c : snapshot.getChildren()) {
                Message m = c.getValue(Message.class);
                if (m == null) continue;

                boolean isFromStaffToMe = m.getSender().equalsIgnoreCase(staff.getEmail())
                        && m.getReceiver().equalsIgnoreCase(currentEmail)
                        && !m.isSeen();

                if (isFromStaffToMe) {
                    c.getRef().child("seen").setValue(true);
                }
            }

            Intent it = new Intent(getContext(), ChatActivity.class);
            it.putExtra("partner_email",  staff.getEmail());
            it.putExtra("partner_name",   staff.getFullName());
            it.putExtra("partner_avatar", staff.getAvatar());
            startActivity(it);
        });
    }

    private String sanitizeEmail(String email) {
        return email == null ? "" : email.replaceAll("[.#\\$\\[\\]]", ",");
    }
}
