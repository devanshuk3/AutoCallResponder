package com.example.autocallresponder;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private List<RadioButton> radioButtons;
    private Toast modeToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRadioButtons();
        
        // Restore previous state
        ModeManager.Mode currentMode = ModeManager.getMode(this);
        updateRadioButtonsUI(currentMode);
    }

    private void setupRadioButtons() {
        radioButtons = new ArrayList<>();
        RadioButton rbBusy = findViewById(R.id.radioBusy);
        RadioButton rbDriving = findViewById(R.id.radioDriving);
        RadioButton rbSleeping = findViewById(R.id.radioSleeping);
        RadioButton rbTurnOff = findViewById(R.id.radioTurnOff);

        radioButtons.add(rbBusy);
        radioButtons.add(rbDriving);
        radioButtons.add(rbSleeping);
        radioButtons.add(rbTurnOff);

        rbBusy.setOnClickListener(v -> setMode(ModeManager.Mode.BUSY));
        rbDriving.setOnClickListener(v -> setMode(ModeManager.Mode.DRIVING));
        rbSleeping.setOnClickListener(v -> setMode(ModeManager.Mode.SLEEPING));
        rbTurnOff.setOnClickListener(v -> setMode(ModeManager.Mode.NONE));
    }

    private void setMode(ModeManager.Mode mode) {
        ModeManager.setMode(this, mode);
        updateRadioButtonsUI(mode);

        Intent serviceIntent = new Intent(this, ForegroundKeepAliveService.class);
        if (mode != ModeManager.Mode.NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            stopService(serviceIntent);
        }

        showModeToast(mode == ModeManager.Mode.NONE ? "Auto responder OFF" : mode.name() + " mode enabled");
    }

    private void showModeToast(CharSequence message) {
        if (modeToast != null) {
            modeToast.cancel();
        }
        modeToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        modeToast.show();
    }

    private void updateRadioButtonsUI(ModeManager.Mode mode) {
        findViewById(R.id.radioBusy).setSelected(mode == ModeManager.Mode.BUSY);
        ((RadioButton)findViewById(R.id.radioBusy)).setChecked(mode == ModeManager.Mode.BUSY);
        
        findViewById(R.id.radioDriving).setSelected(mode == ModeManager.Mode.DRIVING);
        ((RadioButton)findViewById(R.id.radioDriving)).setChecked(mode == ModeManager.Mode.DRIVING);
        
        findViewById(R.id.radioSleeping).setSelected(mode == ModeManager.Mode.SLEEPING);
        ((RadioButton)findViewById(R.id.radioSleeping)).setChecked(mode == ModeManager.Mode.SLEEPING);
        
        findViewById(R.id.radioTurnOff).setSelected(mode == ModeManager.Mode.NONE);
        ((RadioButton)findViewById(R.id.radioTurnOff)).setChecked(mode == ModeManager.Mode.NONE);
    }
}