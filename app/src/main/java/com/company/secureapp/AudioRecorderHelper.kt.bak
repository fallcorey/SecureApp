package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private val TAG = "AudioRecorder"
    private var recordingStartTime: Long = 0

    // Начать запись на 5 минут
    fun startRecording(): Boolean {
        try {
            stopRecording() // Останавливаем предыдущую запись

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioChannels(1)
                setOutputFile(createAudioFile()?.absolutePath)
                setMaxDuration(5 * 60 * 1000) // 5 минут в миллисекундах
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            Log.d(TAG, "Recording started for 5 minutes. File: ${currentFile?.absolutePath}")
            return true

        } catch (e: IOException) {
            Log.e(TAG, "Recording failed - IO: ${e.message}")
            return false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Recording failed - State: ${e.message}")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Recording failed - General: ${e.message}")
            return false
        }
    }

    // Остановить запись
    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                if (isRecording()) {
                    stop()
                    val duration = System.currentTimeMillis() - recordingStartTime
                    Log.d(TAG, "Recording stopped. Duration: ${duration/1000} seconds")
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Stop failed - already stopped: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Stop failed - General: ${e.message}")
            } finally {
                release()
            }
        }
        mediaRecorder = null
        recordingStartTime = 0
    }

    // Создать файл для записи
    private fun createAudioFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            File(storageDir, "emergency_recording_${timeStamp}.mp3").apply {
                currentFile = this
                Log.d(TAG, "Audio file created: $absolutePath")
            }
        } catch (e: IOException) {
            Log.e(TAG, "File creation failed: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "File creation failed - General: ${e.message}")
            null
        }
    }

    // Получить путь к записанному файлу
    fun getRecordedFilePath(): String {
        return currentFile?.absolutePath ?: "Файл не создан"
    }

    // Получить директорию для записей
    fun getRecordingsDirectory(): String {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return dir?.absolutePath ?: "Директория не доступна"
    }

    // Проверить идет ли запись
    fun isRecording(): Boolean {
        return try {
            mediaRecorder != null
        } catch (e: Exception) {
            false
        }
    }

    // Получить оставшееся время записи в секундах
    fun getRemainingTime(): Long {
        if (recordingStartTime == 0L) return 0
        val elapsed = System.currentTimeMillis() - recordingStartTime
        val remaining = (5 * 60 * 1000 - elapsed).coerceAtLeast(0)
        return remaining / 1000 // возвращаем секунды
    }

    // Очистка ресурсов
    fun cleanup() {
        stopRecording()
        currentFile?.delete()
        currentFile = null
    }
}
