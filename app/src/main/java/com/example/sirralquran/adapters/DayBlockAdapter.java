package com.example.sirralquran.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sirralquran.R;
import com.example.sirralquran.models.AshraDay;
import java.util.List;

/**
 * Adapter for displaying day blocks in each Ashra
 */
public class DayBlockAdapter extends RecyclerView.Adapter<DayBlockAdapter.DayBlockViewHolder> {

    private Context context;
    private List<AshraDay> dayList;
    private OnDayClickListener listener;
    private int selectedColor;

    public interface OnDayClickListener {
        void onDayClick(AshraDay day);
    }

    public DayBlockAdapter(Context context, List<AshraDay> dayList, int selectedColor, OnDayClickListener listener) {
        this.context = context;
        this.dayList = dayList;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayBlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_block, parent, false);
        return new DayBlockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayBlockViewHolder holder, int position) {
        AshraDay day = dayList.get(position);
        
        holder.dayNumberText.setText(String.valueOf(day.getDayNumber()));
        holder.dayTitleText.setText(day.getTitle());
        holder.dayDescriptionText.setText(day.getDescription());
        
        // Set circle color
        holder.dayCircleBg.setBackgroundResource(getCircleDrawable());
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    private int getCircleDrawable() {
        switch (selectedColor) {
            case 1:
                return R.drawable.circle_day_gold;
            case 2:
                return R.drawable.circle_day_teal;
            case 3:
                return R.drawable.circle_day_purple;
            default:
                return R.drawable.circle_day_gold;
        }
    }

    public void updateData(List<AshraDay> newDayList, int color) {
        this.dayList = newDayList;
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    static class DayBlockViewHolder extends RecyclerView.ViewHolder {
        View dayCircleBg;
        TextView dayNumberText;
        TextView dayTitleText;
        TextView dayDescriptionText;

        public DayBlockViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCircleBg = itemView.findViewById(R.id.dayCircleBg);
            dayNumberText = itemView.findViewById(R.id.dayNumberText);
            dayTitleText = itemView.findViewById(R.id.dayTitleText);
            dayDescriptionText = itemView.findViewById(R.id.dayDescriptionText);
        }
    }
}
