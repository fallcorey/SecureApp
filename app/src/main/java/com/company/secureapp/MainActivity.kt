package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {

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

        preferenceHelper = SimplePreferenceHelper(this)
        audioRecorder = AudioRecorderHelper(this)
        locationHelper = LocationHelper(this)
        networkHelper = NetworkHelper(this)

        // Находим элементы
        sosButton = findViewById(R.id.sos_button)
        timerText = findViewById(R.id.timer_text)
        statusText = findViewById(R.id.status_text)
        settingsButton = findViewById(R.id.settings_button)

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

    // Таймер обратного отсчета 3 секунды
    private fun startCountdown() {
        isEmergencyActive = true
        sosButton.text = "CANCEL"
        sosButton.setBackgroundResource(R.drawable.sos_button_bg_pressed)
        timerText.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        statusText.text = "Release to cancel emergency"

        countDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                timerText.text = "Sending in: $seconds"
            }

            override fun onFinish() {
                if (isEmergencyActive) {
                    startEmergencyProcedure()
                }
            }
        }.start()
    }

    // Отмена экстренного режима
    private fun cancelEmergency() {
        isEmergencyActive = false
        countDownTimer?.cancel()
        resetUI()
        Toast.makeText(this, "Emergency cancelled", Toast.LENGTH_SHORT).show()
    }

    // Сброс UI к исходному состоянию
    private fun resetUI() {
        sosButton.text = "SOS"
        sosButton.setBackgroundResource(R.drawable.sos_button_bg)
        timerText.visibility = View.GONE
        statusText.visibility = View.GONE
    }

    // Основная процедура экстренного оповещения
    private fun startEmergencyProcedure() {
        statusText.text = "Sending emergency alert..."
        
        try {
            val savedSmsNumber = preferenceHelper.getString("sms_number", "")
            val savedUserName = preferenceHelper.getString("user_name", "User")
            
            if (savedSmsNumber.isBlank()) {
                showError("❌ Please set SMS number in settings")
                resetUI()
                return
            }

            // Получаем локацию
            val locationInfo = locationHelper.getLocationString()
            val networkInfo = networkHelper.getNetworkInfo()

            // Начинаем запись звука
            var isRecording = false
            if (audioRecorder.startRecording()) {
                isRecording = true
                handler.postDelayed({ stopRecording() }, 30000)
            }

            // Формируем сообщение
            val message = "🚨 EMERGENCY from $savedUserName!\n" +
                         "Need immediate assistance!\n" +
                         "$locationInfo\n" +
                         "Network: $networkInfo\n" +
                         if (isRecording) "Audio recording active" else ""

            // Отправляем SMS
            val smsSent = networkHelper.sendSms(savedSmsNumber, message)
            
            if (smsSent) {
                statusText.text = "✅ Emergency alert sent!"
                showToast("Help is on the way! SMS sent to emergency contacts")
            } else {
                statusText.text = "❌ Failed to send alert"
                showError("Failed to send SMS. Trying alternative methods...")
            }
            
            // Автоматический сброс через 5 секунд
            handler.postDelayed({
                resetUI()
                isEmergencyActive = false
            }, 5000)
            
        } catch (e: Exception) {
            statusText.text = "❌ Error occurred"
            showError("Error: ${e.message}")
            resetUI()
        }
    }

    private fun stopRecording() {
        audioRecorder.stopRecording()
        val filePath = audioRecorder.getRecordedFilePath()
        Log.d("AudioRecord", "Recording saved: $filePath")
    }

    // ... остальные методы checkAllPermissions, requestAllPermissions, 
    // onRequestPermissionsResult, onDestroy остаются без изменений
    // из предыдущего кода

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
