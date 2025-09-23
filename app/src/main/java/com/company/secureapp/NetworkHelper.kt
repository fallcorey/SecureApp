package com.company.secureapp

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class NetworkHelper(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    data class AlertResult(val success: Boolean, val messages: List<String>, val details: String = "")

    // ОСНОВНОЙ ИСПРАВЛЕННЫЙ МЕТОД
    fun sendEmergencyAlert(
        userName: String,
        userPhone: String,
        locationInfo: String,
        networkInfo: String,
        audioRecorded: Boolean,
        recordingTime: Long,
        serverUrl: String,
        authToken: String,
        smsNumber: String
    ): AlertResult {
        val messages = mutableListOf<String>()
        var success = false
        var details = ""

        try {
            Log.d("NetworkHelper", "=== Starting emergency alert procedure ===")
            Log.d("NetworkHelper", "Network available: ${isNetworkAvailable()}")
            Log.d("NetworkHelper", "SMS number provided: ${smsNumber.isNotBlank()}")
            Log.d("NetworkHelper", "SMS permission: ${hasSmsPermission()}")

            // ВАЖНО: SMS должны иметь ВЫСШИЙ ПРИОРИТЕТ при отсутствии интернета
            val networkAvailable = isNetworkAvailable()
            
            if (!networkAvailable) {
                Log.d("NetworkHelper", "No network - attempting SMS only")
                messages.add("Network: Unavailable")
                
                // ПРИ ОТКЛЮЧЕННОМ ИНТЕРНЕТЕ - ПЫТАЕМСЯ ОТПРАВИТЬ SMS СРАЗУ
                if (smsNumber.isNotBlank() && hasSmsPermission()) {
                    val smsMessage = createEmergencyMessage(userName, userPhone, locationInfo, networkInfo, audioRecorded)
                    val smsResult = sendEmergencySMS(smsNumber, smsMessage)
                    
                    if (smsResult.success) {
                        messages.add("SMS: Sent successfully (no network)")
                        success = true
                        details = "Alert sent via SMS (no internet)"
                        Log.d("NetworkHelper", "SMS sent successfully without network")
                    } else {
                        messages.add("SMS: Failed - ${smsResult.details}")
                        details = "SMS failed: ${smsResult.details}"
                        Log.e("NetworkHelper", "SMS failed without network: ${smsResult.details}")
                    }
                } else {
                    val errorMsg = when {
                        smsNumber.isBlank() -> "SMS number not configured"
                        !hasSmsPermission() -> "SMS permission denied"
                        else -> "SMS not available"
                    }
                    messages.add("SMS: Cannot send - $errorMsg")
                    details = errorMsg
                    Log.e("NetworkHelper", "Cannot send SMS: $errorMsg")
                }
            } else {
                Log.d("NetworkHelper", "Network available - attempting all methods")
                messages.add("Network: Available")
                
                var serverSuccess = false
                var smsSuccess = false
                
                // Пытаемся отправить на сервер (если есть URL)
                if (serverUrl.isNotBlank()) {
                    val serverResult = sendToServer(userName, userPhone, locationInfo, networkInfo, 
                        audioRecorded, recordingTime, serverUrl, authToken)
                    
                    if (serverResult.success) {
                        messages.add("Server: Success")
                        serverSuccess = true
                        Log.d("NetworkHelper", "Server alert sent successfully")
                    } else {
                        messages.add("Server: Failed - ${serverResult.details}")
                        Log.e("NetworkHelper", "Server alert failed: ${serverResult.details}")
                    }
                } else {
                    messages.add("Server: Not configured")
                }
                
                // Пытаемся отправить SMS (параллельно или как fallback)
                if (smsNumber.isNotBlank() && hasSmsPermission()) {
                    val smsMessage = createEmergencyMessage(userName, userPhone, locationInfo, networkInfo, audioRecorded)
                    val smsResult = sendEmergencySMS(smsNumber, smsMessage)
                    
                    if (smsResult.success) {
                        messages.add("SMS: Success")
                        smsSuccess = true
                        Log.d("NetworkHelper", "SMS sent successfully")
                    } else {
                        messages.add("SMS: Failed - ${smsResult.details}")
                        Log.e("NetworkHelper", "SMS failed: ${smsResult.details}")
                    }
                } else {
                    messages.add("SMS: Not configured or no permission")
                }
                
                success = serverSuccess || smsSuccess
                details = when {
                    serverSuccess && smsSuccess -> "Alert sent to server and via SMS"
                    serverSuccess -> "Alert sent to server"
                    smsSuccess -> "Alert sent via SMS"
                    else -> "All delivery methods failed"
                }
            }
            
        } catch (e: Exception) {
            Log.e("NetworkHelper", "Error in sendEmergencyAlert: ${e.message}")
            messages.add("Error: ${e.message}")
            details = "Exception: ${e.message}"
        }
        
        Log.d("NetworkHelper", "=== Alert procedure completed: success=$success ===")
        return AlertResult(success, messages, details)
    }

    // Улучшенный метод отправки SMS
    private fun sendEmergencySMS(phoneNumber: String, message: String): AlertResult {
        return try {
            Log.d("NetworkHelper", "Attempting to send SMS to: $phoneNumber")
            Log.d("NetworkHelper", "SMS message length: ${message.length}")
            
            // Проверяем номер телефона
            if (phoneNumber.isBlank() || phoneNumber.length < 5) {
                return AlertResult(false, listOf(), "Invalid phone number")
            }
            
            // Проверяем разрешение
            if (!hasSmsPermission()) {
                return AlertResult(false, listOf(), "No SMS permission")
            }
            
            val smsManager = SmsManager.getDefault()
            
            // Разбиваем длинное сообщение на части
            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                Log.d("NetworkHelper", "Long SMS sent in ${parts.size} parts")
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("NetworkHelper", "Single SMS sent")
            }
            
            AlertResult(true, listOf(), "SMS delivered")
            
        } catch (e: SecurityException) {
            Log.e("NetworkHelper", "SecurityException sending SMS: ${e.message}")
            AlertResult(false, listOf(), "SMS permission denied")
        } catch (e: IllegalArgumentException) {
            Log.e("NetworkHelper", "IllegalArgumentException sending SMS: ${e.message}")
            AlertResult(false, listOf(), "Invalid destination or message")
        } catch (e: Exception) {
            Log.e("NetworkHelper", "Exception sending SMS: ${e.message}")
            AlertResult(false, listOf(), "SMS failed: ${e.message}")
        }
    }

    // Проверка разрешения SMS (должна быть в MainActivity)
    private fun hasSmsPermission(): Boolean {
        return android.content.pm.PackageManager.PERMISSION_GRANTED == 
            context.checkSelfPermission(android.Manifest.permission.SEND_SMS)
    }

    // Проверка доступности сети
    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnectedOrConnecting == true
        } catch (e: Exception) {
            Log.e("NetworkHelper", "Error checking network: ${e.message}")
            false
        }
    }

    // Создание сообщения для SMS
    private fun createEmergencyMessage(
        userName: String,
        userPhone: String,
        locationInfo: String,
        networkInfo: String,
        audioRecorded: Boolean
    ): String {
        return """
            🚨 EMERGENCY ALERT 🚨
            Name: $userName
            Phone: $userPhone
            Location: $locationInfo
            Network: $networkInfo
            Audio: ${if (audioRecorded) "Recorded" else "Not available"}
            Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
            """.trimIndent()
    }

    // Метод отправки на сервер (без изменений)
    private fun sendToServer(...): AlertResult {
        // ... существующий код ...
    }
}
