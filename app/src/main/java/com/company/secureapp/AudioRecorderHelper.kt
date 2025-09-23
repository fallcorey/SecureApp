package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var isRecording = false

    companion object {
        private const val TAG = "AudioRecorderHelper"
    }

    fun startRecording(): Boolean {
        if (isRecording) {
            stopRecording()
        }

        try {
            val audioDir = getRecordingsDirectory()
            Log.d(TAG, "Recording directory: ${audioDir.absolutePath}")
            
            if (!audioDir.exists()) {
                val created = audioDir.mkdirs()
                Log.d(TAG, "Directory created: $created")
                if (!created) {
                    Log.e(TAG, "Failed to create directory")
                    return false
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !audioDir.canWrite()) {
                Log.e(TAG, "No write permission to directory")
                return false
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.aac")
            currentFilePath = audioFile.absolutePath

            Log.d(TAG, "Creating file: ${audioFile.absolutePath}")

            mediaRecorder = MediaRecorder()
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                }
            }

            return try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                isRecording = true
                Log.d(TAG, "✅ Recording STARTED successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting recording: ${e.message}")
                mediaRecorder?.release()
                mediaRecorder = null
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ General exception: ${e.message}")
            return false
        }
    }

    fun stopRecording(): Boolean {
        return try {
            if (isRecording && mediaRecorder != null) {
                Log.d(TAG, "Stopping recording...")
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping: ${e.message}")
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false

                val file = File(currentFilePath ?: "")
                if (file.exists()) {
                    Log.d(TAG, "✅ Recording STOPPED. File: ${file.name} (${file.length()} bytes)")
                    true
                } else {
                    Log.e(TAG, "❌ File not found: $currentFilePath")
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in stopRecording: ${e.message}")
            false
        }
    }

    fun getRecordedFilePath(): String? = currentFilePath

    fun getRecordedFile(): File? = currentFilePath?.let { File(it) }?.takeIf { it.exists() }

    fun isRecording(): Boolean = isRecording

    fun cleanup() { stopRecording() }

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
            dir.listFiles()?.filter { it.isFile && it.name.endsWith(".aac") } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
