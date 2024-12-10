package com.example.countdowntimer.recycler_view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.countdowntimer.R;
import com.example.countdowntimer.event_db.EventModel;
import com.example.countdowntimer.utils.ImageHandler;
import com.example.countdowntimer.utils.TimeHandler;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventModel> modelList;
    private final Context context;

    private final ImageHandler imgHandler = new ImageHandler();
    private final TimeHandler timeHandler = new TimeHandler();

    private final EventRecyclerInterface eventRecyclerInterface;


    private final String TAG = "EVENT_ADAPTER";

    public EventAdapter(List<EventModel> modelList, Context context, EventRecyclerInterface eventRecyclerInterface) {
        this.modelList = modelList;
        this.context = context;
        this.eventRecyclerInterface = eventRecyclerInterface;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_view, parent, false);
        return new EventViewHolder(view, eventRecyclerInterface);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        try {
            holder.eventNameView.setText(modelList.get(position).getEventName());
            holder.eventDescriptionView.setText(modelList.get(position).getEventDescription());
            holder.eventImageView.setImageBitmap(imgHandler.getImageFromPath(context, modelList.get(position).getEventImage()));
            String dateTime = modelList.get(position).getEventDate() + " " + modelList.get(position).getEventTime();
            long milliseconds = timeHandler.getMillis(dateTime);
            new CountDownTimer(milliseconds, 1000) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long millisUntilFinished) {
                    long[] getRemainingTime = timeHandler.getTime(millisUntilFinished);
                    holder.daysView.setText(Long.toString(getRemainingTime[0]));
                    holder.hoursView.setText(Long.toString(getRemainingTime[1]));
                    holder.minutesView.setText(Long.toString(getRemainingTime[2]));
                    holder.secondsView.setText(Long.toString(getRemainingTime[3]));
                }

                @Override
                public void onFinish() {
                    holder.daysView.setText("0");
                    holder.hoursView.setText("0");
                    holder.minutesView.setText("0");
                    holder.secondsView.setText("0");
                }
            }.start();


        } catch (Exception e) {
            Log.e(TAG, "Error occurred in event adapter: " + e);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventNameView;
        private final TextView eventDescriptionView;
        private final TextView daysView;
        private final TextView hoursView;
        private final TextView minutesView;
        private final TextView secondsView;
        private final ImageView eventImageView;

        public EventViewHolder(@NonNull View itemView, EventRecyclerInterface eventRecyclerInterface) {
            super(itemView);
            eventNameView = itemView.findViewById(R.id.event_tv);
            eventDescriptionView = itemView.findViewById(R.id.description_tv);
            daysView = itemView.findViewById(R.id.days_tv);
            hoursView = itemView.findViewById(R.id.hours_tv);
            minutesView = itemView.findViewById(R.id.minutes_tv);
            secondsView = itemView.findViewById(R.id.seconds_tv);
            eventImageView = itemView.findViewById(R.id.event_image);

            itemView.setOnClickListener(v -> {
                if (eventRecyclerInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        eventRecyclerInterface.onEventClick(pos);
                    }
                }
            });
        }

    }
}