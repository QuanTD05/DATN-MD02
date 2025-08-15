package com.example.datn_md02.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private DatabaseReference usersRef, chatsRef;
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
        adapter = new StaffAdapter(staffList, getContext(), this::openChat);
        rvStaff.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

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

    private void loadStaffRealtime() {
        usersRef.orderByChild("role").equalTo("staff")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        staffList.clear();
                        User currentUserObj = null;

                        for (DataSnapshot ds : snap.getChildren()) {
                            User u = ds.getValue(User.class);
                            if (u == null) continue;

                            // Lấy status từ users
                            Object statusVal = ds.child("status").getValue();
                            boolean isOnline = false;
                            if (statusVal instanceof String) {
                                isOnline = "online".equalsIgnoreCase((String) statusVal);
                            } else if (statusVal instanceof Boolean) {
                                isOnline = (Boolean) statusVal;
                            }
                            u.setOnline(isOnline);

                            // Mặc định tin nhắn
                            u.setLastMessageText("Chưa có tin nhắn");
                            u.setLastMessageTimestamp(0);
                            u.setUnreadCount(0);
                            u.setHasUnread(false);

                            if (u.getEmail().equalsIgnoreCase(currentEmail)) {
                                currentUserObj = u; // lưu lại để đẩy lên đầu
                            } else {
                                staffList.add(u);
                            }
                        }

                        // Đẩy user hiện tại lên đầu danh sách
                        if (currentUserObj != null) {
                            staffList.add(0, currentUserObj);
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

        // Chỉ sắp xếp phần còn lại nếu danh sách > 1 phần tử
        if (staffList.size() > 1) {
            List<User> others = staffList.subList(1, staffList.size());
            Collections.sort(others, (a, b) ->
                    Long.compare(b.getLastMessageTimestamp(),
                            a.getLastMessageTimestamp()));
        }

        adapter.notifyDataSetChanged();
    }


    private void openChat(User u) {
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot c : snapshot.getChildren()) {
                    Message m = c.getValue(Message.class);
                    if (m == null) continue;

                    boolean isFromUserToMe = m.getSender().equalsIgnoreCase(u.getEmail())
                            && m.getReceiver().equalsIgnoreCase(currentEmail)
                            && !m.isSeen();

                    if (isFromUserToMe) {
                        c.getRef().child("seen").setValue(true);
                    }
                }

                Intent it = new Intent(getContext(), ChatActivity.class);
                it.putExtra("partner_email",  u.getEmail());
                it.putExtra("partner_name",   u.getFullName());
                it.putExtra("partner_avatar", u.getAvatar());
                startActivity(it);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
