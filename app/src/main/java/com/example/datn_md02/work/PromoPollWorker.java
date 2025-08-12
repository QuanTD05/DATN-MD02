package com.example.datn_md02.work;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.datn_md02.Util.NotificationUtils;
import com.google.firebase.database.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PromoPollWorker extends Worker {
    public static final String UNIQUE_NAME = "promo_poll_worker";

    public PromoPollWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        NotificationUtils.ensureChannels(getApplicationContext());

        // đọc /admin_promotions -> lấy item mới nhất 30 phút gần đây
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("admin_promotions");
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] hasNew = {false};
        final String[] title = {"🎁 Khuyến mãi mới"};
        final String[] body  = {""};

        ref.orderByChild("timestamp").limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long threshold = System.currentTimeMillis() - 30 * 60 * 1000L;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Long ts = ds.child("timestamp").getValue(Long.class);
                            String t  = ds.child("title").getValue(String.class);
                            String m  = ds.child("message").getValue(String.class);
                            if (ts != null && ts >= threshold) {
                                hasNew[0] = true;
                                if (t != null) title[0] = t;
                                if (m != null) body[0]  = m;
                                break;
                            }
                        }
                        latch.countDown();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { latch.countDown(); }
                });

        try { latch.await(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}

        if (hasNew[0]) {
            Notification n = NotificationUtils
                    .buildIncoming(getApplicationContext(),
                            title[0], body[0], "myapp://promotions")
                    .build();
            ((NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify((int) System.currentTimeMillis(), n);
        }
        return Result.success();
    }

    public static void schedule(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                PromoPollWorker.class, 30, TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, req
        );
    }

    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelUniqueWork(UNIQUE_NAME);
    }
}
