package com.company.secureapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.telephony.SmsManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class NetworkHelper(private val context: Context) {

    private val client = OkHttpClient()

    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun sendToMattermost(serverUrl: String, channelId: String, login: String, token: String, message: String, audioFile: Uri?) {
        val url = "$serverUrl/hooks/$channelId"
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("payload", """{"text":"$message","username":"$login"}""")
            .apply {
                audioFile?.let { uri ->
                    val file = File(uri.path ?: return@let)
                    addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("audio/*".toMediaType())
                    )
                }
            }
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
            }
        })
    }

    fun sendToServer(endpointUrl: String, message: String, audioFile: Uri?) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("message", message)
            .apply {
                audioFile?.let { uri ->
                    val file = File(uri.path ?: return@let)
                    addFormDataPart(
                        "audio",
                        file.name,
                        file.asRequestBody("audio/*".toMediaType())
                    )
                }
            }
            .build()

        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
            }
        })
    }

    fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            // Handle SMS sending failure
        }
    }
}
