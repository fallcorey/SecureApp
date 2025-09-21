package com.company.secureapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log

class VolumeKeyHelper(private val context: Context, private val onSosActivated: () -> Unit) {

    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var volumeReceiver: BroadcastReceiver? = null
    private var lastVolumeChangeTime: Long = 0
    private var volumePressCount: Int = 0
    private val VOLUME_PRESS_DELAY = 1000L // 1 second for volume press sequence
    private val REQUIRED_PRESS_COUNT = 3 // Press both volume keys 3 times

    fun startListening() {
        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == AudioManager.VOLUME_CHANGED_ACTION) {
                    val currentTime = System.currentTimeMillis()
                    
                    if (currentTime - lastVolumeChangeTime > VOLUME_PRESS_DELAY) {
                        // Reset counter if too much time passed
                        volumePressCount = 0
                    }
                    
                    volumePressCount++
                    lastVolumeChangeTime = currentTime
                    
                    Log.d("VolumeKeyHelper", "Volume pressed. Count: $volumePressCount")
                    
                    if (volumePressCount >= REQUIRED_PRESS_COUNT) {
                        volumePressCount = 0
                        onSosActivated.invoke()
                    }
                }
            }
        }

        // Register receiver
        val filter = IntentFilter(AudioManager.VOLUME_CHANGED_ACTION)
        context.registerReceiver(volumeReceiver, filter)
        Log.d("VolumeKeyHelper", "Started listening for volume keys")
    }

    fun stopListening() {
        volumeReceiver?.let {
            context.unregisterReceiver(it)
            volumeReceiver = null
        }
        Log.d("VolumeKeyHelper", "Stopped listening for volume keys")
    }

    fun simulateVolumePress() {
        // For testing purposes
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastVolumeChangeTime > VOLUME_PRESS_DELAY) {
            volumePressCount = 0
        }
        
        volumePressCount++
        lastVolumeChangeTime = currentTime
        
        Log.d("VolumeKeyHelper", "Simulated volume press. Count: $volumePressCount")
        
        if (volumePressCount >= REQUIRED_PRESS_COUNT) {
            volumePressCount = 0
            onSosActivated.invoke()
        }
    }
}
