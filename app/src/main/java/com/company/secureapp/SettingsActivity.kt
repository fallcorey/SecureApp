package com.company.secureapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "SettingsActivity started")
            setContentView(R.layout.activity_settings)
            Log.d(TAG, "Layout loaded")
            
            // Теперь используем обычные Button и EditText
            val saveButton = findViewById<Button>(R.id.save_button)
            val smsNumber = findViewById<EditText>(R.id.sms_number)
            val serverUrl = findViewById<EditText>(R.id.server_endpoint_url)
            val userName = findViewById<EditText>(R.id.user_full_name)
            val userPhone = findViewById<EditText>(R.id.user_phone_number)
            
            saveButton.setOnClickListener {
                Toast.makeText(this, 
                    "Settings saved!\n" +
                    "Server: ${serverUrl.text}\n" +
                    "SMS: ${smsNumber.text}\n" +
                    "Name: ${userName.text}\n" +
                    "Phone: ${userPhone.text}", 
                    Toast.LENGTH_LONG).show()
                finish()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Settings error", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
