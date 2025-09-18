package com.company.secureapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsDebug"
    private lateinit var preferenceHelper: SimplePreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 1. Инициализируем помощник настроек
            preferenceHelper = SimplePreferenceHelper(this)
            Log.d(TAG, "PreferenceHelper initialized")
            
            // 2. Загружаем layout
            setContentView(R.layout.activity_settings)
            Log.d(TAG, "Layout loaded")
            
            // 3. Находим все элементы
            val saveButton = findViewById<Button>(R.id.save_button)
            val smsNumber = findViewById<EditText>(R.id.sms_number)
            val serverUrl = findViewById<EditText>(R.id.server_endpoint_url)
            val userName = findViewById<EditText>(R.id.user_full_name)
            val userPhone = findViewById<EditText>(R.id.user_phone_number)

            Log.d(TAG, "All UI elements found")
            
            // 4. ЗАГРУЗКА: Показываем сохраненные настройки
            smsNumber.setText(preferenceHelper.getString("sms_number", ""))
            serverUrl.setText(preferenceHelper.getString("server_url", ""))
            userName.setText(preferenceHelper.getString("user_name", ""))
            userPhone.setText(preferenceHelper.getString("user_phone", ""))
            
            Log.d(TAG, "Saved settings loaded into fields")
            
            // 5. СОХРАНЕНИЕ: Обработчик кнопки
            saveButton.setOnClickListener {
                try {
                    Log.d(TAG, "Save button clicked")
                    
                    // Сохраняем все настройки
                    preferenceHelper.saveString("sms_number", smsNumber.text.toString())
                    preferenceHelper.saveString("server_url", serverUrl.text.toString())
                    preferenceHelper.saveString("user_name", userName.text.toString())
                    preferenceHelper.saveString("user_phone", userPhone.text.toString())
                    
                    Log.d(TAG, "All settings saved to storage")
                    
                    // Показываем подтверждение
                    Toast.makeText(this, 
                        "✅ Settings saved permanently!\n" +
                        "📱 SMS: ${smsNumber.text}\n" +
                        "🌐 Server: ${serverUrl.text}\n" +
                        "👤 Name: ${userName.text}\n" +
                        "📞 Phone: ${userPhone.text}", 
                        Toast.LENGTH_LONG).show()
                    
                    // Закрываем экран
                    finish()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Save error: ${e.message}", e)
                    Toast.makeText(this, "❌ Save error", Toast.LENGTH_LONG).show()
                }
            }
            
            Log.d(TAG, "SettingsActivity ready")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Settings error", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
