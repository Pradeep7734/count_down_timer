package com.example.countdowntimer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.countdowntimer.FinalAlarmActivity;

public class EventHandlerReceiver extends BroadcastReceiver {

    private final String TAG = "EVENT_HANDLER_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            String eventName = intent.getStringExtra("NAME");
            Intent toFinalActivityIntent = new Intent(context, FinalAlarmActivity.class);
            toFinalActivityIntent.putExtra("NAME", eventName);
            toFinalActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(toFinalActivityIntent);
        }
        catch (Exception e){
            Log.e(TAG, "Error occurred in event handler receiver: "+e);
        }
    }
}
