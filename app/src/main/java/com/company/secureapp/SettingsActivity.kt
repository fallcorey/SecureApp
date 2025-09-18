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
            preferenceHelper = SimplePreferenceHelper(this)
            setContentView(R.layout.activity_settings)
            
            val saveButton = findViewById<Button>(R.id.save_button)
            val smsNumber = findViewById<EditText>(R.id.sms_number)
            val serverUrl = findViewById<EditText>(R.id.server_endpoint_url)
            val userName = findViewById<EditText>(R.id.user_full_name)
            val userPhone = findViewById<EditText>(R.id.user_phone_number)

            // Загрузка сохраненных настроек
            smsNumber.setText(preferenceHelper.getString("sms_number", ""))
            serverUrl.setText(preferenceHelper.getString("server_url", ""))
            userName.setText(preferenceHelper.getString("user_name", ""))
            userPhone.setText(preferenceHelper.getString("user_phone", ""))
            
            saveButton.setOnClickListener {
                try {
                    // Сохранение настроек
                    preferenceHelper.saveString("sms_number", smsNumber.text.toString())
                    preferenceHelper.saveString("server_url", serverUrl.text.toString())
                    preferenceHelper.saveString("user_name", userName.text.toString())
                    preferenceHelper.saveString("user_phone", userPhone.text.toString())
                    
                    Toast.makeText(this, "✅ Settings saved!", Toast.LENGTH_LONG).show()
                    finish()
                    
                } catch (e: Exception) {
                    Toast.makeText(this, "❌ Save error", Toast.LENGTH_LONG).show()
                }
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Settings error", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
