// File: com/example/datn_md02/service/MessageForegroundService.java
package com.example.datn_md02.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.datn_md02.Model.Message;
import com.example.datn_md02.Util.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MessageForegroundService extends Service {
    private static final int FG_ID = 4441;
    private static final String TAG = "MsgFG";

    private DatabaseReference chatsRef;
    private ValueEventListener listener;
    private String meEmail;
    private long lastTs = 0L;

    @Override public void onCreate() {
        super.onCreate();

        // Bắt buộc: tạo channel trước khi startForeground (NotificationUtils tự ensure)
        NotificationUtils.ensureChannels(this);
        Notification ongoing = NotificationUtils
                .buildOngoing(this, "Đang lắng nghe tin nhắn…")
                .build();
        startForeground(FG_ID, ongoing); // gọi trong 5s đầu

        meEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (TextUtils.isEmpty(meEmail)) {
            Log.w(TAG, "No user logged in, stop service");
            stopSelf();
            return;
        }

        // Chỉ nghe tin NHẬN (receiver = meEmail)
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        Query q = chatsRef.orderByChild("receiver").equalTo(meEmail);

        listener = new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String sender = ds.child("sender").getValue(String.class);
                    String content = ds.child("content").getValue(String.class);
                    Boolean isImage = ds.child("image").getValue(Boolean.class);
                    Long ts = ds.child("timestamp").getValue(Long.class);
                    if (ts == null || sender == null) continue;

                    if (ts > lastTs) {
                        lastTs = ts;

                        String shown = Boolean.TRUE.equals(isImage) ? "[Hình ảnh]" :
                                (content != null ? content : "");
                        // deep link mở ChatActivity với peer
                        String peerName = sender.contains("@") ? sender.substring(0, sender.indexOf('@')) : sender;
                        String deep = "myapp://chat?peerId=" + sanitize(sender)
                                + "&peerName=" + peerName;

                        Notification n = NotificationUtils
                                .buildIncoming(MessageForegroundService.this, sender, shown, deep)
                                .build();
                        ((android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                .notify((int) System.currentTimeMillis(), n);
                    }
                }
            }
            @Override public void onCancelled(DatabaseError error) {
                Log.e(TAG, "listen cancelled: " + error);
            }
        };
        q.addValueEventListener(listener);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // giữ sống tối đa có thể
    }

    @Override public void onDestroy() {
        if (chatsRef != null && listener != null) chatsRef.removeEventListener(listener);
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    private String sanitize(String e) { return e == null ? "" : e.replaceAll("[.#\\$\\[\\]]", ","); }
}
