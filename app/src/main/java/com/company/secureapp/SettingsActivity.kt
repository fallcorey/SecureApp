package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

class SettingsActivity : BaseActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var languageSpinner: Spinner
    private var currentLanguage: String = "en"
    private var languageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceHelper = SimplePreferenceHelper(this)

        val saveButton = findViewById<Button>(R.id.save_button)
        languageSpinner = findViewById<Spinner>(R.id.language_spinner)
        val serverUrl = findViewById<EditText>(R.id.server_url)
        val serverAuthToken = findViewById<EditText>(R.id.server_auth_token)
        val mattermostWebhook = findViewById<EditText>(R.id.mattermost_webhook)
        val mattermostChannel = findViewById<EditText>(R.id.mattermost_channel)
        val smsNumber = findViewById<EditText>(R.id.sms_number)
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        // Настраиваем Spinner
        setupLanguageSpinner()

        // Загружаем сохраненные настройки
        serverUrl.setText(preferenceHelper.getString("server_url", ""))
        serverAuthToken.setText(preferenceHelper.getString("server_auth_token", ""))
        mattermostWebhook.setText(preferenceHelper.getString("mattermost_webhook", ""))
        mattermostChannel.setText(preferenceHelper.getString("mattermost_channel", ""))
        smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        userName.setText(preferenceHelper.getString("user_name", ""))
        userPhone.setText(preferenceHelper.getString("user_phone", ""))

        // Загружаем сохраненный язык
        currentLanguage = preferenceHelper.getString("app_language", "en")
        
        // Устанавливаем правильную позицию в Spinner
        val position = when (currentLanguage) {
            "en" -> 0 // English на позиции 0
            "ru" -> 1 // Русский на позиции 1
            else -> 0
        }
        languageSpinner.setSelection(position)

        // Обработчик выбора языка
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = when (position) {
                    0 -> "en" // English
                    1 -> "ru" // Русский
                    else -> "en"
                }
                
                if (selectedLanguage != currentLanguage) {
                    // Меняем язык сразу при выборе
                    preferenceHelper.saveString("app_language", selectedLanguage)
                    changeLanguage(selectedLanguage)
                    languageChanged = true
                    currentLanguage = selectedLanguage // Обновляем текущий язык
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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
                showToast("Phone number must start with '+' (format: +79123456789)")
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

                showToast(R.string.settings_saved)
                finish()
                
            } catch (e: Exception) {
                showToast("Save error: ${e.message}")
            }
        }
    }

    private fun setupLanguageSpinner() {
        // Создаем адаптер для Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    override fun onBackPressed() {
        if (languageChanged) {
            // При смене языка перезапускаем главный экран
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
