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

        loadStaff();
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

    private void loadStaff() {
        usersRef.orderByChild("role").equalTo("staff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        staffList.clear();
                        for (DataSnapshot ds : snap.getChildren()) {
                            User u = ds.getValue(User.class);
                            if (u == null) continue;
                            if (u.getEmail().equalsIgnoreCase(currentEmail)) continue;

                            // init fields
                            u.setLastMessageText("Chưa có tin nhắn");
                            u.setLastMessageTimestamp(0);
                            u.setUnreadCount(0);
                            u.setHasUnread(false);
                            u.setOnline(false);
                            staffList.add(u);

                            // presence listener
                            String key = sanitizeEmail(u.getEmail());
                            FirebaseDatabase.getInstance()
                                    .getReference("status")
                                    .child(key)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot s) {
                                            Boolean on = s.getValue(Boolean.class);
                                            u.setOnline(on != null && on);
                                            adapter.notifyDataSetChanged();
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                                    });
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

        // Gửi số lượng tin nhắn chưa đọc về UserActivity
        if (unreadListener != null) {
            unreadListener.onUnreadMessages(totalUnread);
        }

        Collections.sort(staffList, (a,b) ->
                Long.compare(b.getLastMessageTimestamp(),
                        a.getLastMessageTimestamp()));
        adapter.notifyDataSetChanged();
    }

    private void openChat(User u) {
        // Đánh dấu tất cả tin nhắn từ u -> mình là đã đọc trước khi mở chat
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

                // Sau khi đánh dấu xong thì mở ChatActivity
                Intent it = new Intent(getContext(), ChatActivity.class);
                it.putExtra("partner_email",  u.getEmail());
                it.putExtra("partner_name",   u.getFullName());
                it.putExtra("partner_avatar", u.getAvatar());
                startActivity(it);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String sanitizeEmail(String email) {
        return email.replaceAll("[.#\\$\\[\\]]", ",");
    }
}
