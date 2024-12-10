package com.example.countdowntimer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.countdowntimer.databinding.ActivityFinalAlarmBinding;
import com.example.countdowntimer.event_db.EventDao;
import com.example.countdowntimer.event_db.EventDatabase;
import com.example.countdowntimer.event_db.EventModel;
import com.example.countdowntimer.utils.ImageHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinalAlarmActivity extends AppCompatActivity {

    private ActivityFinalAlarmBinding binding;
    private static final String TAG = "FINAL_ALARM_ACTIVITY";

    private EventDao dao;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ImageHandler imageHandler = new ImageHandler();
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinalAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.primary_color));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_color));

        Log.d(TAG, "Final Alarm activity started");
        setAnimationToDoneButton();

        initDatabase();
        handleIncomingIntent();
        startAlarmTone();


        binding.finalDoneButton.setOnClickListener(v -> {
            mp.stop();
            mp.release();
            finish();
        });
    }

    private void initDatabase() {
        try {
            dao = EventDatabase.getInstance(this).eventDao();
            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in database initialization: " + e);
        }
    }

    private void handleIncomingIntent() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra("NAME")) {
                String eventName = intent.getStringExtra("NAME");
                Log.d(TAG, "Got the event name: " + eventName);
                if (eventName != null) {
                    executor.execute(() -> {
                        EventModel model = dao.getEventModel(eventName);
                        if (model != null) {
                            runOnUiThread(() -> {
                                binding.finalTitleName.setText(model.getEventName());
                                binding.finalBackgroundLogo.setImageBitmap(
                                        imageHandler.getImageFromPath(FinalAlarmActivity.this, model.getEventImage())
                                );
                            });
                        } else {
                            Log.e(TAG, "EventModel not found for name: " + eventName);
                        }
                    });
                } else {
                    Log.e(TAG, "Event name is null in the intent extras");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in handleIncomingIntent: " + e);
        }
    }

    private void startAlarmTone() {
        try {
            mp = MediaPlayer.create(FinalAlarmActivity.this, R.raw.ringtone);
            mp.start();
            mp.setLooping(true);
            Log.d(TAG, "Event music is started.");
        } catch (Exception e) {
            Log.e(TAG, "Error occurred in media player: " + e);
        }
    }

    private void setAnimationToDoneButton() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.final_acitivty_done_button);
        binding.finalDoneButton.startAnimation(animation);
    }
}
