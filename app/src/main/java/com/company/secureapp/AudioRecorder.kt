package com.company.secureapp

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): Uri? {
        if (mediaRecorder != null) {
            stopRecording()
        }

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                
                currentFile = createAudioFile()
                currentFile?.let {
                    setOutputFile(it.absolutePath)
                }
                
                prepare()
                start()
            }
            return currentFile?.let { getUriForFile(it) }
        } catch (e: IOException) {
            stopRecording()
            return null
        } catch (e: IllegalStateException) {
            stopRecording()
            return null
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                // Ignore if already stopped
            }
            release()
        }
        mediaRecorder = null
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

    private fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
