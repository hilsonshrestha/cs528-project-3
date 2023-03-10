package com.example.recognizingactivities.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.DetectedActivity

object ActivityState {
    private val state = MutableLiveData<Int>(DetectedActivity.STILL)
    private var startTime: Long = 0

    fun updateState(newState: Int) {
        state.value = newState
    }

    fun getState(): LiveData<Int> {
        return state
    }

    fun startActivityTimer() {
        startTime = System.currentTimeMillis()
    }

    fun getStartTime(): Long {
        return startTime
    }
}