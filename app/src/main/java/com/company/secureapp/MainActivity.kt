package com.company.secureapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.company.secureapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var networkHelper: NetworkHelper
    private lateinit var preferenceHelper: PreferenceHelper

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(this)
        audioRecorder = AudioRecorder(this)
        networkHelper = NetworkHelper(this)
        preferenceHelper = PreferenceHelper(this)

        binding.sosButton.setOnClickListener {
            checkPermissionsAndSendAlert()
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkPermissionsAndSendAlert() {
        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            sendAlert()
        }
    }

    private fun sendAlert() {
        try {
            // Получаем геолокацию
            locationHelper.getCurrentLocation { location ->
                val lat = location?.latitude ?: 0.0
                val lon = location?.longitude ?: 0.0
                val mapsUrl = "https://maps.google.com/?q=$lat,$lon"

                // Начинаем запись аудио
                val audioFile = audioRecorder.startRecording()

                // Получаем настройки пользователя
                val fullName = preferenceHelper.getString("user_full_name", "")
                val phoneNumber = preferenceHelper.getString("user_phone_number", "")
                val messageText = "Emergency alert from $fullName"

                val alertMessage = "ALERT: $fullName, tel:$phoneNumber. Loc: $mapsUrl. Msg: $messageText"

                // Проверяем наличие интернета
                if (networkHelper.isInternetAvailable()) {
                    sendViaHttp(alertMessage, audioFile)
                } else {
                    sendViaSms(alertMessage)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendViaHttp(message: String, audioFile: Uri?) {
        val mattermostEnabled = preferenceHelper.getBoolean("mattermost_enabled", false)
        
        if (mattermostEnabled) {
            val serverUrl = BuildConfig.MATTERMOST_SERVER_URL
            val channelId = BuildConfig.MATTERMOST_CHANNEL_ID
            val login = BuildConfig.MATTERMOST_LOGIN
            val token = BuildConfig.MATTERMOST_TOKEN
            
            networkHelper.sendToMattermost(serverUrl, channelId, login, token, message, audioFile)
        } else {
            val endpointUrl = BuildConfig.SERVER_ENDPOINT_URL
            networkHelper.sendToServer(endpointUrl, message, audioFile)
        }
        
        Toast.makeText(this, "Alert sent via HTTP", Toast.LENGTH_SHORT).show()
    }

    private fun sendViaSms(message: String) {
        val smsNumber = preferenceHelper.getString("sms_number", BuildConfig.DEFAULT_SMS_NUMBER)
        networkHelper.sendSms(smsNumber, message)
        Toast.makeText(this, "Alert sent via SMS", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                sendAlert()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.stopRecording()
        locationHelper.stopLocationUpdates()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
