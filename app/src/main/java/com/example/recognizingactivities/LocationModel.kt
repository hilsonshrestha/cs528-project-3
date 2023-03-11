package com.example.recognizingactivities

import com.google.android.gms.maps.model.LatLng

data class LocationModel (
    val location: LatLng? = null,
    var address: String = ""
)
