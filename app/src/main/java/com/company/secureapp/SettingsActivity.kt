package com.company.secureapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

class SettingsActivity : BaseActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var languageSpinner: Spinner
    private lateinit var recordingTimeSpinner: Spinner
    private var currentLanguage: String = "en"
    private var languageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceHelper = SimplePreferenceHelper(this)
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        val saveButton = findViewById<Button>(R.id.save_button)
        languageSpinner = findViewById<Spinner>(R.id.language_spinner)
        recordingTimeSpinner = findViewById<Spinner>(R.id.recording_time_spinner)
        val serverUrl = findViewById<EditText>(R.id.server_url)
        val serverAuthToken = findViewById<EditText>(R.id.server_auth_token)
        val smsNumber = findViewById<EditText>(R.id.sms_number)
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        // Настраиваем Spinner'ы
        setupLanguageSpinner()
        setupRecordingTimeSpinner()

        // Загружаем сохраненные настройки
        loadSavedSettings(serverUrl, serverAuthToken, smsNumber, userName, userPhone)

        // Обработчик выбора языка
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = when (position) {
                    0 -> "en"
                    1 -> "ru"
                    2 -> "es"
                    3 -> "fr"
                    else -> "en"
                }
                
                if (selectedLanguage != currentLanguage) {
                    languageChanged = true
                    currentLanguage = selectedLanguage
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        saveButton.setOnClickListener {
            saveSettings(serverUrl, serverAuthToken, smsNumber, userName, userPhone)
        }
    }

    private fun loadSavedSettings(
        serverUrl: EditText, serverAuthToken: EditText, 
        smsNumber: EditText, userName: EditText, userPhone: EditText
    ) {
        serverUrl.setText(preferenceHelper.getString("server_url", ""))
        serverAuthToken.setText(preferenceHelper.getString("server_auth_token", ""))
        smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        userName.setText(preferenceHelper.getString("user_name", ""))
        userPhone.setText(preferenceHelper.getString("user_phone", ""))

        // Загружаем сохраненный язык
        currentLanguage = sharedPreferences.getString("selected_language", "en") ?: "en"
        val languagePosition = when (currentLanguage) {
            "en" -> 0
            "ru" -> 1
            "es" -> 2
            "fr" -> 3
            else -> 0
        }
        languageSpinner.setSelection(languagePosition)

        // Загружаем сохраненное время записи
        val savedRecordingTime = preferenceHelper.getString("recording_time", "30000")
        val recordingTimeValues = resources.getStringArray(R.array.recording_time_values)
        val recordingTimePosition = recordingTimeValues.indexOf(savedRecordingTime).coerceAtLeast(0)
        recordingTimeSpinner.setSelection(recordingTimePosition)
    }

    private fun saveSettings(
        serverUrl: EditText, serverAuthToken: EditText, 
        smsNumber: EditText, userName: EditText, userPhone: EditText
    ) {
        val serverUrlText = serverUrl.text.toString().trim()
        val serverAuthTokenText = serverAuthToken.text.toString().trim()
        val smsNumberText = smsNumber.text.toString().trim()
        val userNameText = userName.text.toString().trim()
        val userPhoneText = userPhone.text.toString().trim()

        // Проверяем номер телефона
        if (smsNumberText.isNotBlank() && !smsNumberText.startsWith("+")) {
            showToast("Phone number must start with '+' (format: +79123456789)")
            return
        }

        // Проверяем что указан хотя бы один способ оповещения
        if (smsNumberText.isBlank() && serverUrlText.isBlank()) {
            showToast("Please set at least one alert method: SMS number or Server URL")
            return
        }

        try {
            // Сохраняем все настройки
            preferenceHelper.saveString("server_url", serverUrlText)
            preferenceHelper.saveString("server_auth_token", serverAuthTokenText)
            preferenceHelper.saveString("sms_number", smsNumberText)
            preferenceHelper.saveString("user_name", userNameText)
            preferenceHelper.saveString("user_phone", userPhoneText)

            // Сохраняем время записи
            val recordingTimeValues = resources.getStringArray(R.array.recording_time_values)
            val selectedRecordingTime = recordingTimeValues[recordingTimeSpinner.selectedItemPosition]
            preferenceHelper.saveString("recording_time", selectedRecordingTime)

            // Показываем статус записи аудио
            val recordingTime = selectedRecordingTime.toLongOrNull() ?: 30000
            val audioStatusMessage = if (recordingTime == 0L) {
                "Audio recording DISABLED (0 seconds)"
            } else {
                "Audio recording: ${recordingTime / 1000} seconds"
            }

            // Сохраняем язык только при изменении
            if (languageChanged) {
                changeLanguage(currentLanguage)
                showToast("Settings saved! $audioStatusMessage - Language changed to $currentLanguage")
                restartApp()
            } else {
                showToast("Settings saved! $audioStatusMessage")
                finish()
            }
            
        } catch (e: Exception) {
            showToast("Save error: ${e.message}")
        }
    }

    private fun restartApp() {
        finish()
    }

    private fun setupLanguageSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun setupRecordingTimeSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.recording_time_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recordingTimeSpinner.adapter = adapter
    }

    override fun onBackPressed() {
        if (languageChanged) {
            restartApp()
        } else {
            super.onBackPressed()
        }
    }
}
