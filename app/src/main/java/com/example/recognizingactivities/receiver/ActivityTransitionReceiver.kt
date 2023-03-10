package com.example.recognizingactivities.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.recognizingactivities.util.ActivityState
import com.example.recognizingactivities.util.ActivityTransitionUtil
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult

class ActivityTransitionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TAG", "This is printed since BroadcastReceiver got triggered")
        if (ActivityTransitionResult.hasResult(intent)){
            Log.d("TAG", "This is printed since got an intent")
            val result = ActivityTransitionResult.extractResult(intent)
            Log.d("TAG", "extracted intent from the receiver...${result.toString()}")
            result?.let{
                    result.transitionEvents.forEach { event ->
                        Log.d("TAG", event.toString())

                        val activityType = ActivityTransitionUtil.toActivityString(event.activityType)
                        val transitionType = ActivityTransitionUtil.toTransitionType(event.transitionType)
                        Log.d("TAG", "Transition: $activityType - $transitionType")

                        // Display toast with old activity
                        if(event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                            val duration = (System.currentTimeMillis() - ActivityState.getStartTime()) / 1000
                            val durationMin = duration / 60
                            val durationSec = duration % 60
                            val info = "You were $activityType for ${durationMin}m, ${durationSec}s"
                            Toast.makeText(context, info, Toast.LENGTH_LONG).show()
                        }
                        // Update UI with new activity
                        else if(event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                            ActivityState.startActivityTimer()
                            ActivityState.updateState(event.activityType)
                        }
                    }

            }
        }
    }
}