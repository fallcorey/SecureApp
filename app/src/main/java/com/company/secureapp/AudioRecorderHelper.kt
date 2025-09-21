package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
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

    fun startRecording(): Boolean {
        if (isRecording) {
            Log.d("AudioRecorder", "Already recording, stopping first")
            stopRecording()
        }

        try {
            // Создаем папку Security_app в external files directory
            val audioDir = File(context.getExternalFilesDir(null), "Security_app")
            if (!audioDir.exists()) {
                val created = audioDir.mkdirs()
                Log.d("AudioRecorder", "Directory created: $created, path: ${audioDir.absolutePath}")
            }

            // Генерируем имя файла с timestamp
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.3gp")
            currentFilePath = audioFile.absolutePath

            Log.d("AudioRecorder", "Starting recording to: ${audioFile.absolutePath}")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d("AudioRecorder", "Recording started successfully")
                    return true
                } catch (e: IllegalStateException) {
                    Log.e("AudioRecorder", "IllegalStateException: ${e.message}")
                    e.printStackTrace()
                    release()
                    return false
                } catch (e: IOException) {
                    Log.e("AudioRecorder", "IOException: ${e.message}")
                    e.printStackTrace()
                    release()
                    return false
                } catch (e: Exception) {
                    Log.e("AudioRecorder", "Exception: ${e.message}")
                    e.printStackTrace()
                    release()
                    return false
                }
            }
            
        } catch (e: Exception) {
            Log.e("AudioRecorder", "General exception: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    fun stopRecording(): Boolean {
        return try {
            if (isRecording && mediaRecorder != null) {
                Log.d("AudioRecorder", "Stopping recording")
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e("AudioRecorder", "Error stopping: ${e.message}")
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false
                Log.d("AudioRecorder", "Recording stopped successfully")
                true
            } else {
                Log.d("AudioRecorder", "Not recording or mediaRecorder is null")
                false
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Exception in stopRecording: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getRecordedFilePath(): String? {
        return currentFilePath
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun cleanup() {
        Log.d("AudioRecorder", "Cleaning up")
        stopRecording()
    }

    fun getRecordingStatus(): String {
        return if (isRecording) "Recording active" else "Not recording"
    }

    fun getRecordingsDirectory(): File {
        return File(context.getExternalFilesDir(null), "Security_app")
    }

    fun getAllRecordings(): List<File> {
        val dir = getRecordingsDirectory()
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { it.isFile && it.name.endsWith(".3gp") } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
