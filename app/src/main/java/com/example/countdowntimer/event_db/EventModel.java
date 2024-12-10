package com.example.countdowntimer.event_db;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "events")
public class EventModel {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "eventName")
    private String eventName;

    @ColumnInfo(name = "eventDescription")
    private String eventDescription;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    @ColumnInfo(name = "eventDate")
    private String eventDate;

    @ColumnInfo(name = "eventTime")
    private String eventTime;

    @ColumnInfo(name = "eventImage")
    private String eventImage;

    public EventModel(String eventName, String eventDescription, String eventDate, String eventTime, String eventImage) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventImage = eventImage;
    }
}
