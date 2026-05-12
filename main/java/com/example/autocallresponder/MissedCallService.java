package com.example.autocallresponder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MissedCallService extends Service {
    private static final String CHANNEL_ID = "missed_call_service";
    private static final int NOTIFICATION_ID = 1;
    private MissedCallObserver observer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();

        observer = new MissedCallObserver(this, new Handler(Looper.getMainLooper()));
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer);
    }

    private void startForegroundService() {
        Notification notification = createNotification();
        // Android 14+ compliance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification() {
        String currentModeName = "None";
        ModeManager.Mode mode = ModeManager.getMode(this);
        if (mode == ModeManager.Mode.CUSTOM) {
            String customId = ModeManager.getCurrentCustomModeId(this);
            CustomMode custom = new CustomModeRepository(this).findById(customId);
            if (custom != null) {
                currentModeName = custom.name;
            }
        } else if (mode != ModeManager.Mode.NONE) {
            currentModeName = mode.name();
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Call Responder Active")
                .setContentText("Current mode: " + currentModeName + " | Monitoring missed calls")
                .setSmallIcon(android.R.drawable.sym_call_missed)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Missed Call Monitor", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Update notification in case the mode changed and service was already running
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (observer != null) getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
