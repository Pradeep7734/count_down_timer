package com.example.countdowntimer.event_db;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(EventModel eventModel);

    @Query("SELECT * FROM events")
    List<EventModel> getEvents();

    @Query("SELECT * FROM events WHERE eventName = :eventName")
    EventModel getEventModel(String eventName);

    @Query("DELETE FROM events WHERE eventName = :eventName")
    void deleteEvent(String eventName);




}
