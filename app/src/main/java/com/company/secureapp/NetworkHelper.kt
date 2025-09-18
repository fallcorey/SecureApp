package com.company.secureapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.SmsManager
import android.util.Log

class NetworkHelper(private val context: Context) {

    private val TAG = "NetworkHelper"

    // Проверить есть ли интернет
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Отправить SMS (улучшенная версия)
    fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            if (phoneNumber.isBlank()) {
                Log.e(TAG, "Phone number is empty")
                return false
            }

            val smsManager = SmsManager.getDefault()
            
            // Если сообщение слишком длинное, разбиваем на части
            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            
            Log.d(TAG, "SMS sent successfully to: $phoneNumber")
            true
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SMS permission denied: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid phone number: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "SMS failed: ${e.message}")
            false
        }
    }

    // Получить информацию о сети
    fun getNetworkInfo(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
        } else {
            "No network"
        }
    }
}
