package com.company.secureapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class NetworkHelper(private val context: Context) {

    companion object {
        private const val TAG = "NetworkHelper"
    }

    // Получение информации о сети
    fun getNetworkInfo(): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            val networkType = when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "Unknown"
            }
            
            val carrierName = telephonyManager.networkOperatorName
            val signalStrength = "Unknown" // Для получения силы сигнала нужны дополнительные разрешения
            
            "Network: $networkType, Carrier: $carrierName, Signal: $signalStrength"
        } catch (e: Exception) {
            Log.e(TAG, "Network info error: ${e.message}")
            "Network: Unknown"
        }
    }

    // Проверка наличия интернета
    fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Internet check error: ${e.message}")
            false
        }
    }

    // Основной метод отправки с приоритетами
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
        
        return try {
            Log.d(TAG, "Starting emergency alert sequence...")
            
            val results = mutableListOf<String>()
            var successCount = 0
            
            // 1. ПРИОРИТЕТ: Отправка на сервер (если есть интернет и настройки)
            if (hasInternetConnection() && serverUrl.isNotBlank()) {
                val serverSuccess = sendToServer(serverUrl, authToken, createServerPayload(
                    userName, userPhone, locationInfo, networkInfo, audioRecorded, recordingTime
                ))
                
                if (serverSuccess) {
                    results.add("✓ Sent to server")
                    successCount++
                    Log.d(TAG, "Server send successful")
                } else {
                    results.add("✗ Failed to send to server")
                    Log.w(TAG, "Server send failed")
                }
            } else {
                if (!hasInternetConnection()) {
                    results.add("⚠ No internet for server")
                    Log.w(TAG, "No internet connection for server")
                } else if (serverUrl.isBlank()) {
                    results.add("⚠ No server URL configured")
                    Log.w(TAG, "Server URL not configured")
                }
            }
            
            // 2. РЕЗЕРВ: Отправка SMS (если есть номер)
            if (smsNumber.isNotBlank()) {
                val smsSuccess = sendSms(smsNumber, createSmsMessage(
                    userName, locationInfo, networkInfo, audioRecorded, recordingTime
                ))
                
                if (smsSuccess) {
                    results.add("✓ SMS sent")
                    successCount++
                    Log.d(TAG, "SMS send successful")
                } else {
                    results.add("✗ SMS failed")
                    Log.w(TAG, "SMS send failed")
                }
            } else {
                results.add("⚠ No SMS number configured")
                Log.w(TAG, "SMS number not configured")
            }
            
            // 3. ФИНАЛЬНЫЙ РЕЗУЛЬТАТ
            AlertResult(
                success = successCount > 0,
                messages = results,
                details = "Sent via: ${results.filter { it.startsWith("✓") }.joinToString(", ")}"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Emergency alert error: ${e.message}")
            AlertResult(
                success = false,
                messages = listOf("✗ System error: ${e.message}"),
                details = "Failed to send alert"
            )
        }
    }

    // Отправка на сервер
    private fun sendToServer(serverUrl: String, authToken: String, payload: String): Boolean {
        if (serverUrl.isBlank()) return false
        
        return try {
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SecureApp/1.0")
                
                // Добавляем токен если есть
                if (authToken.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $authToken")
                }
                
                connectTimeout = 10000
                readTimeout = 15000
                doOutput = true
            }

            // Отправка данных
            val outputStream = connection.outputStream
            outputStream.write(payload.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            Log.d(TAG, "Server response: $responseCode")
            
            connection.disconnect()
            responseCode in 200..299
            
        } catch (e: Exception) {
            Log.e(TAG, "Server request failed: ${e.message}")
            false
        }
    }

    // Отправка SMS
    fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS sent to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "SMS send failed: ${e.message}")
            false
        }
    }

    // Создание payload для сервера
    private fun createServerPayload(
        userName: String,
        userPhone: String,
        locationInfo: String,
        networkInfo: String,
        audioRecorded: Boolean,
        recordingTime: Long
    ): String {
        return JSONObject().apply {
            put("alert_id", "alert_${System.currentTimeMillis()}")
            put("event_type", "emergency_alert")
            put("timestamp", System.currentTimeMillis())
            
            put("user_data", JSONObject().apply {
                put("full_name", userName)
                put("phone_number", userPhone)
            })
            
            put("location_data", JSONObject().apply {
                put("info", locationInfo)
                put("timestamp", System.currentTimeMillis())
            })
            
            put("device_info", JSONObject().apply {
                put("network", networkInfo)
            })
            
            put("media", JSONObject().apply {
                put("audio_recording", JSONObject().apply {
                    put("available", audioRecorded)
                    put("duration_ms", recordingTime)
                })
            })
            
            put("additional_data", JSONObject().apply {
                put("app_version", "1.0.0")
                put("platform", "android")
            })
        }.toString()
    }

    // Создание SMS сообщения
    private fun createSmsMessage(
        userName: String,
        locationInfo: String,
        networkInfo: String,
        audioRecorded: Boolean,
        recordingTime: Long
    ): String {
        val recordingDuration = when (recordingTime) {
            30000L -> "30 seconds"
            60000L -> "1 minute"
            120000L -> "2 minutes"
            300000L -> "5 minutes"
            else -> "${recordingTime / 1000} seconds"
        }
        
        return """
            🚨 EMERGENCY from $userName!
            Need immediate assistance!
            
            📍 $locationInfo
            📶 $networkInfo
            ${if (audioRecorded) "🎤 Audio recording active ($recordingDuration)" else ""}
            
            Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
            """.trimIndent()
    }
}
