package com.example.sirralquran.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.sirralquran.R;

/**
 * Dialog to select Fiqh (Calculation Method) for prayer times
 */
public class FiqhSelectionDialog extends Dialog {

    private static final String PREFS_NAME = "FiqhPrefs";
    private static final String KEY_FIQH_METHOD = "fiqh_method";
    private static final int DEFAULT_METHOD = 1; // Karachi (Hanafi)

    private final Context context;
    private final OnFiqhSelectedListener listener;
    private SharedPreferences prefs;

    private RadioGroup fiqhRadioGroup;
    private RadioButton method0; // Shia Ithna-Ashari
    private RadioButton method1; // Karachi (Hanafi - Default)
    private RadioButton method2; // Islamic Society of North America
    private RadioButton method3; // Muslim World League
    private RadioButton method4; // Umm Al-Qura University, Makkah
    private RadioButton method5; // Egyptian General Authority of Survey
    private Button selectButton;
    private Button cancelButton;

    public FiqhSelectionDialog(@NonNull Context context, OnFiqhSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fiqh_selection);

        initializeViews();
        loadCurrentSelection();
        setupListeners();
    }

    private void initializeViews() {
        TextView titleText = findViewById(R.id.dialogTitle);
        titleText.setText("Select Prayer Calculation Method");

        fiqhRadioGroup = findViewById(R.id.fiqhRadioGroup);
        method0 = findViewById(R.id.method0);
        method1 = findViewById(R.id.method1);
        method2 = findViewById(R.id.method2);
        method3 = findViewById(R.id.method3);
        method4 = findViewById(R.id.method4);
        method5 = findViewById(R.id.method5);
        selectButton = findViewById(R.id.selectButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void loadCurrentSelection() {
        int currentMethod = prefs.getInt(KEY_FIQH_METHOD, DEFAULT_METHOD);

        switch (currentMethod) {
            case 0:
                method0.setChecked(true);
                break;
            case 1:
                method1.setChecked(true);
                break;
            case 2:
                method2.setChecked(true);
                break;
            case 3:
                method3.setChecked(true);
                break;
            case 4:
                method4.setChecked(true);
                break;
            case 5:
                method5.setChecked(true);
                break;
            default:
                method1.setChecked(true);
        }
    }

    private void setupListeners() {
        selectButton.setOnClickListener(v -> {
            int selectedMethod = getSelectedMethod();

            // Save selection
            prefs.edit().putInt(KEY_FIQH_METHOD, selectedMethod).apply();

            // Callback
            if (listener != null) {
                listener.onFiqhSelected(selectedMethod, getMethodName(selectedMethod));
            }

            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private int getSelectedMethod() {
        int selectedId = fiqhRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.method0) {
            return 0;
        } else if (selectedId == R.id.method1) {
            return 1;
        } else if (selectedId == R.id.method2) {
            return 2;
        } else if (selectedId == R.id.method3) {
            return 3;
        } else if (selectedId == R.id.method4) {
            return 4;
        } else if (selectedId == R.id.method5) {
            return 5;
        }

        return DEFAULT_METHOD;
    }

    private String getMethodName(int method) {
        switch (method) {
            case 0: return "Shia Ithna-Ashari";
            case 1: return "Karachi (Hanafi)";
            case 2: return "ISNA";
            case 3: return "Muslim World League";
            case 4: return "Umm Al-Qura";
            case 5: return "Egyptian";
            default: return "Karachi (Hanafi)";
        }
    }

    /**
     * Get saved calculation method
     */
    public static int getSavedMethod(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_FIQH_METHOD, DEFAULT_METHOD);
    }

    /**
     * Get saved method name
     */
    public static String getSavedMethodName(Context context) {
        int method = getSavedMethod(context);
        switch (method) {
            case 0: return "Shia Ithna-Ashari";
            case 1: return "Karachi (Hanafi)";
            case 2: return "ISNA";
            case 3: return "Muslim World League";
            case 4: return "Umm Al-Qura";
            case 5: return "Egyptian";
            default: return "Karachi (Hanafi)";
        }
    }

    public interface OnFiqhSelectedListener {
        void onFiqhSelected(int method, String methodName);
    }
}