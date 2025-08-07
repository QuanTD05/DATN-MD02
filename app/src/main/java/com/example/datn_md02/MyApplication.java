package com.example.datn_md02;

import android.app.Application;
import com.pusher.pushnotifications.PushNotifications;

public class MyApplication extends Application {
    private static final String INSTANCE_ID = "c8828f59-ad54-4d42-8f0a-5e46a2b7ed7b"; // your Beams instance ID

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Pusher Beams SDK
        PushNotifications.start(getApplicationContext(), INSTANCE_ID);

        // (Optional) subscribe all devices to global promo topic
        PushNotifications.addDeviceInterest("promo_all");
    }
}
