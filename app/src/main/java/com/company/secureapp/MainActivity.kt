package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private lateinit var networkHelper: NetworkHelper
    private val SMS_PERMISSION_CODE = 1001
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)
        networkHelper = NetworkHelper(this)

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
                showError("❌ Please set SMS number in settings")
                return
            }

            // Проверяем номер телефона
            if (!isValidPhoneNumber(savedSmsNumber)) {
                showError("❌ Invalid phone number format. Use: +79123456789")
                return
            }

            // Получаем локацию
            val locationInfo = locationHelper.getLocationString()
            val networkInfo = networkHelper.getNetworkInfo()

            // Начинаем запись звука
            var isRecording = false
            if (audioRecorder.startRecording()) {
                isRecording = true
                showToast("🎤 Audio recording started")
                handler.postDelayed({ stopRecordingAndNotify() }, 30000)
            }

            // Формируем сообщение
            val message = "🚨 EMERGENCY from $savedUserName!\n" +
                         "Need immediate assistance!\n" +
                         "$locationInfo\n" +
                         "Network: $networkInfo\n" +
                         if (isRecording) "Audio recording active" else ""

            // Отправляем SMS через NetworkHelper
            val smsSent = networkHelper.sendSms(savedSmsNumber, message)
            
            if (smsSent) {
                showToast(
                    "✅ SMS sent to: $savedSmsNumber\n" +
                    "📍 Location: Included\n" +
                    "📶 Network: $networkInfo\n" +
                    if (isRecording) "🎤 Recording audio..." else ""
                )
            } else {
                showError("❌ Failed to send SMS. Check phone number and permissions.")
            }
            
        } catch (e: Exception) {
            showError("❌ Error: ${e.message}")
        }
    }

    // Проверка формата номера телефона
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+") && phoneNumber.length > 10
    }

    private fun stopRecordingAndNotify() {
        audioRecorder.stopRecording()
        showToast("⏹️ Recording stopped\n💾 Saved to: ${audioRecorder.getRecordedFilePath()}")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Обработка разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
            var allGranted = true
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    showError("Permission denied: ${permissions[i]}")
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
