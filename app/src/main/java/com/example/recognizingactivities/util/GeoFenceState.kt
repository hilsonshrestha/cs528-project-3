package com.example.recognizingactivities.util;

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData;

object GeoFenceState {
    private val fuller = MutableLiveData <Int>(0)

    private val library = MutableLiveData <Int>(0)

    fun incrementFuller() {
        fuller.value = fuller.value?.plus(1)
    }

    fun incrementLibrary() {
        library.value = library.value?.plus(1)
    }

    fun getFullerState(): LiveData<Int> {
        return fuller
    }

    fun getLibraryState(): LiveData<Int> {
        return library
    }

}
