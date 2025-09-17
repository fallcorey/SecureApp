package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var isRecording = false

    fun startRecording(): Boolean {
        if (isRecording) {
            stopRecording()
        }

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioChannels(1)
                
                currentFile = createAudioFile()
                setOutputFile(currentFile?.absolutePath)
                
                prepare()
                start()
            }
            isRecording = true
            return true
        } catch (e: IOException) {
            stopRecording()
            return false
        } catch (e: IllegalStateException) {
            stopRecording()
            return false
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                if (isRecording) {
                    stop()
                }
            } catch (e: IllegalStateException) {
                // Ignore if already stopped
            } finally {
                release()
            }
        }
        mediaRecorder = null
        isRecording = false
    }

    fun getRecordedFile(): File? {
        return if (!isRecording) currentFile else null
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File.createTempFile(
            "AUDIO_${timeStamp}_",
            ".mp4",
            storageDir
        )
    }

    fun cleanup() {
        stopRecording()
        currentFile?.delete()
        currentFile = null
    }
}
