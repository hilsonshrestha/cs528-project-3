package com.example.recognizingactivities

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.example.recognizingactivities.databinding.ActivityMainBinding
import com.example.recognizingactivities.receiver.ActivityTransitionReceiver
import com.example.recognizingactivities.util.ActivityState
import com.example.recognizingactivities.util.ActivityTransitionUtil
import com.example.recognizingactivities.util.Constants
import com.example.recognizingactivities.util.Constants.ACTIVITY_TRANSITION_REQUEST_CODE
import com.google.android.gms.location.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var client: ActivityRecognitionClient
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        client = ActivityRecognition.getClient(this)

        binding.switchActivityTransition.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && !ActivityTransitionUtil.hasActivityTransitionPermission(this)
                ) {
                    /* Change the switch back to false because the permission is not yet done*/
                    binding.switchActivityTransition.isChecked = false
                    Log.d("TAG", "Executing request permission...")
                    requestActivityTransitionPermission()
                } else {
                    Log.d("TAG", "Already has permission...")
                    requestForUpdates()
                }
            } else {
                Log.d("TAG", "No permissions found...")
                removeUpdates()
            }
        }

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
        })
    }

    private fun requestForUpdates(){
        client
            .requestActivityTransitionUpdates(
                ActivityTransitionUtil.getActivityTransitionRequest(),
                getPendingIntent()
            )
            .addOnSuccessListener {
//                Log.d("TAG", "PendingIntent content ${getPendingIntent().toString()}")
                Log.d("TAG", "Success - Request Updates")
            }
            .addOnFailureListener {
                Log.d("TAG", "Failure - Request Updates")
            }
    }

    private fun removeUpdates(){
        client
            .removeActivityUpdates(getPendingIntent()) // the same pending intent
    }

    /* Create a pending intent b/c the */

    private fun getPendingIntent() : PendingIntent{
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        Log.d("TAG", "PendingIntent is being called...")
        Log.d("TAG", "intent content...${intent.toString()}")
        return PendingIntent.getBroadcast(
            this,
            Constants.ACTIVITY_TRANSITION_REQUEST_CODE_RECEIVER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        /* switch the checkbox back */
        binding.switchActivityTransition.isChecked = true
        requestForUpdates()
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
            android.Manifest.permission.ACTIVITY_RECOGNITION
        )
    }
}