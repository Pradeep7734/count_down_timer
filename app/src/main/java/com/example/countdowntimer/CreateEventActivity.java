package com.example.countdowntimer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.countdowntimer.databinding.ActivityCreateEventBinding;
import com.example.countdowntimer.event_db.EventDao;
import com.example.countdowntimer.event_db.EventDatabase;
import com.example.countdowntimer.event_db.EventModel;
import com.example.countdowntimer.utils.AlarmNotificationHandler;
import com.example.countdowntimer.utils.EventHandlerAlarm;
import com.example.countdowntimer.utils.ImageHandler;
import com.example.countdowntimer.utils.TimeHandler;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CreateEventActivity extends AppCompatActivity {

    private ActivityCreateEventBinding binding;
    private final int GALLERY_REQ_CODE = 101;
    private final String TAG = "CREATE_EVENT_ACTIVITY";

    private ImageHandler imgHandler;
    private EventDao dao;

    private String old_event_name = null;
    private boolean isUpdateCalled = false;

    private File imageFolder;

    private AlarmNotificationHandler alarmHandler;
    private EventHandlerAlarm eventHandlerAlarm;
    private TimeHandler timeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Create Event Activity is starting");

        // Initializing all the classes.
        init();
        isUpdateCalled = isUpdateEventCalled();


        // fetching image from gallery and attaching to imageview
        binding.eventImage.setOnClickListener(v -> {
            Log.d(TAG, "Selecting image from gallery");
            try {
                getAndSetImage();
                Log.d(TAG, "Image attached success.");
            } catch (Exception e) {
                Log.e(TAG, "Error occurred in fetching image from gallery: " + e);
            }
        });


        // Picking date from DatePicker
        binding.dateParent.setEndIconOnClickListener(v -> {
            Log.d(TAG, "Calendar is clicked to select date");
            getAndSetDateToEt();
        });


        // Picking time from TimePicker
        binding.timeParent.setEndIconOnClickListener(v -> {
            Log.d(TAG, "Clock is clicked to select time");
            getAndSetTimeToEt();
        });


        // Creating event, storing into database, scheduling alarm
        binding.createEventBtn.setOnClickListener(v -> {
            Log.d(TAG, "Create Event button is clicked.");
            try {
                if (!doValidChecks()) return;

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.createEventBtn.setVisibility(View.GONE);
                Log.d(TAG, "Creating event...");

                String eventName = Objects.requireNonNull(binding.eventNameEt.getText()).toString();
                String eventDescription = Objects.requireNonNull(binding.eventDescriptionEt.getText()).toString();
                String eventDate = Objects.requireNonNull(binding.eventDateEt.getText()).toString();
                String eventTime = Objects.requireNonNull(binding.eventTimeEt.getText()).toString();
                String eventImage = imgHandler.postAndGetImagePath(binding.eventImage, eventName, imageFolder);

                Thread createEventThread = new Thread(() -> {
                    EventModel model = new EventModel(eventName, eventDescription, eventDate, eventTime, eventImage);
                    if (isUpdateCalled) {
                        dao.deleteEvent(old_event_name);
                    }
                    dao.insertEvent(model);
                });
                createEventThread.start();
                createEventThread.join();

                String time = eventDate + " " + eventTime;
                long eventTargetMillis = timeHandler.getTimeMillis(time);
                long alarmNotificationMillis = timeHandler.get5MinBeforeMillis(time);

                if ((alarmNotificationMillis + 60000) > System.currentTimeMillis()) {
                    Log.d(TAG, "Scheduling notification for time: " + timeHandler.getTimeStamp(alarmNotificationMillis));
                    alarmHandler.scheduleNotification(eventName, alarmNotificationMillis);
                    Log.d(TAG, "Notification is scheduled");
                } else {
                    Log.d(TAG, "Notification is not schedule as time left is less than 5 minutes");
                }

                Log.d(TAG, "Scheduling Event execution alarm for: "+eventName+" for time: "+timeHandler.getTimeStamp(eventTargetMillis));
                eventHandlerAlarm.scheduleEventExecution(eventName, eventTargetMillis);


                toMainActivity();
            } catch (Exception e) {
                Log.e(TAG, "Error occurred when create event is clicked: " + e);
            }

        });

        binding.cancelEvent.setOnClickListener(v -> {
            Log.d(TAG, "Canceling event creation. Moving back to MainActivity");
            toMainActivity();
        });

    }

    private void init() {
        Log.d(TAG, "Initializing classes and database object");
        try {
            imgHandler = new ImageHandler();
            alarmHandler = new AlarmNotificationHandler(this);
            timeHandler = new TimeHandler();
            eventHandlerAlarm = new EventHandlerAlarm(this);
            EventDatabase db = Room.databaseBuilder(
                            CreateEventActivity.this,
                            EventDatabase.class,
                            "event_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
            dao = db.eventDao();
            Log.d(TAG, "Initializing classes and database object are done...");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in init function of Create Event Activity: " + e);
        }

        try {
            Log.d(TAG, "Creating folder for event images");
            String FOLDER_NAME = "Images";
            imageFolder = new File(getExternalFilesDir(null), FOLDER_NAME);

            if (!imageFolder.exists()) {
                if (imageFolder.mkdir()) {
                    Log.d(TAG, "Images folder created successfully");
                } else {
                    Log.d(TAG, "Failed to create image folder");
                }
            } else {
                Log.d(TAG, "Image folder is already created");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred when creating images folder: " + e);
        }
    }

    private boolean isUpdateEventCalled() {
        Log.d(TAG, "Checking whether the call is made for update event.");
        try {

            // getting intent
            Intent updateIntent = getIntent();

            if (updateIntent.hasExtra("NAME")) {
                Log.d(TAG, "Yes, this call is for update event.");

                Thread getAndUpdateViewWithExistingData = new Thread(() -> {
                    old_event_name = updateIntent.getStringExtra("NAME");
                    Log.d(TAG, "Fetching details from database for: " + old_event_name);
                    EventModel model = dao.getEventModel(old_event_name);
                    Log.d(TAG, "Details are fetched successfully: " + model.toString());

                    runOnUiThread(() -> {
                        binding.eventNameEt.setText(model.getEventName());
                        binding.eventDescriptionEt.setText(model.getEventDescription());
                        binding.eventDateEt.setText(model.getEventDate());
                        binding.eventTimeEt.setText(model.getEventTime());
                        binding.eventImage.setImageBitmap(imgHandler.getImageFromPath(getApplicationContext(), model.getEventImage()));
                        Log.d(TAG, "Data views are updated successfully");
                    });
                });

                getAndUpdateViewWithExistingData.start();
                getAndUpdateViewWithExistingData.join();

                return true;
            } else {
                Log.d(TAG, "No update event call made..");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in isUpdateEventCalled function: " + e);
        }

        return false;
    }

    private void getAndSetImage() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in getAndSetImage()." + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == GALLERY_REQ_CODE) {
                    assert data != null;
                    Uri uriImage = data.getData();
                    if (uriImage != null) {
                        binding.eventImage.setImageURI(uriImage);
                        Log.d(TAG, "Image fetched successfully");
                    } else {
                        Toast.makeText(CreateEventActivity.this, "Error in fetching image. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in onActivityResult function: " + e);
        }
    }

    private void getAndSetDateToEt() {
        try {
            LocalDate date = LocalDate.now();
            new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    binding.eventDateEt.setText(setTimeFormatCorrect(dayOfMonth) + "-" + setTimeFormatCorrect(month + 1) + "-" + setTimeFormatCorrect(year));
                    Log.d(TAG, "Date is set to view: " + binding.eventDateEt.getText());
                }
            }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in getAndSetDateToEt function: " + e);
        }
    }

    private void getAndSetTimeToEt() {
        try {
            int hour, minute;
            if (Objects.requireNonNull(binding.eventTimeEt.getText()).length() == 0){
                LocalTime time = LocalTime.now();
                hour = time.getHour();
                minute = time.getMinute();
            }
            else{
                String[] timeArray = binding.eventTimeEt.getText().toString().split(":");
                hour = Integer.parseInt(timeArray[0]);
                minute = Integer.parseInt(timeArray[1]);
            }
            new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    binding.eventTimeEt.setText(setTimeFormatCorrect(hourOfDay) + ":" + setTimeFormatCorrect(minute));
                    Log.d(TAG, "Time is set to view: " + Objects.requireNonNull(binding.eventTimeEt.getText()));
                }
            }, hour, minute, true).show();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in getAndSetTimeToEt function: " + e);
        }
    }

    private String setTimeFormatCorrect(int value) {
        return value < 10 ? "0" + value : "" + value;
    }

    private boolean doValidChecks() {
        try {
            Log.d(TAG, "Going to perform valid checks");

            // checking for event name
            if (binding.eventNameEt.toString().isEmpty()) {
                Log.d(TAG, "Event name is empty");
                Toast.makeText(this, "Event Title is required", Toast.LENGTH_SHORT).show();
                return false;
            }

            // checking for date parameter
            if (Objects.requireNonNull(binding.eventDateEt.getText()).toString().isEmpty()) {
                Log.d(TAG, "Event date is incorrect");
                Toast.makeText(this, "Date should not be empty.", Toast.LENGTH_SHORT).show();
                return false;
            }

            // checking for date parameter
            if (Objects.requireNonNull(binding.eventTimeEt.getText()).toString().isEmpty()) {
                Log.d(TAG, "Event time is incorrect");
                Toast.makeText(this, "Time should not be empty.", Toast.LENGTH_SHORT).show();
                return false;
            }

            String dateTime = binding.eventDateEt.getText().toString() + " " + binding.eventTimeEt.getText().toString();

            if (isDateTimePast(dateTime)) {
                Log.d(TAG, "Event date and time is incorrect");
                Toast.makeText(this, "Date and Time should not be the past time.", Toast.LENGTH_SHORT).show();
                return false;
            }

            Log.d(TAG, "Valid checks success.");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in valid checks function: " + e);
        }

        return false;
    }

    public boolean isDateTimePast(String dateTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime targetDateTime = LocalDateTime.parse(dateTime, dateFormatter);
        return targetDateTime.isBefore(LocalDateTime.now());
    }

    private void toMainActivity() {
        Intent intent = new Intent(CreateEventActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}