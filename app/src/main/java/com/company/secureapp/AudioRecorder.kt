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
                setOutputFile(createAudioFile()?.absolutePath)
                prepare()
                start()
            }

            Log.d(TAG, "Recording started")
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
            }
            release()
        }
        mediaRecorder = null
        Log.d(TAG, "Recording stopped")
    }

    // Создать файл для записи
    private fun createAudioFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            File.createTempFile("EMERGENCY_${timeStamp}_", ".mp3", storageDir).also {
                currentFile = it
            }
        } catch (e: IOException) {
            Log.e(TAG, "File creation failed: ${e.message}")
            null
        }
    }

    // Получить записанный файл
    fun getRecordedFile(): File? = currentFile

    // Проверить идет ли запись
    fun isRecording(): Boolean = mediaRecorder != null
}
