package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View

class MainActivity : BaseActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var audioRecorder: AudioRecorderHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var networkHelper: NetworkHelper
    private val SMS_PERMISSION_CODE = 1001
    private val STORAGE_PERMISSION_CODE = 1002
    
    private lateinit var sosButton: Button
    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var settingsButton: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var isEmergencyActive = false
    private val handler = Handler(Looper.getMainLooper())

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

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            true
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAllPermissions(): Boolean {
        val smsPerm = checkSmsPermission()
        val audioPerm = checkAudioPermission()
        val locationPerm = checkLocationPermission()
        val storagePerm = checkStoragePermission()
        
        Log.d("MainActivity", "Permissions - SMS: $smsPerm, Audio: $audioPerm, Location: $locationPerm, Storage: $storagePerm")
        
        return smsPerm && audioPerm && locationPerm && storagePerm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)
        networkHelper = NetworkHelper(this)

        debugAudioRecording()

        sosButton = findViewById(R.id.sos_button)
        timerText = findViewById(R.id.timer_text)
        statusText = findViewById(R.id.status_text)
        settingsButton = findViewById(R.id.settings_button)

        sosButton.text = "SOS"
        settingsButton.text = getString(R.string.settings_button)

        sosButton.setOnClickListener {
            if (isEmergencyActive) {
                cancelEmergency()
            } else {
                if (checkAllPermissions()) {
                    startCountdown()
                } else {
                    requestAllPermissions()
                }
            }
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun debugAudioRecording() {
        Log.d("MainActivity", "=== Audio Recording Debug ===")
        Log.d("MainActivity", "Audio permission: ${checkAudioPermission()}")
        Log.d("MainActivity", "Storage permission: ${checkStoragePermission()}")
        
        try {
            Log.d("MainActivity", "Recording status: ${audioRecorder.isRecording()}")
            Log.d("MainActivity", "Recordings directory: ${audioRecorder.getRecordingsDirectory()}")
            
            val recordings = audioRecorder.getAllRecordings()
            Log.d("MainActivity", "Existing recordings: ${recordings.size}")
            recordings.forEach { file ->
                Log.d("MainActivity", " - ${file.name} (${file.length()} bytes)")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in debugAudioRecording: ${e.message}")
        }
    }

    private fun startCountdown() {
        isEmergencyActive = true
        sosButton.text = "CANCEL"
        sosButton.setBackgroundResource(android.R.drawable.btn_default)
        timerText.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        statusText.text = getString(R.string.release_to_cancel)

        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                timerText.text = getString(R.string.sending_in, seconds.toString())
            }

            override fun onFinish() {
                if (isEmergencyActive) {
                    startEmergencyProcedure()
                }
            }
        }.start()
    }

    private fun cancelEmergency() {
        isEmergencyActive = false
        countDownTimer?.cancel()
        resetUI()
        showToast(R.string.emergency_cancelled)
    }

    private fun resetUI() {
        sosButton.text = "SOS"
        sosButton.setBackgroundResource(R.drawable.sos_button_background)
        timerText.visibility = View.GONE
        statusText.visibility = View.GONE
    }

    private fun startEmergencyProcedure() {
        statusText.text = "Sending emergency alert..."
        
        try {
            val savedSmsNumber = preferenceHelper.getString("sms_number", "")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            val serverUrl = preferenceHelper.getString("server_url", "")
            val authToken = preferenceHelper.getString("server_auth_token", "")
            
            if (savedSmsNumber.isBlank() && serverUrl.isBlank()) {
                showToast("Please configure SMS number or server URL in settings")
                resetUI()
                return
            }

            if (!checkSmsPermission() && savedSmsNumber.isNotBlank()) {
                showToast("SMS permission required for emergency alerts")
                resetUI()
                return
            }

            val recordingTime = preferenceHelper.getString("recording_time", "30000").toLongOrNull() ?: 30000

            // ИСПРАВЛЕННАЯ СТРОКА: используем networkHelper.getNetworkInfo()
            val locationInfo = locationHelper.getLocationString()
            val networkInfo = networkHelper.getNetworkInfo()

            val isRecording = if (checkAudioPermission()) {
                audioRecorder.startRecording()
            } else {
                false
            }
            Log.d("MainActivity", "Audio recording started: $isRecording")

            Thread {
                try {
                    Log.d("MainActivity", "Starting alert in background thread")
                    
                    val alertResult = networkHelper.sendEmergencyAlert(
                        userName = savedUserName,
                        userPhone = preferenceHelper.getString("user_phone", ""),
                        locationInfo = locationInfo,
                        networkInfo = networkInfo,
                        audioRecorded = isRecording,
                        recordingTime = recordingTime,
                        serverUrl = serverUrl,
                        authToken = authToken,
                        smsNumber = savedSmsNumber
                    )
                    
                    Log.d("MainActivity", "Alert result: ${alertResult.success}")
                    alertResult.messages.forEach { message ->
                        Log.d("MainActivity", "Alert step: $message")
                    }
                    
                    runOnUiThread {
                        if (alertResult.success) {
                            statusText.text = "Emergency alert sent!"
                            showToast("Alert delivered! ${alertResult.details}")
                        } else {
                            statusText.text = "Failed to send alert"
                            showToast("Failed: ${alertResult.details}")
                            alertResult.messages.forEach { message ->
                                Log.e("MainActivity", "FAILED: $message")
                            }
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("MainActivity", "Background thread error: ${e.message}")
                    runOnUiThread {
                        statusText.text = "Error occurred"
                        showToast("Error: ${e.message}")
                    }
                }
            }.start()

            if (isRecording) {
                handler.postDelayed({
                    val stopped = audioRecorder.stopRecording()
                    val file = audioRecorder.getRecordedFile()
                    Log.d("MainActivity", "Recording stopped: $stopped")
                    
                    if (file != null && file.exists()) {
                        Log.d("MainActivity", "Recording saved: ${file.name} (${file.length()} bytes)")
                    }
                }, recordingTime)
            }
            
            handler.postDelayed({
                resetUI()
                isEmergencyActive = false
            }, 5000)
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in emergency procedure: ${e.message}")
            e.printStackTrace()
            statusText.text = "Error occurred"
            showToast("Error: ${e.message}")
            resetUI()
        }
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkSmsPermission()) permissionsToRequest.add(Manifest.permission.SEND_SMS)
        if (!checkAudioPermission()) permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (!checkStoragePermission()) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (!checkLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        Log.d("MainActivity", "Requesting permissions: $permissionsToRequest")
        
        if (permissionsToRequest.isNotEmpty()) {
            val requestCode = if (permissionsToRequest.contains(Manifest.permission.SEND_SMS)) {
                SMS_PERMISSION_CODE
            } else {
                STORAGE_PERMISSION_CODE
            }
            
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        Log.d("MainActivity", "Permission result - Code: $requestCode")
        
        var allGranted = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                showToast("Permission denied: ${permissions[i]}")
                Log.d("MainActivity", "Permission denied: ${permissions[i]}")
            } else {
                Log.d("MainActivity", "Permission granted: ${permissions[i]}")
            }
        }
        
        if (allGranted) {
            showToast("All permissions granted!")
            if (requestCode == SMS_PERMISSION_CODE) {
                startCountdown()
            }
        } else {
            showToast("Some permissions were denied. App may not work correctly.")
        }
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
