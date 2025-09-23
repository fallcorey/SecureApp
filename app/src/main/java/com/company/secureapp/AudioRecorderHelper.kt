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
        // Останавливаем предыдущую запись, если она активна
        if (isRecording) {
            stopRecording()
        }

        try {
            // === ИСПРАВЛЕННЫЙ БЛОК: совместимость с Android 10+ ===
            // Создаем папку для записей с учетом версии Android
            val audioDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Для Android 10+ используем scoped storage
                File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Security_app")
            } else {
                // Для старых версий используем традиционный путь
                File(Environment.getExternalStorageDirectory(), "Security_app")
            }
            
            if (!audioDir.exists()) {
                val created = audioDir.mkdirs()
                Log.d(TAG, "Directory created: $created, path: ${audioDir.absolutePath}")
            }

            // Проверяем доступность папки (только для Android < 10)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !audioDir.canWrite()) {
                Log.e(TAG, "Cannot write to directory: ${audioDir.absolutePath}")
                return false
            }

            // Генерируем имя файла с timestamp
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFile = File(audioDir, "emergency_$timeStamp.aac")
            
            currentFilePath = audioFile.absolutePath

            Log.d(TAG, "Starting recording to: ${audioFile.absolutePath}")

            // Создаем и настраиваем MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                // Шаг 1: Установка источника аудио
                setAudioSource(MediaRecorder.AudioSource.MIC)
                
                // Шаг 2: Установка формата вывода
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // Шаг 3: Установка аудио кодека
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                
                // Шаг 4: Установка файла вывода
                setOutputFile(audioFile.absolutePath)
                
                // === ИСПРАВЛЕННЫЙ БЛОК: настройки для разных версий Android ===
                // Дополнительные настройки для лучшего качества
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setAudioSamplingRate(44100) // 44.1 kHz
                    setAudioEncodingBitRate(128000) // 128 kbps
                }
                
                // Шаг 5: Подготовка и старт
                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Recording started successfully")
                    return true
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException during preparation: ${e.message}")
                    e.printStackTrace()
                    return false
                } catch (e: IOException) {
                    Log.e(TAG, "IOException during preparation: ${e.message}")
                    Log.e(TAG, "File path: ${audioFile.absolutePath}")
                    Log.e(TAG, "File exists: ${audioFile.exists()}")
                    Log.e(TAG, "Can write: ${audioFile.canWrite()}")
                    e.printStackTrace()
                    return false
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected exception during preparation: ${e.message}")
                    e.printStackTrace()
                    return false
                }
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: No permission to record audio")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "General exception: ${e.message}")
            e.printStackTrace()
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
                        // Продолжаем выполнение даже при ошибке остановки
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false
                
                // Проверяем, создался ли файл
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
            e.printStackTrace()
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
        // === ИСПРАВЛЕННЫЙ МЕТОД: совместимость с Android 10+ ===
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

    fun getRecordingStatus(): String {
        return if (isRecording) {
            "Recording active to: ${currentFilePath}"
        } else {
            "Not recording"
        }
    }

    // Метод для отладки - проверяет доступность записи
    fun checkRecordingCapability(): String {
        val dir = getRecordingsDirectory()
        val capabilities = StringBuilder()
        
        capabilities.append("Directory: ${dir.absolutePath}\n")
        capabilities.append("Exists: ${dir.exists()}\n")
        capabilities.append("Android Version: ${Build.VERSION.SDK_INT}\n")
        
        // Проверка записи только для старых версий Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            capabilities.append("Can write: ${dir.canWrite()}\n")
            capabilities.append("Free space: ${dir.freeSpace / (1024 * 1024)} MB\n")
        } else {
            capabilities.append("Using scoped storage (Android 10+)\n")
        }
        
        return capabilities.toString()
    }
}
