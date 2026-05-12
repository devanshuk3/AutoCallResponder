package com.example.autocallresponder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Disable auto-responder
        ModeManager.setMode(context, ModeManager.Mode.NONE);
        
        // Stop the service
        Intent serviceIntent = new Intent(context, MissedCallService.class);
        context.stopService(serviceIntent);
        
        Toast.makeText(context, "Timer finished: Auto-responder turned off", Toast.LENGTH_LONG).show();
        
        // Refresh UI if activity is running
        Intent refreshIntent = new Intent("com.example.autocallresponder.REFRESH_UI");
        context.sendBroadcast(refreshIntent);
    }
}
