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
    private lateinit var locationHelper: LocationHelper
    private val SMS_PERMISSION_CODE = 1001
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)

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
        return checkSmsPermission() && checkAudioPermission() && checkLocationPermission()
    }

    private fun checkSmsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Запрос всех разрешений
    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkSmsPermission()) permissionsToRequest.add(Manifest.permission.SEND_SMS)
        if (!checkAudioPermission()) permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        if (!checkLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), SMS_PERMISSION_CODE)
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

            // Получаем локацию
            val locationInfo = locationHelper.getLocationString()

            // Начинаем запись звука
            var isRecording = false
            if (audioRecorder.startRecording()) {
                isRecording = true
                Toast.makeText(this, "🎤 Audio recording started", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ stopRecording() }, 30000)
            }

            // Отправляем SMS с локацией
            val message = "🚨 EMERGENCY from $savedUserName!\n" +
                         "Need immediate assistance!\n" +
                         "$locationInfo\n" +
                         if (isRecording) "Audio recording active" else ""

            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(savedSmsNumber, null, message, null, null)
            
            Toast.makeText(this, 
                "✅ SMS sent to: $savedSmsNumber\n" +
                "📍 Location included\n" +
                if (isRecording) "🎤 Recording audio..." else "", 
                Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        audioRecorder.stopRecording()
        Toast.makeText(this, "⏹️ Recording stopped", Toast.LENGTH_LONG).show()
    }

    // Обработка разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
            var allGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    Toast.makeText(this, "Permission denied: ${permissions[i]}", Toast.LENGTH_LONG).show()
                }
            }
            if (allGranted) startEmergencyProcedure()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
