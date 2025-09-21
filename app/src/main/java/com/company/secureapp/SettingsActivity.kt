package com.company.secureapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : BaseActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceHelper = SimplePreferenceHelper(this)
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Настройка Spinner для времени записи
        val recordingTimeSpinner: Spinner = findViewById(R.id.recording_time_spinner)
        val timeOptions = arrayOf("30 seconds", "1 minute", "2 minutes", "5 minutes")
        val timeValues = arrayOf("30000", "60000", "120000", "300000")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recordingTimeSpinner.adapter = adapter

        // Установка текущего значения
        val currentTime = preferenceHelper.getString("recording_time", "30000")
        val selectedIndex = timeValues.indexOf(currentTime).coerceAtLeast(0)
        recordingTimeSpinner.setSelection(selectedIndex)

        // Настройка Spinner для языка
        val languageSpinner: Spinner = findViewById(R.id.language_spinner)
        val languageOptions = arrayOf("English", "Russian", "Spanish", "French")
        val languageCodes = arrayOf("en", "ru", "es", "fr")
        
        val langAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = langAdapter

        // Установка текущего языка
        val currentLanguage = sharedPreferences.getString("selected_language", "en") ?: "en"
        val langIndex = languageCodes.indexOf(currentLanguage).coerceAtLeast(0)
        languageSpinner.setSelection(langIndex)

        // Кнопка сохранения
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            // Сохранение времени записи
            val selectedTime = timeValues[recordingTimeSpinner.selectedItemPosition]
            preferenceHelper.saveString("recording_time", selectedTime)

            // Сохранение языка и его применение
            val selectedLanguage = languageCodes[languageSpinner.selectedItemPosition]
            changeLanguage(selectedLanguage)

            showToast("Settings saved! Language: ${languageOptions[languageSpinner.selectedItemPosition]}")
            finish()
        }

        // Кнопка отмены - используйте правильный ID из вашего layout файла
        // Если у вас кнопка называется "cancel_button", то оставьте как есть
        // Если называется по-другому, замените R.id.cancel_button на правильный ID
        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            finish()
        }
    }
}
