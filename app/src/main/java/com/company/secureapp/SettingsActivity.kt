package com.company.secureapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "SettingsActivity started")
            setContentView(R.layout.activity_settings)
            Log.d(TAG, "Layout set successfully")
            
            // Находим только кнопку (остальные элементы пока не используем)
            val saveButton = findViewById<Button>(R.id.save_button)
            Log.d(TAG, "Save button found")
            
            saveButton.setOnClickListener {
                // Временное сообщение без использования EditText
                Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Закрываем активити
            }
            
            Log.d(TAG, "SettingsActivity created successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in SettingsActivity: ${e.message}", e)
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
