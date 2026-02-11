package com.example.sirralquran.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sirralquran.R;
import com.example.sirralquran.models.DayLesson;
import java.util.List;

public class DayLessonAdapter extends RecyclerView.Adapter<DayLessonAdapter.DayLessonViewHolder> {

    private List<DayLesson> lessonList;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(DayLesson lesson, int position);
    }

    public DayLessonAdapter(List<DayLesson> lessonList, OnLessonClickListener listener) {
        this.lessonList = lessonList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_lesson, parent, false);
        return new DayLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayLessonViewHolder holder, int position) {
        DayLesson lesson = lessonList.get(position);
        holder.bind(lesson, position);
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    class DayLessonViewHolder extends RecyclerView.ViewHolder {
        private View leftGoldBar;
        private TextView dayNumberBadge;
        private TextView surahInfoText;
        private TextView lessonTitleText;
        private TextView lessonDescriptionText;
        private ImageView completionDot;
        private ImageView lockIcon;

        public DayLessonViewHolder(@NonNull View itemView) {
            super(itemView);
            leftGoldBar = itemView.findViewById(R.id.leftGoldBar);
            dayNumberBadge = itemView.findViewById(R.id.dayNumberBadge);
            surahInfoText = itemView.findViewById(R.id.surahInfoText);
            lessonTitleText = itemView.findViewById(R.id.lessonTitleText);
            lessonDescriptionText = itemView.findViewById(R.id.lessonDescriptionText);
            completionDot = itemView.findViewById(R.id.completionDot);
            lockIcon = itemView.findViewById(R.id.lockIcon);
        }

        public void bind(DayLesson lesson, int position) {
            // Set day number in badge
            dayNumberBadge.setText("Day " + lesson.getDayNumber());

            // Set Surah info
            surahInfoText.setText("Surah " + lesson.getSurahNumber());

            // Set lesson title and description
            lessonTitleText.setText(lesson.getTitle());
            lessonDescriptionText.setText(lesson.getDescription());

            // Handle locked/unlocked/completed states
            if (lesson.isLocked()) {
                // Locked day
                leftGoldBar.setVisibility(View.INVISIBLE);
                completionDot.setVisibility(View.GONE);
                lockIcon.setVisibility(View.VISIBLE);
                itemView.setAlpha(0.6f);
                itemView.setEnabled(false);
            } else if (lesson.isCompleted()) {
                // Completed day
                leftGoldBar.setVisibility(View.VISIBLE);
                completionDot.setVisibility(View.VISIBLE);
                lockIcon.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                itemView.setEnabled(true);
            } else {
                // Unlocked but not completed
                leftGoldBar.setVisibility(View.VISIBLE);
                completionDot.setVisibility(View.GONE);
                lockIcon.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                itemView.setEnabled(true);
            }

            // Handle click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && !lesson.isLocked()) {
                        listener.onLessonClick(lesson, position);
                    }
                }
            });
        }
    }

    public void updateLessonCompletion(int position, boolean completed) {
        if (position >= 0 && position < lessonList.size()) {
            lessonList.get(position).setCompleted(completed);
            notifyItemChanged(position);
        }
    }

    public void updateData(List<DayLesson> newLessonList) {
        this.lessonList = newLessonList;
        notifyDataSetChanged();
    }
}