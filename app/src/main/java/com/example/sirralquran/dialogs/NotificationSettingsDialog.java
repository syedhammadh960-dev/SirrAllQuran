package com.example.sirralquran.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.sirralquran.R;
import com.example.sirralquran.models.Prayer;

/**
 * Dialog to configure notification settings for a prayer
 */
public class NotificationSettingsDialog extends Dialog {

    private final Prayer prayer;
    private final OnSettingsSavedListener listener;

    private CheckBox notificationEnabledCheckbox;
    private RadioGroup offsetRadioGroup;
    private RadioButton offset0Min;
    private RadioButton offset5Min;
    private RadioButton offset10Min;
    private RadioButton offset15Min;
    private RadioButton offset30Min;
    private RadioButton offset60Min;
    private Button saveButton;
    private Button cancelButton;

    public NotificationSettingsDialog(@NonNull Context context, Prayer prayer, OnSettingsSavedListener listener) {
        super(context);
        this.prayer = prayer;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notification_settings);

        initializeViews();
        loadCurrentSettings();
        setupListeners();
    }

    private void initializeViews() {
        TextView titleText = findViewById(R.id.dialogTitle);
        titleText.setText("Notification Settings - " + prayer.getName());

        notificationEnabledCheckbox = findViewById(R.id.notificationEnabledCheckbox);
        offsetRadioGroup = findViewById(R.id.offsetRadioGroup);
        offset0Min = findViewById(R.id.offset0Min);
        offset5Min = findViewById(R.id.offset5Min);
        offset10Min = findViewById(R.id.offset10Min);
        offset15Min = findViewById(R.id.offset15Min);
        offset30Min = findViewById(R.id.offset30Min);
        offset60Min = findViewById(R.id.offset60Min);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void loadCurrentSettings() {
        // Set checkbox
        notificationEnabledCheckbox.setChecked(prayer.hasNotification());

        // Set radio button based on offset
        switch (prayer.getNotificationOffset()) {
            case 0:
                offset0Min.setChecked(true);
                break;
            case 5:
                offset5Min.setChecked(true);
                break;
            case 10:
                offset10Min.setChecked(true);
                break;
            case 15:
                offset15Min.setChecked(true);
                break;
            case 30:
                offset30Min.setChecked(true);
                break;
            case 60:
                offset60Min.setChecked(true);
                break;
            default:
                offset15Min.setChecked(true);
        }

        // Enable/disable radio group based on checkbox
        offsetRadioGroup.setEnabled(prayer.hasNotification());
        for (int i = 0; i < offsetRadioGroup.getChildCount(); i++) {
            offsetRadioGroup.getChildAt(i).setEnabled(prayer.hasNotification());
        }
    }

    private void setupListeners() {
        // Toggle radio group when checkbox changes
        notificationEnabledCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            offsetRadioGroup.setEnabled(isChecked);
            for (int i = 0; i < offsetRadioGroup.getChildCount(); i++) {
                offsetRadioGroup.getChildAt(i).setEnabled(isChecked);
            }
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            // Update prayer settings
            prayer.setHasNotification(notificationEnabledCheckbox.isChecked());

            int selectedId = offsetRadioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.offset0Min) {
                prayer.setNotificationOffset(0);
            } else if (selectedId == R.id.offset5Min) {
                prayer.setNotificationOffset(5);
            } else if (selectedId == R.id.offset10Min) {
                prayer.setNotificationOffset(10);
            } else if (selectedId == R.id.offset15Min) {
                prayer.setNotificationOffset(15);
            } else if (selectedId == R.id.offset30Min) {
                prayer.setNotificationOffset(30);
            } else if (selectedId == R.id.offset60Min) {
                prayer.setNotificationOffset(60);
            }

            // Callback
            if (listener != null) {
                listener.onSettingsSaved(prayer);
            }

            dismiss();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dismiss());
    }

    public interface OnSettingsSavedListener {
        void onSettingsSaved(Prayer prayer);
    }
}