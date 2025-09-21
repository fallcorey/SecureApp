package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var emailHelper: EmailHelper
    private lateinit var volumeKeyHelper: VolumeKeyHelper
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
        emailHelper = EmailHelper(this)

        // Инициализация VolumeKeyHelper
        volumeKeyHelper = VolumeKeyHelper(this) {
            if (!isEmergencyActive) {
                if (checkAllPermissions()) {
                    startCountdown()
                } else {
                    requestAllPermissions()
                }
            }
        }

        // Находим элементы
        sosButton = findViewById(R.id.sos_button)
        timerText = findViewById(R.id.timer_text)
        statusText = findViewById(R.id.status_text)
        settingsButton = findViewById(R.id.settings_button)

        // Устанавливаем тексты
        sosButton.text = "SOS"
        settingsButton.text = getString(R.string.settings_button)

        // Обработка intent от виджета
        handleWidgetIntent()

        // Callback для завершения записи аудио
        audioRecorder.onRecordingComplete = { file ->
            Log.d("AudioRecord", "Recording completed: ${file.absolutePath}")
            Log.d("AudioRecord", "File size: ${audioRecorder.getRecordedFileSizeFormatted()}")
            
            // Отправляем аудиофайл на email
            sendAudioToEmail(file)
        }

        audioRecorder.onRecordingError = { error ->
            Log.e("AudioRecord", "Recording error: $error")
            showToast("Audio recording error: $error")
        }

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

    override fun onResume() {
        super.onResume()
        volumeKeyHelper.startListening()
    }

    override fun onPause() {
        super.onPause()
        volumeKeyHelper.stopListening()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent()
    }

    // Обработка intent от виджета
    private fun handleWidgetIntent() {
        if (intent?.action == "ACTION_TRIGGER_SOS") {
            if (!isEmergencyActive) {
                if (checkAllPermissions()) {
                    startCountdown()
                } else {
                    requestAllPermissions()
                }
            }
        }
    }

    // Перехват физических кнопок
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeKeyHelper.simulateVolumePress()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Таймер обратного отсчета 3 секунды
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

    // Отмена экстренного режима
    private fun cancelEmergency() {
        isEmergencyActive = false
        countDownTimer?.cancel()
        resetUI()
        Toast.makeText(this, getString(R.string.emergency_cancelled), Toast.LENGTH_LONG).show()
    }

    // Сброс UI к исходному состоянию
    private fun resetUI() {
        sosButton.text = getString(R.string.sos_button)
        sosButton.setBackgroundResource(R.drawable.sos_button_background)
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
                showToast("Please set SMS number in settings")
                resetUI()
                return
            }

            // Получаем настройку времени записи
            val recordingTime = preferenceHelper.getString("recording_time", "30000").toLongOrNull() ?: 30000

            // Получаем локацию
            val locationInfo = locationHelper.getLocationString()
            val networkInfo = networkHelper.getNetworkInfo()

            // Начинаем запись звука с указанным временем
            var isRecording = false
            if (audioRecorder.startRecording(recordingTime)) {
                isRecording = true
            }

            // Формируем сообщение с информацией о времени записи
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

            // Отправляем SMS
            val smsSent = networkHelper.sendSms(savedSmsNumber, message)
            
            if (smsSent) {
                statusText.text = "Emergency alert sent!"
                showToast("Help is on the way! SMS sent to emergency contacts")
            } else {
                statusText.text = "Failed to send alert"
                showToast("Failed to send SMS. Trying alternative methods...")
            }
            
            // Автоматический сброс через 5 секунд
            handler.postDelayed({
                resetUI()
                isEmergencyActive = false
            }, 5000)
            
        } catch (e: Exception) {
            statusText.text = "Error occurred"
            showToast("Error: ${e.message}")
            resetUI()
        }
    }

    // Отправка аудио на email
    private fun sendAudioToEmail(audioFile: File) {
        val savedEmail = preferenceHelper.getString("email_address", "")
        val savedUserName = preferenceHelper.getString("user_name", "User")
        
        if (savedEmail.isNotBlank() && audioFile.exists()) {
            showToast(getString(R.string.sending_audio))
            
            // Используем EmailHelper для отправки
            val emailSent = emailHelper.sendAudioFile(savedEmail, audioFile, savedUserName)
            
            if (emailSent) {
                Log.d("Email", "Audio file sent to $savedEmail")
                showToast(getString(R.string.audio_sent))
            }
        } else {
            Log.d("Email", "Email not configured or audio file missing")
        }
    }

    private fun stopRecording() {
        audioRecorder.stopRecording()
        val filePath = audioRecorder.getRecordedFilePath()
        Log.d("AudioRecord", "Recording saved: $filePath")
    }

    // Вспомогательные методы для показа сообщений
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
            if (allGranted) startCountdown()
        }
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        audioRecorder.cleanup()
        handler.removeCallbacksAndMessages(null)
    }
}
