package com.company.secureapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceHelper = SimplePreferenceHelper(this)

        val saveButton = findViewById<Button>(R.id.save_button)
        val smsNumber = findViewById<EditText>(R.id.sms_number)
        val serverUrl = findViewById<EditText>(R.id.server_endpoint_url)
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        // Загружаем сохраненные настройки
        smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        serverUrl.setText(preferenceHelper.getString("server_url", ""))
        userName.setText(preferenceHelper.getString("user_name", ""))
        userPhone.setText(preferenceHelper.getString("user_phone", ""))

        saveButton.setOnClickListener {
            val smsNumberText = smsNumber.text.toString().trim()
            val serverUrlText = serverUrl.text.toString().trim()
            val userNameText = userName.text.toString().trim()
            val userPhoneText = userPhone.text.toString().trim()

            // Проверяем номер телефона
            if (smsNumberText.isNotBlank() && !smsNumberText.startsWith("+")) {
                Toast.makeText(this, 
                    "❌ Phone number must start with '+' (format: +79123456789)", 
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверяем длину номера
            if (smsNumberText.isNotBlank() && smsNumberText.length < 11) {
                Toast.makeText(this, 
                    "❌ Phone number too short (minimum 11 digits with +)", 
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверяем свой номер телефона
            if (userPhoneText.isNotBlank() && !userPhoneText.startsWith("+")) {
                Toast.makeText(this, 
                    "❌ Your phone number must start with '+'", 
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                // Сохраняем настройки
                preferenceHelper.saveString("sms_number", smsNumberText)
                preferenceHelper.saveString("server_url", serverUrlText)
                preferenceHelper.saveString("user_name", userNameText)
                preferenceHelper.saveString("user_phone", userPhoneText)
                
                // Показываем успешное сообщение
                val successMessage = if (smsNumberText.isNotBlank()) {
                    "✅ Settings saved!\n📱 SMS will be sent to: $smsNumberText"
                } else {
                    "✅ Settings saved!\n⚠️ SMS number not set - alerts won't work"
                }
                
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this, "❌ Save error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
