package com.company.secureapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_settings)
            
            // Пока используем только кнопку, без EditText
            val saveButton = findViewById<Button>(R.id.save_button)

            saveButton.setOnClickListener {
                Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Закрываем активити
            }
            
        } catch (e: Exception) {
            // Если ошибка - показываем и закрываем
            Toast.makeText(this, "Settings error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
