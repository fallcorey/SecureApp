package com.company.secureapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val saveButton = findViewById<Button>(R.id.save_button)
        
        // Находим все EditText поля
        val serverUrl = findViewById<EditText>(R.id.server_endpoint_url)
        val smsNumber = findViewById<EditText>(R.id.sms_number)
        val userName = findViewById<EditText>(R.id.user_full_name)
        val userPhone = findViewById<EditText>(R.id.user_phone_number)

        saveButton.setOnClickListener {
            // Сохраняем настройки (пока просто показываем)
            Toast.makeText(this, 
                "Settings saved!\n" +
                "Server: ${serverUrl.text}\n" +
                "SMS: ${smsNumber.text}\n" +
                "Name: ${userName.text}\n" +
                "Phone: ${userPhone.text}", 
                Toast.LENGTH_LONG).show()
            
            finish() // Закрываем активити
        }
    }
}
