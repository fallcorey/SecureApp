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
            Log.d(TAG, "Already recording, stopping first")
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

            // Check write permission for older Android versions
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !audioDir.canWrite()) {
                Log.e(TAG, "No write permission to directory")
                return false
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.aac")
            currentFilePath = audioFile.absolutePath

            Log.d(TAG, "Creating file: ${audioFile.absolutePath}")

            mediaRecorder = MediaRecorder()
            
            // Configure MediaRecorder
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

            // Start recording with proper return
            return try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                isRecording = true
                Log.d(TAG, "✅ Recording STARTED successfully")
                Log.d(TAG, "✅ File path: $currentFilePath")
                true
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException: ${e.message}")
                e.printStackTrace()
                mediaRecorder?.release()
                mediaRecorder = null
                false
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                e.printStackTrace()
                mediaRecorder?.release()
                mediaRecorder = null
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}")
            e.printStackTrace()
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
                        Log.e(TAG, "Error stopping MediaRecorder: ${e.message}")
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false

                val file = getRecordedFile()
                if (file != null && file.exists()) {
                    val fileSize = file.length()
                    Log.d(TAG, "✅ Recording STOPPED successfully")
                    Log.d(TAG, "✅ File: ${file.name} (${fileSize} bytes)")
                    Log.d(TAG, "✅ Path: ${file.absolutePath}")
                    true
                } else {
                    Log.e(TAG, "❌ Recording stopped but FILE NOT FOUND")
                    Log.e(TAG, "❌ Expected path: $currentFilePath")
                    false
                }
            } else {
                Log.d(TAG, "Not recording or mediaRecorder is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in stopRecording: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getRecordedFilePath(): String? {
        return currentFilePath
    }

    fun getRecordedFile(): File? {
        return currentFilePath?.let { File(it) }?.takeIf { it.exists() }
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
            dir.listFiles()?.filter { 
                it.isFile && (it.name.endsWith(".aac") || it.name.endsWith(".3gp") || it.name.endsWith(".mp4")) 
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getStorageInfo(): String {
        val dir = getRecordingsDirectory()
        return """
            📱 Audio Storage Info:
            Android Version: ${Build.VERSION.SDK_INT}
            Directory: ${dir.absolutePath}
            Exists: ${dir.exists()}
            Is Directory: ${dir.isDirectory}
            Can Read: ${dir.canRead()}
            Can Write: ${if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) dir.canWrite() else "Scoped Storage"}
            File Count: ${dir.listFiles()?.size ?: 0}
        """.trimIndent()
    }
}
