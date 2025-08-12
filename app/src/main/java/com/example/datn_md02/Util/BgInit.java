package com.example.datn_md02.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.example.datn_md02.service.MessageForegroundService;
import com.example.datn_md02.work.PollUnreadWorker;
import com.example.datn_md02.work.PromoPollWorker;

public class BgInit {
    public static void startAll(Context ctx) {
        Intent svc = new Intent(ctx, MessageForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(ctx, svc);
        } else {
            ctx.startService(svc);
        }
        PollUnreadWorker.schedule(ctx);
        PromoPollWorker.schedule(ctx);
    }

    public static void stopAll(Context ctx) {
        PollUnreadWorker.cancel(ctx);
        PromoPollWorker.cancel(ctx);
        ctx.stopService(new Intent(ctx, MessageForegroundService.class));
    }
}
