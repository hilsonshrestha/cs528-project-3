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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.recognizingactivities.databinding.ActivityMainBinding
import com.example.recognizingactivities.receiver.ActivityTransitionReceiver
import com.example.recognizingactivities.util.*
import com.example.recognizingactivities.util.Constants.ACTIVITY_TRANSITION_REQUEST_CODE
import com.example.recognizingactivities.util.Constants.LOCATION_REQUEST_CODE
import com.google.android.gms.location.*
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, SensorEventListener {

    private lateinit var client: ActivityRecognitionClient
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaPlayerUtil: MyMediaPlayerUtil

    private lateinit var stepsCounterUtil: StepCounterUtil;

    //Step counter code
    private lateinit var sensorManager : SensorManager;
    /*private val SMOOTHING_WINDOW_SIZE = 20;
    private val mRawAccelValues = FloatArray(3)

    private var mGraph1LastXValue = 0.0
    private var mGraph2LastXValue = 0.0

    private val mAccelValueHistory = Array(3) {
        FloatArray(
            SMOOTHING_WINDOW_SIZE
        )
    }
    private val mRunningAccelTotal = FloatArray(3)
    private val mCurAccelAvg = FloatArray(3)
    private var mCurReadIndex = 0

    private var mStepCounter = 0f

    private var mSeries1: LineGraphSeries<DataPoint>? = null
    private var mSeries2: LineGraphSeries<DataPoint>? = null

    //private val lastMag = 0.0
    private var avgMag = 0.0
    private var netMag = 0.0

    private var lastXPoint = 1.0
    var stepThreshold = 1.0
    var noiseThreshold = 2.0
    private val windowSize = 10*/

    //private lateinit var stepCounter: StepCounterUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager;

        client = ActivityRecognition.getClient(this)

        binding.switchActivityTransition.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && !ActivityTransitionUtil.hasActivityTransitionPermission(this)
                ) {
                    /* Change the switch back to false because the permission is not yet done*/
                    binding.switchActivityTransition.isChecked = false
                    Log.d("TAG", "Requesting permission for Activity Recognition...")
                    requestActivityTransitionPermission()
                } else {
                    Toast.makeText(this, "Permission for Activity Recognition found. Start detecting now...", Toast.LENGTH_LONG).show()
                    requestForActivityUpdates()
                }
            } else {
                deregisterForActivityUpdates()
            }
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

        //For step counter

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        stepsCounterUtil = StepCounterUtil(this)
        //val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        /*mSeries1 = LineGraphSeries()
        mSeries2 = LineGraphSeries()*/
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
                Toast.makeText(this, "Failure - Request Updates", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Successful deregistration of activity recognition", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Unsuccessful deregistration of activity recognition", Toast.LENGTH_LONG).show()
            }
    }

    /* Create a pending intent b/c the */

    private fun getPendingIntent() : PendingIntent{
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        intent.action = "action.TRANSITIONS_DATA"
        Log.d("TAG", "PendingIntent is being called...")
        Log.d("TAG", "intent content...${intent.toString()}")

        return PendingIntent.getBroadcast(
            this,
            Constants.ACTIVITY_TRANSITION_REQUEST_CODE_RECEIVER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        /* switch the checkbox back */
        binding.switchActivityTransition.isChecked = true
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
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onResume() {
        super.onResume()

        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        when{
            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            }
            else -> {
                Toast.makeText(this, "Your device is not compatible", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerUtil.closeMediaPlayer()
        // remove activity transition update
        deregisterForActivityUpdates()
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {

        val steps = stepsCounterUtil.detectSteps(event);
        Log.i("Steps", steps.toString())
        binding.steps.text = "Steps taken since app started: $steps";
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //
    }

}