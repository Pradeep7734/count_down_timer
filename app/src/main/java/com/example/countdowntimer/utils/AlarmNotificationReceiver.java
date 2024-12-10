package com.example.countdowntimer.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.countdowntimer.MainActivity;
import com.example.countdowntimer.R;
import com.example.countdowntimer.event_db.EventDao;
import com.example.countdowntimer.event_db.EventDatabase;
import com.example.countdowntimer.event_db.EventModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "ALARM_NOTIFICATION_RECEIVER";
    private static final String CHANNEL_ID = "ALARM_5_MIN_NOTIFICATION";
    private static final int NOTIFICATION_ID = 1000;

    private final ImageHandler imageHandler = new ImageHandler();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (!intent.hasExtra("NAME")) {
                Log.e(TAG, "No event name is present in the intent.");
                return;
            }

            String eventName = intent.getStringExtra("NAME");
            if (eventName == null || eventName.trim().isEmpty()) {
                Log.e(TAG, "Event name is empty or null.");
                return;
            }

            EventDao dao = EventDatabase.getInstance(context).eventDao();

            // Retrieve EventModel asynchronously
            Future<EventModel> future = executor.submit(() -> dao.getEventModel(eventName));
            EventModel eventModel = future.get(); // Blocking call

            if (eventModel == null) {
                Log.e(TAG, "No event found for the provided name: " + eventName);
                return;
            }

            // Create notification channel if needed
            createNotificationChannel(context);

            Intent startMainActivtyIntent = new Intent(context, MainActivity.class);
            startMainActivtyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    startMainActivtyIntent,
                    PendingIntent.FLAG_IMMUTABLE
            );


            // Build and display the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(imageHandler.getImageFromPath(context, eventModel.getEventImage()))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentTitle(eventModel.getEventName())
                    .setContentText("Be ready. Event will occur in a few minutes")
                    .setSubText("Upcoming Event")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error occurred in receiver: ", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null. Unable to create notification channel.");
                return;
            }

            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existingChannel == null) {
                CharSequence name = context.getString(R.string.notification_channel_name);
                String description = context.getString(R.string.notification_channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                notificationManager.createNotificationChannel(channel);
                Log.i(TAG, "Notification channel created: " + CHANNEL_ID);
            } else {
                Log.i(TAG, "Notification channel already exists: " + CHANNEL_ID);
            }
        }
    }
}
