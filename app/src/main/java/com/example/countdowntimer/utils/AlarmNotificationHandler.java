package com.example.countdowntimer.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmNotificationHandler {

    private final Context context;
    private final String TAG = "ALARM_NOTIFICATION_HANDLER";

    public AlarmNotificationHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleNotification(String eventName, long targetMills) {
        try {
            Intent notificationIntent = new Intent(context, AlarmNotificationReceiver.class);
            notificationIntent.putExtra("NAME", eventName);
            PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(
                    context,
                    eventName.hashCode(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMills,
                    notificationPendingIntent
            );
        }
        catch (Exception e){
            Log.e(TAG, "Error occurred in schedule notification: "+e);
        }
    }
}
