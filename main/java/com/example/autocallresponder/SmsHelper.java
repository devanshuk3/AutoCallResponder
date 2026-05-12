package com.example.autocallresponder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.os.Build;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class SmsHelper {
    public static void send(Context context, String number, String msg) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w("SMS_HELPER", "No permission");
            return;
        }

        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                @SuppressWarnings("deprecation")
                SmsManager defaultManager = SmsManager.getDefault();
                smsManager = defaultManager;
            }

            if (smsManager != null) {
                ArrayList<String> parts = smsManager.divideMessage(msg);
                if (parts.size() > 1) {
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null);
                } else {
                    smsManager.sendTextMessage(number, null, msg, null, null);
                }
                Log.d("SMS_HELPER", "Sent to " + number);
            } else {
                Log.e("SMS_HELPER", "SmsManager is null");
            }
        } catch (Exception e) {
            Log.e("SMS_HELPER", "Fail", e);
        }
    }
}
