package com.company.secureapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File

class EmailHelper(private val context: Context) {

    fun sendAudioFile(email: String, audioFile: File, userName: String): Boolean {
        if (email.isBlank()) {
            Toast.makeText(context, context.getString(R.string.no_email_configured), Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Emergency Audio Recording - $userName")
                putExtra(Intent.EXTRA_TEXT, "Emergency audio recording from SecureApp. User: $userName")
                
                // Прикрепляем аудиофайл
                val audioUri = Uri.fromFile(audioFile)
                putExtra(Intent.EXTRA_STREAM, audioUri)
            }

            context.startActivity(Intent.createChooser(emailIntent, "Send audio recording via"))
            true
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.audio_send_error), Toast.LENGTH_LONG).show()
            false
        }
    }

    fun sendAudioFileWithGmail(email: String, audioFile: File, userName: String): Boolean {
        if (email.isBlank()) {
            return false
        }

        return try {
            val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                setPackage("com.google.android.gm")
                type = "audio/*"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Emergency Audio Recording - $userName")
                putExtra(Intent.EXTRA_TEXT, "Emergency audio recording from SecureApp.\n\nUser: $userName\n\nThis is an automated emergency message.")
                
                val audioUri = Uri.fromFile(audioFile)
                putExtra(Intent.EXTRA_STREAM, audioUri)
            }

            context.startActivity(gmailIntent)
            true
        } catch (e: Exception) {
            // Если Gmail не установлен, используем обычный метод
            sendAudioFile(email, audioFile, userName)
        }
    }
}
