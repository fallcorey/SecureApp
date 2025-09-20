package com.company.secureapp

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ServerHelper(private val context: Context) {

    private val TAG = "ServerHelper"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Отправить данные на сервер
    fun sendToServer(serverUrl: String, authToken: String?, data: Map<String, String>): Boolean {
        if (serverUrl.isBlank()) return false

        return try {
            val json = createJsonPayload(data)
            val requestBuilder = Request.Builder()
                .url(serverUrl)
                .post(json.toRequestBody(JSON))

            // Добавляем авторизацию если есть токен
            authToken?.takeIf { it.isNotBlank() }?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                if (success) {
                    Log.d(TAG, "Server response: ${response.body?.string()}")
                } else {
                    Log.e(TAG, "Server error: ${response.code} - ${response.message}")
                }
                success
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Server error: ${e.message}")
            false
        }
    }

    // Отправить в Mattermost
    fun sendToMattermost(webhookUrl: String, channel: String?, data: Map<String, String>): Boolean {
        if (webhookUrl.isBlank()) return false

        return try {
            val mattermostPayload = createMattermostPayload(data, channel)
            val request = Request.Builder()
                .url(webhookUrl)
                .post(mattermostPayload.toRequestBody(JSON))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Mattermost error: ${e.message}")
            false
        }
    }

    private fun createJsonPayload(data: Map<String, String>): String {
        val jsonEntries = data.entries.joinToString(",\n    ") { 
            "\"${it.key}\": \"${it.value}\"" 
        }
        return "{\n    $jsonEntries\n}"
    }

    private fun createMattermostPayload(data: Map<String, String>, channel: String?): String {
        val text = data.entries.joinToString("\n") { "**${it.key}**: ${it.value}" }
        val channelPart = channel?.takeIf { it.isNotBlank() } ?: ""
        return """{
            "text": "$text",
            "channel": "$channelPart"
        }""".trimIndent()
    }

    // Проверить доступность сервера
    fun isServerAvailable(serverUrl: String): Boolean {
        if (serverUrl.isBlank()) return false
        
        return try {
            val request = Request.Builder()
                .url(serverUrl)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}
