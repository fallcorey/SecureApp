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
            Log.d(TAG, "📱 SettingsActivity started")
            setContentView(R.layout.activity_settings)
            Log.d(TAG, "✅ Layout loaded")
            
            // Пробуем найти кнопку
            val saveButton = findViewById<Button>(R.id.save_button)
            Log.d(TAG, "🔍 Save button: $saveButton")
            
            if (saveButton == null) {
                Log.e(TAG, "❌ Save button not found!")
                Toast.makeText(this, "Save button not found", Toast.LENGTH_LONG).show()
                return
            }
            
            // Добавляем обработчик клика
            saveButton.setOnClickListener {
                Log.d(TAG, "🎯 Save button clicked")
                Toast.makeText(this, "💾 Settings saved!", Toast.LENGTH_SHORT).show()
                finish() // Закрываем экран настроек
            }
            
            Log.d(TAG, "✅ SettingsActivity ready")
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
