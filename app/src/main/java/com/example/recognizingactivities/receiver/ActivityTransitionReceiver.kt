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
                    val info =
                        "Transition: ${ActivityTransitionUtil.toActivityString(event.activityType)} - ${ActivityTransitionUtil.toTransitionType(event.transitionType)}"
                    Log.d("TAG", info)
                    Toast.makeText(context, info, Toast.LENGTH_LONG).show()

                    // Update UI
                    if(event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        ActivityState.updateState(event.activityType)
                    }
                    // creating a new intent to pass the result data back to the calling component (i.e. MainActivity)
                    val resultIntent = Intent()
                    resultIntent.putExtra("result_key", info)
                }
            }
        }
    }
}