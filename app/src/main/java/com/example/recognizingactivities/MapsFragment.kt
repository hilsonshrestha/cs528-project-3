package com.example.recognizingactivities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest

    private var currentLocation: Location? = null

    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null

    private val locationViewModel: LocationViewModel by activityViewModels()

    private var currentLocationModel: LocationModel? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        this.googleMap = googleMap
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(100)
            .setMaxUpdateDelayMillis(1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation
                Log.i("LOCATION", "location received $currentLocation.latitude")

                if (currentLocation != null) {
                    val currentLocation = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                    if (marker == null) {

                        val smallMarker = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.marker), 64, 64, false)
                        val m = googleMap.addMarker(
                            MarkerOptions().position(currentLocation).title("Location").icon(
                                BitmapDescriptorFactory.fromBitmap(smallMarker))
                        )
                        marker = m

                        currentLocationModel = LocationModel(currentLocation, "")
                    }
                    marker!!.position = currentLocation

                    val geoCoder = context?.let { Geocoder(it, Locale("en", "us")) }
                    val addresses = geoCoder?.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
                    if (addresses != null && addresses.size == 1) {
                        Log.i("LOCATION", addresses[0].getAddressLine(0))
                        currentLocationModel?.address = addresses[0].getAddressLine(0)
                    }
                    locationViewModel.selectItem(currentLocationModel!!)

                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
                }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        unsubscribeToLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        subscribeToLocationUpdates()
    }


    private fun subscribeToLocationUpdates() {
        try {
            // TODO: Step 1.5, Subscribe to location changes.
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            Log.e("LOCATION", "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun unsubscribeToLocationUpdates() {
        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LOCATION", "Location Callback removed.")
                } else {
                    Log.d("LOCATION", "Failed to remove Location Callback.")
                }
            }
        } catch (unlikely: SecurityException) {
            Log.e("LOCATION", "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }
}