package com.example.recognizingactivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel:ViewModel() {
    private val mutableSelectedItem = MutableLiveData<LocationModel>()
    val selectedItem: LiveData<LocationModel> get() = mutableSelectedItem

    fun selectItem(item: LocationModel) {
        mutableSelectedItem.value = item
    }
}