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
            
            // СОЗДАЕМ ПАПКУ С ПРОВЕРКОЙ
            if (!audioDir.exists()) {
                val created = audioDir.mkdirs()
                Log.d(TAG, "Directory creation: $created")
                if (!created) {
                    Log.e(TAG, "❌ FAILED to create directory!")
                    return false
                }
            }

            // ПРОВЕРЯЕМ ЧТО ПАПКА СУЩЕСТВУЕТ И ДОСТУПНА
            if (!audioDir.exists()) {
                Log.e(TAG, "❌ Directory still doesn't exist after creation!")
                return false
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.aac")
            currentFilePath = audioFile.absolutePath

            Log.d(TAG, "Attempting to create file: ${audioFile.absolutePath}")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                }
            }

            // ВАЖНО: правильная последовательность и обработка ошибок
            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
                isRecording = true
                Log.d(TAG, "✅ Recording STARTED successfully")
                Log.d(TAG, "✅ File should be at: $currentFilePath")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during recording start: ${e.message}")
                e.printStackTrace()
                mediaRecorder?.release()
                mediaRecorder = null
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ General exception in startRecording: ${e.message}")
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
                    try {
                        release()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error releasing MediaRecorder: ${e.message}")
                    }
                }
                mediaRecorder = null
                isRecording = false

                // ПОДРОБНАЯ ПРОВЕРКА ФАЙЛА
                val file = getRecordedFile()
                if (file != null && file.exists()) {
                    val fileSize = file.length()
                    Log.d(TAG, "✅ Recording STOPPED successfully")
                    Log.d(TAG, "✅ File exists: ${file.name}")
                    Log.d(TAG, "✅ File size: $fileSize bytes")
                    Log.d(TAG, "✅ Full path: ${file.absolutePath}")
                    true
                } else {
                    Log.e(TAG, "❌ Recording stopped but FILE NOT FOUND!")
                    Log.e(TAG, "❌ Expected path: $currentFilePath")
                    
                    // ДИАГНОСТИКА: проверяем что вообще есть в папке
                    val dir = getRecordingsDirectory()
                    if (dir.exists()) {
                        val files = dir.listFiles()
                        Log.e(TAG, "Files in directory: ${files?.size ?: 0}")
                        files?.forEach { f ->
                            Log.e(TAG, " - ${f.name} (${f.length()} bytes)")
                        }
                    } else {
                        Log.e(TAG, "❌ Directory doesn't exist: ${dir.absolutePath}")
                    }
                    false
                }
            } else {
                Log.d(TAG, "Not recording or mediaRecorder is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in stopRecording: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getRecordedFilePath(): String? {
        return currentFilePath
    }

    fun getRecordedFile(): File? {
        val path = currentFilePath ?: return null
        val file = File(path)
        return if (file.exists()) file else null
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
            // Android 10+ - Scoped Storage
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Security_app")
        } else {
            // Android 9 и ниже - Traditional Storage
            File(Environment.getExternalStorageDirectory(), "Security_app")
        }
    }

    fun getAllRecordings(): List<File> {
        val dir = getRecordingsDirectory()
        return if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.filter { 
                it.isFile && (it.name.endsWith(".aac") || it.name.endsWith(".mp4") || it.name.contains("emergency_")) 
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    // ДИАГНОСТИЧЕСКИЙ МЕТОД
    fun debugStorage(): String {
        val dir = getRecordingsDirectory()
        val info = StringBuilder()
        info.append("=== AUDIO STORAGE DEBUG ===\n")
        info.append("Android Version: ${Build.VERSION.SDK_INT}\n")
        info.append("Directory: ${dir.absolutePath}\n")
        info.append("Exists: ${dir.exists()}\n")
        info.append("Is Directory: ${dir.isDirectory}\n")
        
        if (dir.exists()) {
            info.append("Can Read: ${dir.canRead()}\n")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                info.append("Can Write: ${dir.canWrite()}\n")
            }
            val files = dir.listFiles()
            info.append("File Count: ${files?.size ?: 0}\n")
            files?.forEach { file ->
                info.append(" - ${file.name} (${file.length()} bytes, ${Date(file.lastModified())})\n")
            }
        } else {
            info.append("❌ DIRECTORY DOES NOT EXIST!\n")
        }
        info.append("Current recording: $isRecording\n")
        info.append("Current file path: $currentFilePath\n")
        info.append("=== END DEBUG ===")
        
        return info.toString()
    }
}
