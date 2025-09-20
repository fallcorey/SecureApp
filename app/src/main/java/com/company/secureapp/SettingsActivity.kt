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
        val serverUrl = findViewById<EditText>(R.id.server_url)
        val serverAuthToken = findViewById<EditText>(R.id.server_auth_token)
        val mattermostWebhook = findViewById<EditText>(R.id.mattermost_webhook)
        val mattermostChannel = findViewById<EditText>(R.id.mattermost_channel)
        val smsNumber = findViewById<EditText>(R.id.sms_number)
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        // Загружаем сохраненные настройки
        serverUrl.setText(preferenceHelper.getString("server_url", ""))
        serverAuthToken.setText(preferenceHelper.getString("server_auth_token", ""))
        mattermostWebhook.setText(preferenceHelper.getString("mattermost_webhook", ""))
        mattermostChannel.setText(preferenceHelper.getString("mattermost_channel", ""))
        smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        userName.setText(preferenceHelper.getString("user_name", ""))
        userPhone.setText(preferenceHelper.getString("user_phone", ""))

        saveButton.setOnClickListener {
            val serverUrlText = serverUrl.text.toString().trim()
            val serverAuthTokenText = serverAuthToken.text.toString().trim()
            val mattermostWebhookText = mattermostWebhook.text.toString().trim()
            val mattermostChannelText = mattermostChannel.text.toString().trim()
            val smsNumberText = smsNumber.text.toString().trim()
            val userNameText = userName.text.toString().trim()
            val userPhoneText = userPhone.text.toString().trim()

            // Проверяем номер телефона
            if (smsNumberText.isNotBlank() && !smsNumberText.startsWith("+")) {
                Toast.makeText(this, 
                    "Phone number must start with '+' (format: +79123456789)", 
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                // Сохраняем все настройки
                preferenceHelper.saveString("server_url", serverUrlText)
                preferenceHelper.saveString("server_auth_token", serverAuthTokenText)
                preferenceHelper.saveString("mattermost_webhook", mattermostWebhookText)
                preferenceHelper.saveString("mattermost_channel", mattermostChannelText)
                preferenceHelper.saveString("sms_number", smsNumberText)
                preferenceHelper.saveString("user_name", userNameText)
                preferenceHelper.saveString("user_phone", userPhoneText)
                
                // Показываем успешное сообщение
                var successMessage = "✅ Settings saved!\n"
                
                if (serverUrlText.isNotBlank()) successMessage += "🌐 Server: Enabled\n"
                if (mattermostWebhookText.isNotBlank()) successMessage += "💬 Mattermost: Enabled\n"
                if (smsNumberText.isNotBlank()) successMessage += "📱 SMS: $smsNumberText\n"
                
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                finish()
                
            } catch (e: Exception) {
                Toast.makeText(this, "Save error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
