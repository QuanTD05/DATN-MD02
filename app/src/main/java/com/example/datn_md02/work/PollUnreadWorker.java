package com.example.datn_md02.work;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.datn_md02.Util.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PollUnreadWorker extends Worker {
    private static final String TAG = "PollUnreadWorker";

    public PollUnreadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        NotificationUtils.ensureChannels(getApplicationContext());

        String meEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (TextUtils.isEmpty(meEmail)) return Result.success();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats");
        Query q = ref.orderByChild("receiver").equalTo(meEmail).limitToLast(5);

        CountDownLatch latch = new CountDownLatch(1);
        final int[] newCount = {0};

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                long nowMinus15m = System.currentTimeMillis() - 15 * 60 * 1000L;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String sender = ds.child("sender").getValue(String.class);
                    Long ts = ds.child("timestamp").getValue(Long.class);
                    if (sender == null || ts == null) continue;
                    if (ts >= nowMinus15m) newCount[0]++; // ước lượng mới
                }
                latch.countDown();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { latch.countDown(); }
        });

        try { latch.await(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}

        if (newCount[0] > 0) {
            Notification n = NotificationUtils
                    .buildIncoming(getApplicationContext(),
                            "Bạn có " + newCount[0] + " tin nhắn mới",
                            "Chạm để mở",
                            "myapp://chat")
                    .build();
            ((NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify((int) System.currentTimeMillis(), n);
        }
        Log.d(TAG, "Polled, newCount=" + newCount[0]);
        return Result.success();
    }

    public static PeriodicWorkRequest buildPeriodicRequest() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        return new PeriodicWorkRequest.Builder(
                PollUnreadWorker.class,
                java.time.Duration.ofMinutes(15)
        ).setConstraints(constraints).build();
    }
}
