package com.example.datn_md02;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.datn_md02.Fragment.ContactFragment;
import com.example.datn_md02.Fragment.HomeFragment;
import com.example.datn_md02.Fragment.NotificationFragment;
import com.example.datn_md02.Fragment.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class UserActivity extends AppCompatActivity {

    private BadgeDrawable badgeNoti;
    private BadgeDrawable badgeMsg;
    private DatabaseReference notiRef;
    private DatabaseReference chatRef;
    private ValueEventListener notiListener;
    private ValueEventListener msgListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Badge cho thông báo
        badgeNoti = bottomNav.getOrCreateBadge(R.id.nav_favorite);
        badgeNoti.setBackgroundColor(getColor(R.color.error));
        badgeNoti.setBadgeTextColor(getColor(android.R.color.white));
        badgeNoti.setVisible(false);

        // Badge cho tin nhắn
        badgeMsg = bottomNav.getOrCreateBadge(R.id.nav_contact);
        badgeMsg.setBackgroundColor(getColor(R.color.error));
        badgeMsg.setBadgeTextColor(getColor(android.R.color.white));
        badgeMsg.setVisible(false);

        // Lấy user hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Tham chiếu tới notifications
        notiRef = FirebaseDatabase.getInstance()
                .getReference("notifications");

        // Tham chiếu tới chats
        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats");

        // Lắng nghe thông báo chưa đọc
        notiListener = notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    // Broadcast cho tất cả
                    if (child.hasChild("title") || child.hasChild("message") || child.hasChild("type")) {
                        Boolean read = child.child("read_by").child(userId).getValue(Boolean.class);
                        if (read == null || !read) {
                            unreadCount++;
                        }
                    }
                    // Thông báo riêng cho user
                    else if (userId.equals(child.getKey())) {
                        for (DataSnapshot noti : child.getChildren()) {
                            Boolean read = noti.child("read").getValue(Boolean.class);
                            if (read == null || !read) {
                                unreadCount++;
                            }
                        }
                    }
                }
                if (unreadCount > 0) {
                    badgeNoti.setVisible(true);
                    badgeNoti.setNumber(unreadCount);
                } else {
                    badgeNoti.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Lắng nghe tin nhắn chưa đọc
        msgListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadMsgCount = 0;
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String sender = msgSnap.child("sender").getValue(String.class);
                    String receiver = msgSnap.child("receiver").getValue(String.class);
                    Boolean seen = msgSnap.child("seen").getValue(Boolean.class);

                    if (receiver != null && receiver.equalsIgnoreCase(userEmail) && (seen == null || !seen)) {
                        unreadMsgCount++;
                    }
                }

                if (unreadMsgCount > 0) {
                    badgeMsg.setVisible(true);
                    badgeMsg.setNumber(unreadMsgCount);
                } else {
                    badgeMsg.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Fragment mặc định
        loadFragment(new HomeFragment());
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_favorite) {
                selectedFragment = new NotificationFragment();
            } else if (itemId == R.id.nav_contact) {
                selectedFragment = new ContactFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                return false;
            }

            loadFragment(selectedFragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notiRef != null && notiListener != null) {
            notiRef.removeEventListener(notiListener);
        }
        if (chatRef != null && msgListener != null) {
            chatRef.removeEventListener(msgListener);
        }
    }
}
