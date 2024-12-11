package com.example.countdowntimer;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.countdowntimer.databinding.ActivityMainBinding;
import com.example.countdowntimer.event_db.EventDao;
import com.example.countdowntimer.event_db.EventDatabase;
import com.example.countdowntimer.event_db.EventModel;
import com.example.countdowntimer.recycler_view.EventAdapter;
import com.example.countdowntimer.recycler_view.EventRecyclerInterface;
import com.example.countdowntimer.utils.AlarmNotificationReceiver;
import com.example.countdowntimer.utils.ImageHandler;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EventRecyclerInterface {

    private ActivityMainBinding binding;
    private ImageHandler imageHandler;
    private EventAdapter eventAdapter;
    private final String TAG = "MAIN_ACTIVITY";
    private List<EventModel> eventModelList;
    private EventDao dao;
    private boolean isClicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Main Activity is starting");

        // Initializing classes and database, collecting events from database.
        init();
        checkPermissions();


        // Go to create event activity
        binding.addEvent.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));


        // Callback is attached to recyclerview for swipe right animation on views.
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.eventsRecyclerView);


        // open internal setting of the app
        binding.settingIcon.setOnClickListener(v -> {
            if (!isClicked) {
                settingIconAnimation(binding.settingIcon, R.anim.rotate_in);
                isClicked = true;
            } else {
                settingIconAnimation(binding.settingIcon, R.anim.rotate_out);
                isClicked = false;
            }
        });
    }

    private void init() {
        Log.d(TAG, "Initializing classes and database object");
        try {
            imageHandler = new ImageHandler();
            EventDatabase db = Room.databaseBuilder(
                            MainActivity.this,
                            EventDatabase.class,
                            "event_db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
            dao = db.eventDao();
            Log.d(TAG, "Initializing classes and database object are done...");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in init function of Main activity: " + e);
        }

        try {
            Log.d(TAG, "Fetching all events from database.");
            Thread fetchEventsCall = getFetchEventsCall();
            fetchEventsCall.join();
            Log.d(TAG, "Fetching complete.");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while fetching the event data from database: " + e);
        }
    }

    @NonNull
    private Thread getFetchEventsCall() {
        Thread fetchEventsCall = new Thread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                eventModelList = dao.getEvents();
                sortEventList(eventModelList);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d(TAG, "Setting UI views with data");
                    if (eventModelList.isEmpty()) {
                        binding.noEventLayout.setVisibility(View.VISIBLE);
                        binding.eventsRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        binding.noEventLayout.setVisibility(View.INVISIBLE);
                        binding.eventsRecyclerView.setVisibility(View.VISIBLE);
                    }
                    eventAdapter = new EventAdapter(eventModelList, MainActivity.this, MainActivity.this);
                    binding.eventsRecyclerView.setAdapter(eventAdapter);
                    eventAdapter.notifyDataSetChanged();
                    Log.d(TAG, "UI is updated with correct events");
                });
                Log.d(TAG, "DB Threading closed connection with the database");
            }
        });
        fetchEventsCall.start();
        return fetchEventsCall;
    }

    private void checkPermissions() {
        String[] permissions = getPermissions();

        boolean allPermissionsGranted = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            int PERMISSION_REQ_CODE = 200;
            requestPermissions(permissions, PERMISSION_REQ_CODE);
        }

        if (!checkForAlarmPermission()){
            requestExactAlarmPermission();
        }
    }

    @NonNull
    private static String[] getPermissions() {
        String READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
        String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
        String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
        String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
        return new String[]{
                READ_MEDIA_IMAGES,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                POST_NOTIFICATIONS
        };
    }

    private boolean checkForAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }

        return true;
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            try {
                int position = viewHolder.getAdapterPosition();
                EventModel deleteModel = eventModelList.get(position);

                Log.d(TAG, "Swiped called. Deleting event: " + deleteModel.getEventName() + " with position: " + position);

                // Removing item from the list;
                eventModelList.remove(position);
                eventAdapter.notifyItemRemoved(position);

                final boolean[] isUndoClicked = {false};

                try {
                    Snackbar.make(binding.eventsRecyclerView, deleteModel.getEventName(), Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                eventModelList.add(position, deleteModel);
                                eventAdapter.notifyItemInserted(position);
                                Log.d(TAG, "Item recovered at position: " + position);
                                isUndoClicked[0] = true;
                            })
                            .setText(deleteModel.getEventName() + " deleted!")
                            .show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {

                        Log.d(TAG, "Undo is not clicked. Deleting from database");
                        if (!isUndoClicked[0]) {

                            new Thread(() -> {
                                Log.d(TAG, "Request to delete event");
                                dao.deleteEvent(deleteModel.getEventName());
                                Log.d(TAG, "Deletion done successfully in database");

                                Log.d(TAG, "Deleting initiated of image from folder");
                                imageHandler.deleteImageFromPath(deleteModel.getEventImage());
                                Log.d(TAG, "Checking Pending intent");


                                Intent notificationIntent = new Intent(MainActivity.this, AlarmNotificationReceiver.class);
                                PendingIntent notificationPendingIntent = PendingIntent.getBroadcast(
                                        MainActivity.this,
                                        deleteModel.getEventName().hashCode(),
                                        notificationIntent,
                                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE
                                );

                                if (notificationPendingIntent != null) {
                                    Log.d(TAG, "Canceling pending event");
                                    notificationPendingIntent.cancel();
                                } else {
                                    Log.d(TAG, "No pending event found to cancel");
                                }
                            }).start();

                        }
                    }, 3000);


                } catch (Exception e) {
                    Log.e(TAG, "Error occurred in snackBar: " + e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while swiping event: " + e);
            }
        }
    };

    private void sortEventList(List<EventModel> events) {
        try {
            events.sort((o1, o2) -> {
                String time1 = o1.getEventDate() + " " + o1.getEventTime();
                String time2 = o2.getEventDate() + " " + o2.getEventTime();
                return time1.compareToIgnoreCase(time2);
            });
            Log.d(TAG, "Events sorted according to time execution.");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in sort event list method: " + e);
        }
    }

    private void settingIconAnimation(ImageView img_view, @AnimRes int anim_id) {
        Animation rotateIn = AnimationUtils.loadAnimation(MainActivity.this, anim_id);
        img_view.startAnimation(rotateIn);
    }

    @Override
    public void onEventClick(int position) {
        Log.d(TAG, "CardView is clicked to update item of position: " + position);
        Intent update_event_intent = new Intent(MainActivity.this, CreateEventActivity.class);
        update_event_intent.putExtra("NAME", eventModelList.get(position).getEventName());
        startActivity(update_event_intent);
    }
}
