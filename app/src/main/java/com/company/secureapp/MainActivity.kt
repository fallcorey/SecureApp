package com.company.secureapp

import android.content.Intent
import android.os.Bundle
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
    // private lateinit var audioRecorder: AudioRecorderHelper  // ЗАКОММЕНТИРОВАТЬ
    private val SMS_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            preferenceHelper = SimplePreferenceHelper(this)
            // audioRecorder = AudioRecorderHelper(this)  // ЗАКОММЕНТИРОВАТЬ
            setContentView(R.layout.activity_main)

            val sosButton = findViewById<Button>(R.id.sos_button)
            val settingsButton = findViewById<Button>(R.id.settings_button)

            sosButton.setOnClickListener {
                if (checkSmsPermission()) {
                    sendEmergencySms()
                } else {
                    requestSmsPermission()
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

    // Запрос разрешения
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_CODE
        )
    }

    // Упрощенная отправка SMS
    private fun sendEmergencySms() {
        try {
            val savedSmsNumber = preferenceHelper.getString("sms_number", "+1234567890")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            
            val message = "🚨 EMERGENCY from $savedUserName! Need help!"
            
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(savedSmsNumber, null, message, null, null)
            
            Toast.makeText(this, "✅ SMS sent to: $savedSmsNumber", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Обработка разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmergencySms()
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}
