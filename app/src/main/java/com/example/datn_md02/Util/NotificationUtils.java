package com.example.datn_md02.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.datn_md02.R;

public final class NotificationUtils {
    public static final String CH_CHAT   = "chat_user_channel";
    public static final String CH_PROMO  = "promo_channel";
    public static final String CH_ONGOING= "ongoing_channel";

    private NotificationUtils() {}

    public static void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;

        NotificationChannel chat = new NotificationChannel(
                CH_CHAT, "Chat (User)", NotificationManager.IMPORTANCE_HIGH);
        chat.setDescription("Thông báo tin nhắn mới");

        NotificationChannel promo = new NotificationChannel(
                CH_PROMO, "Khuyến mãi", NotificationManager.IMPORTANCE_DEFAULT);
        promo.setDescription("Thông báo khuyến mãi mới");

        NotificationChannel ongoing = new NotificationChannel(
                CH_ONGOING, "Nền", NotificationManager.IMPORTANCE_LOW);
        ongoing.setDescription("Dịch vụ chạy nền");

        nm.createNotificationChannel(chat);
        nm.createNotificationChannel(promo);
        nm.createNotificationChannel(ongoing);
    }

    public static NotificationCompat.Builder buildIncoming(Context ctx, String title, String body, String deepLink) {
        PendingIntent pi = PendingIntent.getActivity(
                ctx, (int) System.currentTimeMillis(),
                new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(deepLink)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(ctx, CH_CHAT)
                .setSmallIcon(R.drawable.ic_chat) // make sure this exists
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    public static NotificationCompat.Builder buildPromo(Context ctx, String title, String body, String deepLink) {
        PendingIntent pi = PendingIntent.getActivity(
                ctx, (int) System.currentTimeMillis(),
                new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(deepLink)),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(ctx, CH_PROMO)
                .setSmallIcon(R.drawable.ic_promo) // add a simple bell/megaphone icon
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    public static NotificationCompat.Builder buildOngoing(Context ctx, String text) {
        return new NotificationCompat.Builder(ctx, CH_ONGOING)
                .setSmallIcon(R.drawable.ic_notification) // any small icon
                .setContentTitle("Đang nghe tin nhắn…")
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }
}
