package com.google.firebase.udacity.friendlychat;

import android.app.Application;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.notifications.DefaultNotificationFactory;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AirshipConfigOptions options = new AirshipConfigOptions.Builder()
                .setAnalyticsEnabled(true)
                .setDevelopmentAppKey("O3IrYSvESoiVhtsHAjJtPw")
                .setDevelopmentAppSecret("PK9AVUYnQ0ux2LND_ef2NA")
                .setInProduction(false)
                .setFcmSenderId("739599955218")
                .build();
        UAirship.takeOff(this, options, airship -> {
            DefaultNotificationFactory factory = new DefaultNotificationFactory(getApplicationContext());
            airship.getPushManager().setNotificationFactory(factory);
            airship.getPushManager().setUserNotificationsEnabled(true);
            airship.getPushManager().setPushEnabled(true);
        });
    }
}
