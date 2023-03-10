package com.example.recognizingactivities.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class MyActivityResultContract: ActivityResultContract<Intent, String>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return intent?.getStringExtra("result_key")?: "UNKNOWN"
    }
}