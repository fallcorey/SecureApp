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

    // Начать запись
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
                prepare()
                start()
            }

            Log.d(TAG, "Recording started successfully")
            return true

        } catch (e: IOException) {
            Log.e(TAG, "Recording failed: ${e.message}")
            return false
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Recording failed: ${e.message}")
            return false
        }
    }

    // Остановить запись
    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Stop failed: ${e.message}")
            } finally {
                release()
            }
        }
        mediaRecorder = null
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
            null
        }
    }

    // Получить путь к записанному файлу
    fun getRecordedFilePath(): String {
        return currentFile?.absolutePath ?: "No file recorded"
    }

    // Проверить идет ли запись
    fun isRecording(): Boolean = mediaRecorder != null

    // Очистка ресурсов
    fun cleanup() {
        stopRecording()
        currentFile?.delete()
        currentFile = null
    }
}
