package com.example.recognizingactivities.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast

class MyMediaPlayerUtil (private val context: Context, private val audioResourceId: Int) {

    private lateinit var mediaPlayer: MediaPlayer

    fun start(){
        if (!::mediaPlayer.isInitialized) {
            mediaPlayer = MediaPlayer.create(context, audioResourceId)
            Log.d("TAG", "a mediaPlayer is initialized")
        }
        mediaPlayer.start()
        Toast.makeText(context, "Media playing", Toast.LENGTH_SHORT).show()
        /*
         when the mediaPlayer completed the media content (end of audio)
         set the complete listener to restart the audio when it's finished playing
         */
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0) // set the playback position to the beginning of audio
            mediaPlayer.start() // Start the playback position to the beginning
        }
    }


    fun stop() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setOnCompletionListener (null) // Remove the onCompletionListener to avoid restarting the audio
            mediaPlayer.release()
            mediaPlayer = MediaPlayer.create(context, audioResourceId) // initialize the mediaPlayer again
        }
    }

    fun closeMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        } else {
            mediaPlayer = MediaPlayer()
        }
    }
}