package com.company.secureapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.secureapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)
        loadSettings()
        
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        binding.mattermostServerUrl.setText(preferenceHelper.getString("mattermost_server_url", ""))
        binding.channelId.setText(preferenceHelper.getString("channel_id", ""))
        binding.login.setText(preferenceHelper.getString("login", ""))
        binding.token.setText(preferenceHelper.getString("token", ""))
        binding.smsNumber.setText(preferenceHelper.getString("sms_number", ""))
        binding.serverEndpointUrl.setText(preferenceHelper.getString("server_endpoint_url", ""))
        binding.userFullName.setText(preferenceHelper.getString("user_full_name", ""))
        binding.userPhoneNumber.setText(preferenceHelper.getString("user_phone_number", ""))
        binding.mattermostEnabled.isChecked = preferenceHelper.getBoolean("mattermost_enabled", false)
    }

    private fun saveSettings() {
        try {
            preferenceHelper.saveString("mattermost_server_url", binding.mattermostServerUrl.text.toString())
            preferenceHelper.saveString("channel_id", binding.channelId.text.toString())
            preferenceHelper.saveString("login", binding.login.text.toString())
            preferenceHelper.saveString("token", binding.token.text.toString())
            preferenceHelper.saveString("sms_number", binding.smsNumber.text.toString())
            preferenceHelper.saveString("server_endpoint_url", binding.serverEndpointUrl.text.toString())
            preferenceHelper.saveString("user_full_name", binding.userFullName.text.toString())
            preferenceHelper.saveString("user_phone_number", binding.userPhoneNumber.text.toString())
            preferenceHelper.saveBoolean("mattermost_enabled", binding.mattermostEnabled.isChecked)

            Toast.makeText(this, "Settings saved securely", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
