package com.example.countdowntimer.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EventHandlerAlarm {

    private final String TAG = "EVENT_HANDLER_ALARM";
    private final Context context;

    public EventHandlerAlarm(Context context) {
        this.context = context;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleEventExecution(String eventName, long targetMills) {
        try {
            Intent notificationIntent = new Intent(context, EventHandlerReceiver.class);
            notificationIntent.putExtra("NAME", eventName);
            PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (eventName + "final").hashCode(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMills,
                    notificationPendingIntent
            );
            Log.d(TAG, "Event Alarm is set");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in schedule notification: " + e);
        }
    }
}
