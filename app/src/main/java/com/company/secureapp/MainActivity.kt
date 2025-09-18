package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var audioRecorder: AudioRecorderHelper
    private val SMS_PERMISSION_CODE = 1001
    private val AUDIO_PERMISSION_CODE = 1002
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)

        val sosButton = findViewById<Button>(R.id.sos_button)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        sosButton.setOnClickListener {
            if (checkAllPermissions()) {
                startEmergencyProcedure()
            } else {
                requestAllPermissions()
            }
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // Проверка всех разрешений
    private fun checkAllPermissions(): Boolean {
        return checkSmsPermission() && checkAudioPermission()
    }

    // Проверка разрешения на отправку SMS
    private fun checkSmsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Проверка разрешения на запись audio
    private fun checkAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запрос всех разрешений
    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkSmsPermission()) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }
        
        if (!checkAudioPermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                SMS_PERMISSION_CODE
            )
        }
    }

    // Основная процедура экстренного оповещения
    private fun startEmergencyProcedure() {
        try {
            val savedSmsNumber = preferenceHelper.getString("sms_number", "")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            
            if (savedSmsNumber.isBlank()) {
                Toast.makeText(this, "❌ Please set SMS number in settings", Toast.LENGTH_LONG).show()
                return
            }

            // Начинаем запись звука
            if (audioRecorder.startRecording()) {
                Toast.makeText(this, "🎤 Audio recording started", Toast.LENGTH_SHORT).show()
                
                // Останавливаем запись через 30 секунд
                handler.postDelayed({
                    stopRecording()
                }, 30000) // 30 секунд
            }

            // Отправляем SMS
            val message = "🚨 EMERGENCY ALERT from $savedUserName! " +
                         "Need immediate assistance! " +
                         "Audio recording is in progress."
            
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(savedSmsNumber, null, message, null, null)
            
            Toast.makeText(this, 
                "✅ SMS sent to: $savedSmsNumber\n" +
                "🎤 Recording audio for 30 seconds...", 
                Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Остановка записи
    private fun stopRecording() {
        audioRecorder.stopRecording()
        val filePath = audioRecorder.getRecordedFilePath()
        Toast.makeText(this, 
            "⏹️ Recording stopped\n" +
            "💾 File: $filePath", 
            Toast.LENGTH_LONG).show()
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
            var allGranted = true
            
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    Toast.makeText(this, "Permission denied: ${permissions[i]}", Toast.LENGTH_LONG).show()
                }
            }
            
            if (allGranted) {
                startEmergencyProcedure()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
