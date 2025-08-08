// File: com/example/datn_md02/Util/BgInit.java
package com.example.datn_md02.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.datn_md02.service.MessageForegroundService;
import com.example.datn_md02.work.PollUnreadWorker;


import java.util.concurrent.TimeUnit;

public class BgInit {
    private static final String WORK_NAME = "poll_unread_worker";

    public static void startAll(Context ctx) {
        // 1) Start Foreground Service (Android 8+)
        Intent svc = new Intent(ctx, MessageForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(ctx, svc);
        } else {
            ctx.startService(svc);
        }

        // 2) Enqueue WorkManager (min 15 phút)
        Constraints cons = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                PollUnreadWorker.class,
                15, TimeUnit.MINUTES
        ).setConstraints(cons).build();

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                req
        );
    }
}
