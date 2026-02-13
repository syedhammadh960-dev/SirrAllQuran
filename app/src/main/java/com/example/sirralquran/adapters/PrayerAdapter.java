package com.example.sirralquran.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sirralquran.R;
import com.example.sirralquran.models.Prayer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ULTIMATE FIXED Adapter:
 * 1. Blocks checkbox BEFORE prayer time
 * 2. Blocks long-press (Qaza) BEFORE prayer time
 * 3. Color coding: Qaza (RED), Offered (GREEN), Notification (ORANGE)
 * 4. Clears listener properly (no crash)
 */
public class PrayerAdapter extends RecyclerView.Adapter<PrayerAdapter.ViewHolder> {

    private static final String TAG = "PrayerAdapter";
    private final List<Prayer> prayers;
    private final OnPrayerClickListener listener;

    // COLORS
    private static final int COLOR_QAZA = Color.parseColor("#F44336");      // Red
    private static final int COLOR_OFFERED = Color.parseColor("#4CAF50");   // Green
    private static final int COLOR_NOTIFICATION = Color.parseColor("#FF9800"); // Orange
    private static final int COLOR_DEFAULT = Color.parseColor("#666666");   // Gray

    public PrayerAdapter(List<Prayer> prayers, OnPrayerClickListener listener) {
        this.prayers = prayers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prayer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prayer prayer = prayers.get(position);

        // Set prayer names
        holder.prayerNameText.setText(prayer.getName());
        holder.prayerNameArabicText.setText(prayer.getNameArabic());

        // Build time display with color
        TimeDisplayResult result = buildTimeDisplayWithColor(prayer);
        holder.prayerTimeText.setText(result.text);
        holder.prayerTimeText.setTextColor(result.color);

        // Check if prayer time has arrived
        boolean timeArrived = prayer.hasPrayerTimeArrived();

        // CRITICAL: Clear listener FIRST
        holder.prayerCheckBox.setOnCheckedChangeListener(null);

        // Enable/disable checkbox (only after time OR if already Qaza)
        holder.prayerCheckBox.setEnabled(timeArrived || prayer.isQaza());
        holder.prayerCheckBox.setChecked(prayer.isCompleted());
        holder.prayerCheckBox.setAlpha((timeArrived || prayer.isQaza()) ? 1.0f : 0.5f);

        // Set checkbox listener
        holder.prayerCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && (timeArrived || prayer.isQaza())) {
                Log.d(TAG, "Checkbox changed: " + prayer.getName() + " = " + isChecked);
                listener.onPrayerChecked(prayer, isChecked);
            }
        });

        // Show/hide "Offered" badge
        if (prayer.isCompleted()) {
            holder.offeredBadge.setVisibility(View.VISIBLE);
        } else {
            holder.offeredBadge.setVisibility(View.GONE);
        }

        // Show/hide notification bell
        if (prayer.hasNotification() && !prayer.isCompleted() && !prayer.isQaza()) {
            holder.notificationIcon.setVisibility(View.VISIBLE);
            holder.notificationIcon.setEnabled(true);
            holder.notificationIcon.setAlpha(1.0f);
        } else if (prayer.isCompleted() || prayer.isQaza()) {
            holder.notificationIcon.setVisibility(View.VISIBLE);
            holder.notificationIcon.setEnabled(false);
            holder.notificationIcon.setAlpha(0.3f);
        } else {
            holder.notificationIcon.setVisibility(View.GONE);
        }

        // CRITICAL: Long click ONLY works if time has arrived
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && timeArrived) {
                listener.onPrayerLongClick(prayer);
                return true;
            }
            // Do nothing if time hasn't arrived
            return false;
        });

        // Bell click
        holder.notificationIcon.setOnClickListener(v -> {
            if (listener != null && !prayer.isCompleted() && !prayer.isQaza()) {
                listener.onNotificationIconClick(prayer, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            // Prevent accidental clicks
        });
    }

    /**
     * Build time display with color based on prayer state
     * Priority: Qaza → Offered → Notification → Default
     */
    private TimeDisplayResult buildTimeDisplayWithColor(Prayer prayer) {
        String baseTime = prayer.getTime();

        // Priority 1: Qaza (RED)
        if (prayer.isQaza()) {
            return new TimeDisplayResult(baseTime + " • QAZA", COLOR_QAZA);
        }

        // Priority 2: Offered (GREEN)
        if (prayer.isCompleted() && prayer.getOfferedTime() != null && !prayer.getOfferedTime().isEmpty()) {
            return new TimeDisplayResult(
                    baseTime + " • Offered: " + prayer.getOfferedTime(),
                    COLOR_OFFERED
            );
        }

        // Priority 3: Notification (ORANGE)
        if (prayer.hasNotification() && !prayer.isCompleted()) {
            String notifTime = calculateNotificationTime(prayer.getTime(), prayer.getNotificationOffset());
            return new TimeDisplayResult(
                    baseTime + " • Notif: " + notifTime,
                    COLOR_NOTIFICATION
            );
        }

        // Priority 4: Default (GRAY)
        return new TimeDisplayResult(baseTime, COLOR_DEFAULT);
    }

    /**
     * Helper class for text + color
     */
    private static class TimeDisplayResult {
        String text;
        int color;

        TimeDisplayResult(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    /**
     * Calculate notification time
     */
    private String calculateNotificationTime(String prayerTime, int offsetMinutes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            Date time = sdf.parse(prayerTime);

            if (time == null) return prayerTime;

            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            cal.add(Calendar.MINUTE, -offsetMinutes);

            return sdf.format(cal.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error calculating notification time: " + e.getMessage());
            return prayerTime;
        }
    }

    @Override
    public int getItemCount() {
        return prayers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView prayerNameText;
        TextView prayerNameArabicText;
        TextView prayerTimeText;
        ImageView notificationIcon;
        LinearLayout offeredBadge;
        CheckBox prayerCheckBox;

        ViewHolder(View itemView) {
            super(itemView);
            prayerNameText = itemView.findViewById(R.id.prayerNameText);
            prayerNameArabicText = itemView.findViewById(R.id.prayerNameArabicText);
            prayerTimeText = itemView.findViewById(R.id.prayerTimeText);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            offeredBadge = itemView.findViewById(R.id.offeredBadge);
            prayerCheckBox = itemView.findViewById(R.id.prayerCheckBox);
        }
    }

    public interface OnPrayerClickListener {
        void onPrayerChecked(Prayer prayer, boolean isChecked);
        void onPrayerLongClick(Prayer prayer);
        void onNotificationIconClick(Prayer prayer, int position);
    }
}