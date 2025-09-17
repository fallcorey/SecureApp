package com.company.secureapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "SecureAppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "onCreate started")
            setContentView(R.layout.activity_main)
            Log.d(TAG, "ContentView set successfully")

            // ИСПРАВЬТЕ НАЗАД НА ОБЫЧНЫЕ BUTTON ▼▼▼
            val sosButton = findViewById<Button>(R.id.sos_button)
            val settingsButton = findViewById<Button>(R.id.settings_button)
            // ▲▲▲ ИСПРАВЬТЕ НАЗАД

            sosButton.setOnClickListener {
                Toast.makeText(this, "SOS button clicked", Toast.LENGTH_SHORT).show()
            }

            settingsButton.setOnClickListener {
                Toast.makeText(this, "Settings button clicked", Toast.LENGTH_SHORT).show()
            }

            Log.d(TAG, "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "App error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
