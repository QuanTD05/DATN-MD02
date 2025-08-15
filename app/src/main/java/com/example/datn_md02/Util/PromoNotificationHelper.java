package com.example.datn_md02.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.datn_md02.R;

public class PromoNotificationHelper {
    public static final String CHANNEL_PROMO = "promo_channel";

    public static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_PROMO, "Khuyến mãi",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Thông báo khuyến mãi mới");
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    public static void showPromo(Context c, String title, String body) {
        ensureChannel(c);

        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse("myapp://promotions"));
        PendingIntent pi = PendingIntent.getActivity(
                c, (int) System.currentTimeMillis(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CHANNEL_PROMO)
                .setSmallIcon(R.drawable.ic_notification) // icon có sẵn
                .setContentTitle(title != null ? title : "Khuyến mãi mới")
                .setContentText(body != null ? body : "")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(c).notify((int) System.currentTimeMillis(), b.build());
    }
}
