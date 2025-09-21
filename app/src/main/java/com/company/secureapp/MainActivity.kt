package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var audioRecorder: AudioRecorderHelper
    private lateinit var locationHelper: LocationHelper
    private lateinit var networkHelper: NetworkHelper
    private val SMS_PERMISSION_CODE = 1001
    
    private lateinit var sosButton: Button
    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var settingsButton: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var isEmergencyActive = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Initializing components")
        
        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)
        networkHelper = NetworkHelper(this)

        // Находим элементы
        sosButton = findViewById(R.id.sos_button)
        timerText = findViewById(R.id.timer_text)
        statusText = findViewById(R.id.status_text)
        settingsButton = findViewById(R.id.settings_button)

        // Устанавливаем тексты
        sosButton.text = "SOS"
        settingsButton.text = getString(R.string.settings_button)

        // Логируем состояние разрешений
        Log.d("MainActivity", "Permissions - Audio: ${checkAudioPermission()}, Storage: ${checkStoragePermission()}, Location: ${checkLocationPermission()}, SMS: ${checkSmsPermission()}")

        // Обработка intent от виджета
        handleWidgetIntent()

        sosButton.setOnClickListener {
            Log.d("MainActivity", "SOS button clicked, emergency active: $isEmergencyActive")
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

    // Обработка intent от виджета
    private fun handleWidgetIntent() {
        if (intent?.action == "ACTION_TRIGGER_SOS") {
            Log.d("MainActivity", "Received widget SOS intent")
            if (!isEmergencyActive) {
                if (checkAllPermissions()) {
                    startCountdown()
                } else {
                    requestAllPermissions()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent()
    }

    // Таймер обратного отсчета 3 секунды
    private fun startCountdown() {
        Log.d("MainActivity", "Starting countdown")
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
                Log.d("MainActivity", "Countdown finished, emergency active: $isEmergencyActive")
                if (isEmergencyActive) {
                    startEmergencyProcedure()
                }
            }
        }.start()
    }

    // Отмена экстренного режима
    private fun cancelEmergency() {
        Log.d("MainActivity", "Canceling emergency")
        isEmergencyActive = false
        countDownTimer?.cancel()
        resetUI()
        showToast(getString(R.string.emergency_cancelled))
    }

    // Сброс UI к исходному состоянию
    private fun resetUI() {
        sosButton.text = "SOS"
        sosButton.setBackgroundResource(R.drawable.sos_button_background)
        timerText.visibility = View.GONE
        statusText.visibility = View.GONE
    }

    // Основная процедура экстренного оповещения
    private fun startEmergencyProcedure() {
        Log.d("MainActivity", "Starting emergency procedure")
        statusText.text = "Sending emergency alert..."
        
        try {
            val savedSmsNumber = preferenceHelper.getString("sms_number", "")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            
            if (savedSmsNumber.isBlank()) {
                showToast("Please set SMS number in settings")
                resetUI()
                return
            }

            // Получаем настройку времени записи
            val recordingTime = preferenceHelper.getString("recording_time", "30000").toLongOrNull() ?: 30000
            Log.d("MainActivity", "Recording time: $recordingTime ms")

            // Получаем локацию
            val locationInfo = locationHelper.getLocationString()
            val networkInfo = networkHelper.getNetworkInfo()

            Log.d("MainActivity", "Starting audio recording...")
            val isRecording = audioRecorder.startRecording()
            Log.d("MainActivity", "Audio recording started: $isRecording")

            // Формируем сообщение
            val recordingDuration = when (recordingTime) {
                30000L -> "30 seconds"
                60000L -> "1 minute"
                120000L -> "2 minutes"
                300000L -> "5 minutes"
                else -> "${recordingTime / 1000} seconds"
            }

            val message = "EMERGENCY from $savedUserName!\n" +
                         "Need immediate assistance!\n" +
                         "$locationInfo\n" +
                         "Network: $networkInfo\n" +
                         if (isRecording) "Audio recording active ($recordingDuration)" else ""

            Log.d("MainActivity", "Sending SMS to: $savedSmsNumber")
            Log.d("MainActivity", "Message: $message")

            // Отправляем SMS
            val smsSent = networkHelper.sendSms(savedSmsNumber, message)
            
            if (smsSent) {
                statusText.text = "Emergency alert sent!"
                showToast("Help is on the way! SMS sent to emergency contacts")
                Log.d("MainActivity", "SMS sent successfully")
            } else {
                statusText.text = "Failed to send alert"
                showToast("Failed to send SMS. Trying alternative methods...")
                Log.d("MainActivity", "SMS failed to send")
            }

            // Останавливаем запись через заданное время
            if (isRecording) {
                Log.d("MainActivity", "Scheduling recording stop in $recordingTime ms")
                handler.postDelayed({
                    val stopped = audioRecorder.stopRecording()
                    val filePath = audioRecorder.getRecordedFilePath()
                    Log.d("MainActivity", "Recording stopped: $stopped, File: $filePath")
                    
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.exists()) {
                            val fileSize = file.length()
                            Log.d("MainActivity", "Recording saved: ${file.name}, size: $fileSize bytes")
                        } else {
                            Log.d("MainActivity", "Recording file does not exist")
                        }
                    }
                }, recordingTime)
            }
            
            // Автоматический сброс через 5 секунд
            handler.postDelayed({
                Log.d("MainActivity", "Resetting UI after emergency")
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

    // Проверка всех разрешений
    private fun checkAllPermissions(): Boolean {
        val hasPermissions = checkSmsPermission() && checkAudioPermission() && checkLocationPermission() && checkStoragePermission()
        Log.d("MainActivity", "All permissions granted: $hasPermissions")
        return hasPermissions
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

    private fun checkStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Запрос всех разрешений
    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkSmsPermission()) permissionsToRequest.add(Manifest.permission.SEND_SMS)
        if (!checkAudioPermission()) permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        if (!checkStoragePermission()) permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!checkLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        Log.d("MainActivity", "Requesting permissions: $permissionsToRequest")
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), SMS_PERMISSION_CODE)
        }
    }

    // Обработка разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_CODE) {
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
                startCountdown()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
