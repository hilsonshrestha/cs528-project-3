package com.example.recognizingactivities

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import com.example.recognizingactivities.databinding.ActivityMainBinding
import com.example.recognizingactivities.receiver.ActivityTransitionReceiver
import com.example.recognizingactivities.receiver.GeofenceBroadcastReceiver
import com.example.recognizingactivities.util.*
import com.example.recognizingactivities.util.Constants.ACTIVITY_TRANSITION_REQUEST_CODE
import com.google.android.gms.location.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, SensorEventListener {

    private lateinit var client: ActivityRecognitionClient
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaPlayerUtil: MyMediaPlayerUtil

    private lateinit var stepsCounterUtil: StepCounterUtil
    private lateinit var sensorManager : SensorManager

    lateinit var geofencingClient: GeofencingClient

    private val locationViewModel: LocationViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationViewModel.selectedItem.observe(this, Observer { location ->
            Log.i("MAINACTIVITY", "location was updated...")
            binding.currentAddress.text = "Address: ${location.address}"
        })

        client = ActivityRecognition.getClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        // Run activity recognition once the app starts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && !ActivityTransitionUtil.hasActivityTransitionPermission(this)
        ) {
            Toast.makeText(this, "No permission found. Requesting permission for Activity Recognition now...", Toast.LENGTH_SHORT).show()
            requestActivityTransitionPermission()
        } else {
            Toast.makeText(this, "Permission for Activity Recognition found. Start detecting now...", Toast.LENGTH_SHORT).show()
            requestForActivityUpdates()
        }

        // for MP3 audio
        mediaPlayerUtil = MyMediaPlayerUtil(this, R.raw.beat)


        // update UI on transition
        ActivityState.getState().observe(this, Observer { activity ->
            val (img, text) = when(activity) {
                DetectedActivity.STILL -> R.drawable.still to R.string.still
                DetectedActivity.WALKING -> R.drawable.walking to R.string.walking
                DetectedActivity.RUNNING -> R.drawable.running to R.string.running
                else -> R.drawable.in_vehicle to R.string.in_vehicle
            }
            binding.activityImage.setImageResource(img)
            binding.activityText.setText(text)

            // Updating MP3 audio
            when(activity) {
                DetectedActivity.WALKING -> mediaPlayerUtil.start()
                DetectedActivity.RUNNING -> mediaPlayerUtil.start()
                else -> mediaPlayerUtil.stop()
            }
        })

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepsCounterUtil = StepCounterUtil(this)


        // GeoFence
        createGeoFenceList()

        GeoFenceState.getLibraryState().observe(this, Observer { libraryVisits ->
            binding.txtLibraryCount.text = "Visits to Library geoFence: $libraryVisits"
        })

        GeoFenceState.getFullerState().observe(this, Observer { fullerVisits ->
            binding.txtFullerCount.text = "Visits to Fuller labs geoFence: $fullerVisits"
        })

    }


    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = "action.ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun createGeoFenceList() {
        val geoFences = mutableListOf<Geofence>()
        geoFences.add(Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(Constants.LOCATION_FULLER)

            // Set the circular region of this geofence.
            .setCircularRegion(
                42.2749, -71.8066,
                50.0F
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(TimeUnit.HOURS.toMillis(1))

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(10000)

            // Create the geofence.
            .build()
        )

        geoFences.add(Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(Constants.LOCATION_LIBRARY)

            // Set the circular region of this geofence.
            .setCircularRegion(
                42.2742, -71.8066,
                50.0F
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(TimeUnit.HOURS.toMillis(1))

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(10000)

            // Create the geofence.
            .build()
        )

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)

            // Add the geofences to be monitored by geofencing service.
            .addGeofences(geoFences)
            .build()

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@addOnCompleteListener
                }
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.i("GEOFENCE", "ADDED LISTENER")
                    }
                    addOnFailureListener {
                        Log.e("GEOFENCE", "FAILURE LISTENER")
                    }
                }
            }
        }
    }

    private fun requestForActivityUpdates(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        client
            .requestActivityTransitionUpdates(
                ActivityTransitionUtil.getActivityTransitionRequest(),
                getPendingIntent()
            )
            .addOnSuccessListener {
                Log.d("TAG", "Success - Request Updates")
                ActivityState.startActivityTimer()
            }
            .addOnFailureListener {
                Log.d("TAG", "Failure - Request Updates")
                Toast.makeText(this, "Failure - Request Updates", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deregisterForActivityUpdates(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        client
            .removeActivityUpdates(getPendingIntent()) // the same pending intent
            .addOnSuccessListener {
                getPendingIntent().cancel()
                Toast.makeText(this, "Successful deregistration of activity recognition", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Unsuccessful deregistration of activity recognition", Toast.LENGTH_SHORT).show()
            }
    }

    /* Create a pending intent b/c the */

    private fun getPendingIntent() : PendingIntent{
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        intent.action = "action.TRANSITIONS_DATA"

        return PendingIntent.getBroadcast(
            this,
            Constants.ACTIVITY_TRANSITION_REQUEST_CODE_RECEIVER,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        requestForActivityUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestActivityTransitionPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this) // add "this" for the callback
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestActivityTransitionPermission() {
        EasyPermissions.requestPermissions(
            this,
            "You need to allow activity transition permissions in order to use this feature.",
            ACTIVITY_TRANSITION_REQUEST_CODE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    override fun onResume() {
        super.onResume()

        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        when{
            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
            else -> {
                Toast.makeText(this, "Your device is not compatible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerUtil.closeMediaPlayer()
        // remove activity transition update
        deregisterForActivityUpdates()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val steps = stepsCounterUtil.detectSteps(event)
        binding.steps.text = "Steps taken since app started: $steps"
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //
    }
}