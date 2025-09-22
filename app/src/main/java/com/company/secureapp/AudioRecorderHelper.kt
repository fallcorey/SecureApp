package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var recordingHandler: Handler? = null
    private var recordingRunnable: Runnable? = null
    private val TAG = "AudioRecorder"
    
    // Callback для уведомления о завершении записи
    var onRecordingComplete: ((File) -> Unit)? = null
    var onRecordingError: ((String) -> Unit)? = null

    // Начать запись с автоматической остановкой через заданное время
    fun startRecording(recordingTimeMillis: Long = 30000): Boolean {
        return try {
            stopRecording() // Останавливаем предыдущую запись

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioChannels(1)
                setOutputFile(createAudioFile()?.absolutePath)
                prepare()
                start()
            }

            // Запускаем автоматическую остановку через заданное время
            startAutoStopTimer(recordingTimeMillis)

            Log.d(TAG, "Recording started successfully for $recordingTimeMillis ms")
            true

        } catch (e: IOException) {
            Log.e(TAG, "Recording failed: ${e.message}")
            onRecordingError?.invoke("Recording failed: ${e.message}")
            false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Recording failed: ${e.message}")
            onRecordingError?.invoke("Recording failed: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Recording failed: ${e.message}")
            onRecordingError?.invoke("Recording failed: ${e.message}")
            false
        }
    }

    // Запустить таймер автоматической остановки
    private fun startAutoStopTimer(recordingTimeMillis: Long) {
        recordingHandler = Handler(Looper.getMainLooper())
        recordingRunnable = Runnable {
            Log.d(TAG, "Auto-stop timer triggered after $recordingTimeMillis ms")
            stopRecording()
        }
        recordingHandler?.postDelayed(recordingRunnable!!, recordingTimeMillis)
    }

    // Остановить запись
    fun stopRecording(): Boolean {
        return try {
            // Отменяем таймер автоматической остановки
            recordingRunnable?.let { recordingHandler?.removeCallbacks(it) }
            recordingHandler = null
            recordingRunnable = null

            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                    Log.d(TAG, "Recording stopped successfully")
                    
                    // Уведомляем о завершении записи
                    currentFile?.let { file ->
                        if (file.exists() && file.length() > 0) {
                            onRecordingComplete?.invoke(file)
                            true
                        } else {
                            Log.e(TAG, "Recorded file is empty or doesn't exist")
                            onRecordingError?.invoke("Recorded file is empty")
                            false
                        }
                    } ?: run {
                        Log.e(TAG, "No recorded file found")
                        onRecordingError?.invoke("No recorded file found")
                        false
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Stop failed: ${e.message}")
                    onRecordingError?.invoke("Stop failed: ${e.message}")
                    false
                } catch (e: Exception) {
                    Log.e(TAG, "Stop failed: ${e.message}")
                    onRecordingError?.invoke("Stop failed: ${e.message}")
                    false
                }
            }
            mediaRecorder = null
            true
        } catch (e: Exception) {
            Log.e(TAG, "Stop failed: ${e.message}")
            onRecordingError?.invoke("Stop failed: ${e.message}")
            false
        }
    }

    // Создать файл для записи
    private fun createAudioFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            File.createTempFile(
                "emergency_${timeStamp}_",
                ".mp3",
                storageDir
            ).apply {
                currentFile = this
                Log.d(TAG, "Audio file created: $absolutePath")
            }
        } catch (e: IOException) {
            Log.e(TAG, "File creation failed: ${e.message}")
            onRecordingError?.invoke("File creation failed: ${e.message}")
            null
        }
    }

    // Получить путь к записанному файлу
    fun getRecordedFilePath(): String {
        return currentFile?.absolutePath ?: "No file recorded"
    }

    // Получить файл записи
    fun getRecordedFile(): File? {
        return currentFile
    }

    // Проверить идет ли запись
    fun isRecording(): Boolean = mediaRecorder != null

    // Получить текущее время записи в миллисекундах
    fun getCurrentRecordingTime(): Long {
        return mediaRecorder?.let {
            try {
                // Для MediaRecorder нет прямого метода получения текущего времени записи
                // Возвращаем 0, так как эта информация не доступна напрямую
                0L
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }

    // Очистка ресурсов
    fun cleanup() {
        stopRecording()
        currentFile?.delete()
        currentFile = null
        onRecordingComplete = null
        onRecordingError = null
    }

    // Получить размер записанного файла в байтах
    fun getRecordedFileSize(): Long {
        return currentFile?.length() ?: 0L
    }

    // Получить читаемый размер файла
    fun getRecordedFileSizeFormatted(): String {
        val bytes = getRecordedFileSize()
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
