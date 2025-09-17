package com.company.secureapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sosButton = findViewById<Button>(R.id.sos_button)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        sosButton.setOnClickListener {
            Toast.makeText(this, "SOS button clicked", Toast.LENGTH_SHORT).show()
        }

        settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings button clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
