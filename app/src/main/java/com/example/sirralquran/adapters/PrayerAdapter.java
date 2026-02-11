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
 * COMPLETE FIXED Adapter with:
 * - Qaza display
 * - Crash prevention
 * - Proper time display
 */
public class PrayerAdapter extends RecyclerView.Adapter<PrayerAdapter.ViewHolder> {

    private static final String TAG = "PrayerAdapter";
    private final List<Prayer> prayers;
    private final OnPrayerClickListener listener;

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

        // Build time display based on state
        String timeDisplay = buildTimeDisplay(prayer);
        holder.prayerTimeText.setText(timeDisplay);

        // Set color based on state
        if (prayer.isQaza()) {
            holder.prayerTimeText.setTextColor(Color.parseColor("#F44336")); // Red for Qaza
        } else if (prayer.isCompleted() && prayer.getOfferedTime() != null) {
            holder.prayerTimeText.setTextColor(Color.parseColor("#4CAF50")); // Green for completed
        } else if (prayer.hasNotification() && !prayer.isCompleted()) {
            holder.prayerTimeText.setTextColor(Color.parseColor("#FF9800")); // Orange for notification
        } else {
            holder.prayerTimeText.setTextColor(Color.parseColor("#666666")); // Gray default
        }

        // Check if prayer time has arrived
        boolean timeArrived = prayer.hasPrayerTimeArrived();

        // Enable/disable checkbox
        holder.prayerCheckBox.setEnabled(timeArrived || prayer.isQaza());
        holder.prayerCheckBox.setChecked(prayer.isCompleted());
        holder.prayerCheckBox.setAlpha((timeArrived || prayer.isQaza()) ? 1.0f : 0.5f);

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

        // Checkbox change listener with null check
        holder.prayerCheckBox.setOnCheckedChangeListener(null); // Clear previous listener
        holder.prayerCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null && (timeArrived || prayer.isQaza())) {
                Log.d(TAG, "Checkbox changed: " + prayer.getName() + " = " + isChecked);
                listener.onPrayerChecked(prayer, isChecked);
            }
        });

        // Long click for Qaza marking
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onPrayerLongClick(prayer);
            }
            return true;
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
     * Build time display string based on prayer state
     */
    private String buildTimeDisplay(Prayer prayer) {
        StringBuilder display = new StringBuilder(prayer.getTime());

        if (prayer.isQaza()) {
            // Show QAZA status
            display.append(" • QAZA");
        } else if (prayer.isCompleted() && prayer.getOfferedTime() != null) {
            // Show offered time
            display.append(" • Offered: ").append(prayer.getOfferedTime());
        } else if (prayer.hasNotification() && !prayer.isCompleted()) {
            // Show notification time
            String notifTime = calculateNotificationTime(prayer.getTime(), prayer.getNotificationOffset());
            display.append(" • Notif: ").append(notifTime);
        }

        return display.toString();
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