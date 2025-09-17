package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "SecureAppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "onCreate started")
            setContentView(R.layout.activity_main)
            Log.d(TAG, "ContentView set successfully")

            val sosButton = findViewById<Button>(R.id.sos_button)
            val settingsButton = findViewById<Button>(R.id.settings_button)

            sosButton.setOnClickListener {
                Toast.makeText(this, "SOS activated! Starting emergency alert...", Toast.LENGTH_SHORT).show()
                // Временное сообщение - потом добавим функционал
                //  Запускаем процесс оповещения (пока в упрощенном виде)
                try {
                    startEmergencyProcedure()
                } catch (e: Exception) {
                    // Если что-то пошло не так - показывает ошибку
                    Toast.makeText(this, "SOS error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            settingsButton.setOnClickListener {
                Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()
                // Открываем SettingsActivity
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            Log.d(TAG, "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "App error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    // Функция для запуска экстренного процедуры (пока заглушка)
private fun startEmergencyProcedure() {
    // 1. Сообщаем о начале процесса
    Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show()
    
    // 2. Имитация получения локации (позже заменим на реальный LocationHelper)
    val fakeLocation = "55.7558° N, 37.6173° E" // Пример координат
    
    // 3. Имитация подготовки сообщения
    val emergencyMessage = "EMERGENCY: Need help! Location: $fakeLocation"
    
    // 4. Имитация отправки (позже заменим на реальный NetworkHelper)
    Toast.makeText(this, "Sending alert: $emergencyMessage", Toast.LENGTH_LONG).show()
    
    // 5. Сообщаем об успехе
    Toast.makeText(this, "Emergency alert sent successfully!", Toast.LENGTH_SHORT).show()
}
}
