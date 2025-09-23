package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var isRecording = false

    companion object {
        private const val TAG = "AudioRecorderHelper"
    }

    fun startRecording(): Boolean {
        if (isRecording) {
            Log.d(TAG, "Already recording, stopping first")
            stopRecording()
        }

        try {
            val audioDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Security_app")
            } else {
                File(Environment.getExternalStorageDirectory(), "Security_app")
            }
            
            if (!audioDir.exists()) {
                val created = audioDir.mkdirs()
                Log.d(TAG, "Directory created: $created, path: ${audioDir.absolutePath}")
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !audioDir.canWrite()) {
                Log.e(TAG, "Cannot write to directory: ${audioDir.absolutePath}")
                return false
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.aac")
            currentFilePath = audioFile.absolutePath

            Log.d(TAG, "Starting recording to: ${audioFile.absolutePath}")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                }
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Recording started successfully")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting recording: ${e.message}")
                    release()
                    return false
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}")
            return false
        }
    }

    fun stopRecording(): Boolean {
        return try {
            if (isRecording && mediaRecorder != null) {
                Log.d(TAG, "Stopping recording")
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping MediaRecorder: ${e.message}")
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false
                
                val file = getRecordedFile()
                if (file != null && file.exists()) {
                    val fileSize = file.length()
                    Log.d(TAG, "Recording stopped successfully. File: ${file.name}, Size: $fileSize bytes")
                    true
                } else {
                    Log.e(TAG, "Recording stopped but file not found")
                    false
                }
            } else {
                Log.d(TAG, "Not recording or mediaRecorder is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in stopRecording: ${e.message}")
            false
        }
    }

    fun getRecordedFilePath(): String? {
        return currentFilePath
    }

    fun getRecordedFile(): File? {
        return currentFilePath?.let { File(it) }
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up")
        stopRecording()
    }

    fun getRecordingsDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Security_app")
        } else {
            File(Environment.getExternalStorageDirectory(), "Security_app")
        }
    }

    fun getAllRecordings(): List<File> {
        val dir = getRecordingsDirectory()
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { it.isFile && (it.name.endsWith(".aac") || it.name.endsWith(".3gp") || it.name.endsWith(".mp4")) } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
