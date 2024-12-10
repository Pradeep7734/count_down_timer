package com.example.countdowntimer.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeHandler {

    private final String TAG = "TIME_HANDLER";

    public long[] getTime(long totalMilliSeconds) {

        long days = totalMilliSeconds / (24 * 3600 * 1000);
        long hours = (totalMilliSeconds % (24 * 3600 * 1000)) / (3600 * 1000);
        long minutes = (totalMilliSeconds % (3600 * 1000)) / (60 * 1000);
        long seconds = (totalMilliSeconds % (60 * 1000)) / 1000;

        return new long[]{days, hours, minutes, seconds};
    }

    public long getMillis(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime target = LocalDateTime.parse(time, formatter);
        LocalDateTime current = LocalDateTime.now();

        Duration duration = Duration.between(current, target);
        return duration.toMillis();
    }

    public String getTimeStamp(long milliseconds) {
        Date date = new Date(milliseconds);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public long get5MinBeforeMillis(String time) {
        long targetTime = getTimeMillis(time);
        long notificationTime = targetTime - (1000 * 60 * 5);
        Log.d(TAG, "Target time: "+getTimeStamp(targetTime));
        Log.d(TAG, "Notification time: "+getTimeStamp(notificationTime));
        return notificationTime;
    }

    public long getTimeMillis(String time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
        Instant instant = zonedDateTime.toInstant();
        return instant.toEpochMilli();
    }



}
