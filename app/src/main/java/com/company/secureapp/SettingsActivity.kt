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
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        // Загружаем сохраненные настройки
        smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        userName.setText(preferenceHelper.getString("user_name", ""))
        userPhone.setText(preferenceHelper.getString("user_phone", ""))

        saveButton.setOnClickListener {
            // Сохраняем настройки
            preferenceHelper.saveString("sms_number", smsNumber.text.toString())
            preferenceHelper.saveString("user_name", userName.text.toString())
            preferenceHelper.saveString("user_phone", userPhone.text.toString())
            
            Toast.makeText(this, "✅ Settings saved!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
