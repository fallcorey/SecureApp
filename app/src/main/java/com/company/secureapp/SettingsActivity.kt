package com.company.secureapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton  // ДОБАВЬТЕ ЭТОТ ИМПОРТ
import com.google.android.material.textfield.TextInputEditText  // ДОБАВЬТЕ ЭТОТ ИМПОРТ

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "📱 SettingsActivity started")
            setContentView(R.layout.activity_settings)
            Log.d(TAG, "✅ Layout loaded")
            
            // ИСПРАВЬТЕ НА MaterialButton ▼▼▼
            val saveButton = findViewById<MaterialButton>(R.id.save_button)
            Log.d(TAG, "🔍 Save button: $saveButton")
            
            if (saveButton == null) {
                Log.e(TAG, "❌ Save button not found!")
                Toast.makeText(this, "Save button not found", Toast.LENGTH_LONG).show()
                return
            }
            
            // Находим все EditText поля (ТОЖЕ Material компоненты)
            val serverUrl = findViewById<TextInputEditText>(R.id.server_endpoint_url)
            val smsNumber = findViewById<TextInputEditText>(R.id.sms_number)
            val userName = findViewById<TextInputEditText>(R.id.user_full_name)
            val userPhone = findViewById<TextInputEditText>(R.id.user_phone_number)
            
            saveButton.setOnClickListener {
                Log.d(TAG, "🎯 Save button clicked")
                
                Toast.makeText(this, 
                    "Settings saved!\n" +
                    "Server: ${serverUrl.text}\n" +
                    "SMS: ${smsNumber.text}\n" +
                    "Name: ${userName.text}\n" +
                    "Phone: ${userPhone.text}", 
                    Toast.LENGTH_LONG).show()
                
                finish()
            }
            
            Log.d(TAG, "✅ SettingsActivity ready")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
