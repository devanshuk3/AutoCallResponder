package com.example.autocallresponder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private final List<MaterialCardView> cards = new ArrayList<>();
    private final List<MaterialCardView> customCards = new ArrayList<>();
    private Toast modeToast;
    private CustomModeRepository customModeRepository;
    private LinearLayout customModesContainer;

    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCardSelection(ModeManager.getMode(context), ModeManager.getCurrentCustomModeId(context));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        customModeRepository = new CustomModeRepository(this);
        customModesContainer = findViewById(R.id.customModesContainer);

        setupCards();
        refreshCustomModeCards();
        updateCardSelection(ModeManager.getMode(this), ModeManager.getCurrentCustomModeId(this));
        setupFab();
        animateEntry();

        if (!AutoReplyConsent.isEnabled(this)) {
            showConsentDialog();
        }

        requestRequiredPermissions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, new IntentFilter("com.example.autocallresponder.REFRESH_UI"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(refreshReceiver, new IntentFilter("com.example.autocallresponder.REFRESH_UI"));
        }

        View githubLink = findViewById(R.id.githubLink);
        if (githubLink != null) {
            githubLink.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/devanshuk3"));
                startActivity(browserIntent);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refreshReceiver);
    }

    // ───────────────────────── CONSENT ─────────────────────────

    private void showConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Automatic Reply Permission")
                .setMessage("This app can automatically send a reply SMS after missed calls.\n\nThis will happen without asking each time.\n\nYou can turn this off anytime.")
                .setCancelable(false)
                .setPositiveButton("Allow", (d, w) -> AutoReplyConsent.enable(this))
                .setNegativeButton("Not now", (d, w) -> AutoReplyConsent.disable(this))
                .show();
    }

    // ───────────────────────── UI SETUP ─────────────────────────

    private void setupCards() {
        MaterialCardView cardBusy = findViewById(R.id.cardBusy);
        MaterialCardView cardDriving = findViewById(R.id.cardDriving);
        MaterialCardView cardSleeping = findViewById(R.id.cardSleeping);
        MaterialCardView cardClass = findViewById(R.id.cardClass);
        MaterialCardView cardOff = findViewById(R.id.cardOff);

        cards.add(cardBusy);
        cards.add(cardDriving);
        cards.add(cardSleeping);
        cards.add(cardClass);
        cards.add(cardOff);

        applyClickAnimation(cardBusy);
        applyClickAnimation(cardDriving);
        applyClickAnimation(cardSleeping);
        applyClickAnimation(cardClass);
        applyClickAnimation(cardOff);

        cardBusy.setOnClickListener(v -> setMode(ModeManager.Mode.BUSY));
        cardDriving.setOnClickListener(v -> setMode(ModeManager.Mode.DRIVING));
        cardSleeping.setOnClickListener(v -> setMode(ModeManager.Mode.SLEEPING));
        cardClass.setOnClickListener(v -> setMode(ModeManager.Mode.IN_CLASS));
        cardOff.setOnClickListener(v -> setMode(ModeManager.Mode.NONE));

        findViewById(R.id.btnTimerBusy).setOnClickListener(v -> showTimerDialog(ModeManager.Mode.BUSY, null));
        findViewById(R.id.btnTimerDriving).setOnClickListener(v -> showTimerDialog(ModeManager.Mode.DRIVING, null));
        findViewById(R.id.btnTimerSleeping).setOnClickListener(v -> showTimerDialog(ModeManager.Mode.SLEEPING, null));
        findViewById(R.id.btnTimerClass).setOnClickListener(v -> showTimerDialog(ModeManager.Mode.IN_CLASS, null));
    }

    private void setupFab() {
        View fab = findViewById(R.id.fabAddCustom);
        fab.setOnClickListener(v -> showAddCustomModeDialog());
    }

    private void refreshCustomModeCards() {
        customModesContainer.removeAllViews();
        customCards.clear();
        for (CustomMode mode : customModeRepository.loadCustomModes()) {
            MaterialCardView card = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.item_custom_mode_card, customModesContainer, false);
            TextView title = card.findViewById(R.id.customModeTitle);
            ImageButton btnDelete = card.findViewById(R.id.btnDeleteMode);
            ImageButton btnTimer = card.findViewById(R.id.btnTimerCustom);

            title.setText(mode.name != null ? mode.name.toUpperCase() : "");
            card.setTag(mode.id);
            applyClickAnimation(card);
            card.setOnClickListener(v -> setCustomMode((String) v.getTag()));

            btnDelete.setOnClickListener(v -> {
                customModeRepository.removeCustomMode(mode.id);
                if (ModeManager.getMode(this) == ModeManager.Mode.CUSTOM && mode.id.equals(ModeManager.getCurrentCustomModeId(this))) {
                    setMode(ModeManager.Mode.NONE);
                }
                refreshCustomModeCards();
                updateCardSelection(ModeManager.getMode(this), ModeManager.getCurrentCustomModeId(this));
                showModeToast("Custom mode \"" + mode.name + "\" deleted");
            });

            btnTimer.setOnClickListener(v -> showTimerDialog(ModeManager.Mode.CUSTOM, mode.id));

            customModesContainer.addView(card);
            customCards.add(card);
        }
    }

    private void showTimerDialog(ModeManager.Mode mode, String customId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_timer, null);
        TextInputEditText editHours = dialogView.findViewById(R.id.editTimerHours);
        TextInputEditText editMinutes = dialogView.findViewById(R.id.editTimerMinutes);
        View btnStart = dialogView.findViewById(R.id.btnStartTimer);
        TextView title = dialogView.findViewById(R.id.timerDialogTitle);

        String modeName = (mode == ModeManager.Mode.CUSTOM) ? customModeRepository.findById(customId).name : mode.name();
        title.setText("Timer for " + modeName);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        btnStart.setOnClickListener(v -> {
            String hInput = editHours.getText().toString();
            String mInput = editMinutes.getText().toString();
            
            int hours = TextUtils.isEmpty(hInput) ? 0 : Integer.parseInt(hInput);
            int minutes = TextUtils.isEmpty(mInput) ? 0 : Integer.parseInt(mInput);
            
            int totalMinutes = (hours * 60) + minutes;
            if (totalMinutes <= 0) return;

            if (mode == ModeManager.Mode.CUSTOM) setCustomMode(customId);
            else setMode(mode);

            startTimer(totalMinutes);
            dialog.dismiss();
            
            String timeText = "";
            if (hours > 0) timeText += hours + "h ";
            if (minutes > 0) timeText += minutes + "m";
            showModeToast("Timer set for " + timeText.trim());
        });

        dialog.show();
    }

    private void startTimer(int minutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = SystemClock.elapsedRealtime() + (minutes * 60000L);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void showAddCustomModeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom_mode, null);
        TextInputEditText editName = dialogView.findViewById(R.id.editModeName);
        TextInputEditText editMessage = dialogView.findViewById(R.id.editReplyMessage);
        View btnAdd = dialogView.findViewById(R.id.btnAddMode);

        editMessage.setText("currently ");
        editMessage.setSelection(editMessage.getText().length());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).setCancelable(true).create();

        btnAdd.setOnClickListener(v -> {
            String name = editName.getText() != null ? editName.getText().toString().trim() : "";
            String message = editMessage.getText() != null ? editMessage.getText().toString().trim() : "";
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter a mode name", Toast.LENGTH_SHORT).show();
                return;
            }   
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this, "Please enter a reply message", Toast.LENGTH_SHORT).show();
                return;
            }
            CustomMode customMode = new CustomMode(name, message);
            customModeRepository.addCustomMode(customMode);
            refreshCustomModeCards();
            updateCardSelection(ModeManager.getMode(this), ModeManager.getCurrentCustomModeId(this));
            dialog.dismiss();
            showModeToast("Custom mode \"" + name + "\" added");
        });

        dialog.show();
    }

    private void applyClickAnimation(MaterialCardView card) {
        card.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(80)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    if (event.getAction() == MotionEvent.ACTION_UP) v.performClick();
                    break;
            }
            return false;
        });
    }

    private void animateEntry() {
        View title = findViewById(R.id.header);
        
        if (title != null) {
            title.setAlpha(0f);
            title.setTranslationY(-24f);
            title.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)   
                    .setStartDelay(100)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        long baseDelay = 200L;
        for (int i = 0; i < cards.size(); i++) {
            View card = cards.get(i);
            card.setAlpha(0f);
            card.setTranslationY(40f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay(baseDelay + i * 80L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void updateCardSelection(ModeManager.Mode selectedMode, String selectedCustomModeId) {
        int selectedId;
        switch (selectedMode) {
            case BUSY: selectedId = R.id.cardBusy; break;
            case DRIVING: selectedId = R.id.cardDriving; break;
            case SLEEPING: selectedId = R.id.cardSleeping; break;
            case IN_CLASS: selectedId = R.id.cardClass; break;
            case NONE: selectedId = R.id.cardOff; break;
            default: selectedId = -1;
        }

        for (MaterialCardView card : cards) {
            boolean checked = (selectedMode != ModeManager.Mode.CUSTOM && card.getId() == selectedId);
            card.setChecked(checked);
            card.setStrokeWidth(checked ? 3 : 0);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.stroke_selected));
            card.setElevation(checked ? 8 : 2);
        }
        for (MaterialCardView card : customCards) {
            Object tag = card.getTag();
            boolean checked = (selectedMode == ModeManager.Mode.CUSTOM && selectedCustomModeId != null && selectedCustomModeId.equals(tag));
            card.setChecked(checked);
            card.setStrokeWidth(checked ? 3 : 0);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.stroke_selected));
            card.setElevation(checked ? 8 : 2);
        }
    }

    private boolean hasRequiredPermissions() {
        boolean hasLogs = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean hasSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasLogs && hasSms && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return hasLogs && hasSms;
    }

    private void requestRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.SEND_SMS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!permissions.isEmpty()) ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    private void setMode(ModeManager.Mode mode) {
        if (!AutoReplyConsent.isEnabled(this)) { showConsentDialog(); return; }
        if (!hasRequiredPermissions()) { requestRequiredPermissions(); return; }
        ModeManager.setMode(this, mode);
        updateCardSelection(mode, null);
        Intent serviceIntent = new Intent(this, MissedCallService.class);
        if (mode != ModeManager.Mode.NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent);
            else startService(serviceIntent);
            showModeToast(mode.name() + " mode enabled");
        } else {
            stopService(serviceIntent);
            showModeToast("Auto-responder disabled");
        }
    }

    private void setCustomMode(String customModeId) {
        if (!AutoReplyConsent.isEnabled(this)) { showConsentDialog(); return; }
        if (!hasRequiredPermissions()) { requestRequiredPermissions(); return; }
        CustomMode custom = customModeRepository.findById(customModeId);
        if (custom == null) return;
        ModeManager.setCustomMode(this, customModeId);
        updateCardSelection(ModeManager.Mode.CUSTOM, customModeId);
        Intent serviceIntent = new Intent(this, MissedCallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent);
        else startService(serviceIntent);
        showModeToast(custom.name + " mode enabled");
    }

    private void showModeToast(CharSequence message) {
        if (modeToast != null) modeToast.cancel();
        modeToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        modeToast.show();
    }
}
