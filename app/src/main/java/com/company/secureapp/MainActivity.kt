package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    private val TAG = "SecureAppDebug"
    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var audioRecorder: AudioRecorderHelper
    private val SMS_PERMISSION_CODE = 1001
    private val AUDIO_PERMISSION_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            preferenceHelper = SimplePreferenceHelper(this)
            audioRecorder = AudioRecorderHelper(this)
            setContentView(R.layout.activity_main)

            val sosButton = findViewById<Button>(R.id.sos_button)
            val settingsButton = findViewById<Button>(R.id.settings_button)

            sosButton.setOnClickListener {
                // Проверяем разрешения перед отправкой SMS
                if (checkSmsPermission() && checkAudioPermission()) {
                    sendEmergencySms()
                } else {
                    requestPermissions()
                }
            }

            settingsButton.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "App error: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
    private fun requestPermissions() {
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

    // Отправка экстренного SMS
    private fun sendEmergencySms() {
        try {
            // Начинаем запись звука
            if (audioRecorder.startRecording()) {
                Toast.makeText(this, "🎤 Recording started...", Toast.LENGTH_SHORT).show()
            }

            val savedSmsNumber = preferenceHelper.getString("sms_number", "+1234567890")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            val savedUserPhone = preferenceHelper.getString("user_phone", "")
            
            // Создаем сообщение
            val message = "🚨 EMERGENCY ALERT from $savedUserName ($savedUserPhone)! " +
                         "Need immediate assistance! Location: ..."
            
            // Отправляем SMS
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(savedSmsNumber, null, message, null, null)
            
            // Останавливаем запись через 15 секунд
            Handler(Looper.getMainLooper()).postDelayed({
                audioRecorder.stopRecording()
                Toast.makeText(this, "⏹️ Recording stopped", Toast.LENGTH_SHORT).show()
            }, 15000)
            
            Toast.makeText(this, 
                "✅ SMS sent to: $savedSmsNumber\n🎤 Recording audio...", 
                Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
                sendEmergencySms()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Останавливаем запись при закрытии приложения
        audioRecorder.stopRecording()
    }
}
