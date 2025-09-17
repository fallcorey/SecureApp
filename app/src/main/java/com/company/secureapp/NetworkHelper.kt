package com.company.secureapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.SmsManager
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NetworkHelper(private val context: Context) {

    private val client = OkHttpClient()

    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun sendToMattermost(serverUrl: String, channelId: String, login: String, token: String, message: String) {
        val url = "$serverUrl/api/v4/posts"
        
        val json = """
        {
            "channel_id": "$channelId",
            "message": "$message"
        }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Логирование ошибки
                android.util.Log.e("NetworkHelper", "Mattermost send failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    android.util.Log.e("NetworkHelper", "Mattermost error: ${response.code} - ${response.body?.string()}")
                }
            }
        })
    }

    fun sendToServer(endpointUrl: String, message: String) {
        val json = """
        {
            "message": "$message",
            "timestamp": "${System.currentTimeMillis()}"
        }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("NetworkHelper", "Server send failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    android.util.Log.e("NetworkHelper", "Server error: ${response.code}")
                }
            }
        })
    }

    fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = context.getSystemService(Context.SMS_SERVICE) as SmsManager
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            android.util.Log.e("NetworkHelper", "SMS send failed: ${e.message}")
            Toast.makeText(context, "SMS sending failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
