package com.example.autocallresponder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class MissedCallObserver extends ContentObserver {
    private static final String TAG = "MissedCallObserver";

    private final Context context;
    private long lastHandledCallTime;   
    private boolean isProcessing = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MissedCallObserver(Context ctx, Handler handler) {
        super(handler);
        this.context = ctx.getApplicationContext();
        this.lastHandledCallTime = System.currentTimeMillis(); // Ignore history
    }

    @Override
    public void onChange(boolean selfChange) {
        handleCallLogChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        handleCallLogChange(selfChange, uri);
    }

    private void handleCallLogChange(boolean selfChange, Uri uri) {
        if (isProcessing) return;
        isProcessing = true;
        Log.d(TAG, "Call log changed, checking in 2500ms");
        mainHandler.postDelayed(() -> {
            try {
                checkMissedCallSafe();
            } finally {
                isProcessing = false;
            }
        }, 2500);
    }

    private void checkMissedCallSafe() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_CALL_LOG permission not granted");
            return;
        }
        if (!AutoReplyConsent.isEnabled(context)) {
            Log.w(TAG, "Auto-reply consent disabled");
            return;
        }
        ModeManager.Mode mode = ModeManager.getMode(context);
        if (mode == ModeManager.Mode.NONE) {
            Log.w(TAG, "Mode is NONE");
            return;
        }
        try (Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE},
                CallLog.Calls.TYPE + "=?",
                new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE)},
                CallLog.Calls.DATE + " DESC")) {

            if (cursor != null && cursor.moveToFirst()) {
                long callTime = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                if (callTime <= lastHandledCallTime) {
                    Log.d(TAG, "Most recent missed call already handled (callTime=" + callTime + ", last=" + lastHandledCallTime + ")");
                    return;
                }
                String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                if (TextUtils.isEmpty(number) || "-1".equals(number)) {
                    Log.w(TAG, "Skipping: empty or restricted number");
                    return;
                }
                lastHandledCallTime = callTime;
                String statusOrMessage = ModeManager.getStatusMessage(context);
                String message = "This is a Machine Generated Message : The person you are trying to contact is "
                        + statusOrMessage + " and will call you back soon.";
                Log.i(TAG, "Sending auto-reply to " + number);
                SmsHelper.send(context, number, message);
            } else {
                Log.d(TAG, "No missed calls in cursor");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking missed call", e);
        }
    }
}
