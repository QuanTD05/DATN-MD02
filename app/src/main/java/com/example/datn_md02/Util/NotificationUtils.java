package com.example.datn_md02.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.datn_md02.R;

public class NotificationUtils {
    public static final String CHANNEL_ID_FOREGROUND = "msg_foreground";
    public static final String CHANNEL_ID_CHAT = "msg_incoming";

    public static void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm == null) return;

            NotificationChannel fg = new NotificationChannel(
                    CHANNEL_ID_FOREGROUND, "Chat background", NotificationManager.IMPORTANCE_MIN);
            fg.setDescription("Duy trì dịch vụ nền để nhận tin nhắn");
            nm.createNotificationChannel(fg);

            NotificationChannel chat = new NotificationChannel(
                    CHANNEL_ID_CHAT, "Tin nhắn mới", NotificationManager.IMPORTANCE_HIGH);
            chat.setDescription("Thông báo khi có tin nhắn mới");
            nm.createNotificationChannel(chat);
        }
    }

    public static NotificationCompat.Builder buildOngoing(Context ctx, String text) {
        return new NotificationCompat.Builder(ctx, CHANNEL_ID_FOREGROUND)
                .setSmallIcon(R.drawable.ic_chat) // đảm bảo icon tồn tại
                .setContentTitle("Chat đang chạy nền")
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN);
    }

    public static NotificationCompat.Builder buildIncoming(Context ctx, String title, String body, String deepLink) {
        PendingIntent pi = PendingIntent.getActivity(
                ctx,
                (int) System.currentTimeMillis(),
                new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(deepLink)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(ctx, CHANNEL_ID_CHAT)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }
}
