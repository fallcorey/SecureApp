package com.company.secureapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: SimplePreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceHelper = SimplePreferenceHelper(this)

        val sosButton = findViewById<Button>(R.id.sos_button)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        sosButton.setOnClickListener {
            // Используем сохраненные настройки
            val smsNumber = preferenceHelper.getString("sms_number", "+1234567890")
            val userName = preferenceHelper.getString("user_name", "User")
            
            Toast.makeText(this, 
                "🚨 Emergency alert for: $smsNumber\n👤 From: $userName", 
                Toast.LENGTH_LONG).show()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
