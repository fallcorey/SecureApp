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
    private val handler = Handler(Looper.getMainLooper())
    private var recordingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            preferenceHelper = SimplePreferenceHelper(this)
            audioRecorder = AudioRecorderHelper(this)
            setContentView(R.layout.activity_main)

            val sosButton = findViewById<Button>(R.id.sos_button)
            val settingsButton = findViewById<Button>(R.id.settings_button)

            sosButton.setOnClickListener {
                if (checkAllPermissions()) {
                    startEmergencyProcedure()
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

    // Основная процедура экстренного оповещения
    private fun startEmergencyProcedure() {
        try {
            // Проверяем номер телефона
            val savedSmsNumber = preferenceHelper.getString("sms_number", "")
            if (savedSmsNumber.isBlank() || !savedSmsNumber.startsWith("+")) {
                Toast.makeText(this, 
                    "❌ Укажите номер в формате: +79123456789 в настройках", 
                    Toast.LENGTH_LONG).show()
                return
            }

            // Начинаем запись звука
            if (audioRecorder.startRecording()) {
                val recordingsPath = audioRecorder.getRecordingsDirectory()
                Toast.makeText(this, 
                    "🎤 Запись начата (5 минут)\n💾 Сохраняется в: $recordingsPath", 
                    Toast.LENGTH_LONG).show()
                
                // Запускаем таймер для отслеживания записи
                startRecordingTimer()
            }

            // Отправляем SMS
            val savedUserName = preferenceHelper.getString("user_name", "User")
            val message = "🚨 ЭКСТРЕННОЕ СООБЩЕНИЕ от $savedUserName! " +
                         "Требуется немедленная помощь! " +
                         "Аудиозапись ситуации ведется."
            
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(savedSmsNumber, null, message, null, null)
            
            // Останавливаем запись через 5 минут
            handler.postDelayed({
                stopEmergencyProcedure()
            }, 5 * 60 * 1000) // 5 минут
            
            Toast.makeText(this, 
                "✅ SMS отправлено на: $savedSmsNumber\n" +
                "🎤 Идет запись (5 минут)...", 
                Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Таймер для отслеживания записи
    private fun startRecordingTimer() {
        recordingRunnable = object : Runnable {
            override fun run() {
                if (audioRecorder.isRecording()) {
                    val remainingTime = audioRecorder.getRemainingTime()
                    
                    // Показываем уведомление каждые 30 секунд
                    if (remainingTime % 30 == 0L && remainingTime > 0) {
                        Toast.makeText(this@MainActivity, 
                            "🎤 Запись идет... Осталось: ${remainingTime} сек", 
                            Toast.LENGTH_SHORT).show()
                    }
                    
                    // Продолжаем обновление каждую секунду
                    handler.postDelayed(this, 1000)
                }
            }
        }
        recordingRunnable?.let { handler.post(it) }
    }

    // Остановка экстренной процедуры
    private fun stopEmergencyProcedure() {
        audioRecorder.stopRecording()
        recordingRunnable?.let { handler.removeCallbacks(it) }
        
        val filePath = audioRecorder.getRecordedFilePath()
        Toast.makeText(this, 
            "⏹️ Запись завершена\n💾 Файл: $filePath", 
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
                    Toast.makeText(this, "Разрешение denied: ${permissions[i]}", Toast.LENGTH_LONG).show()
                }
            }
            
            if (allGranted) {
                startEmergencyProcedure()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEmergencyProcedure()
        recordingRunnable?.let { handler.removeCallbacks(it) }
    }
}
