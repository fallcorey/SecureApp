package com.company.secureapp

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.util.Log

class LocationHelper(private val context: Context) {

    private val TAG = "LocationHelper"

    // Получить последнюю известную локацию
    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Пробуем получить GPS локацию
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            
            // Если GPS нет, пробуем сетевую локацию
            gpsLocation ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Location error: ${e.message}")
            null
        }
    }

    // Проверить разрешения на локацию
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Получить координаты в формате строки
    fun getLocationString(): String {
        val location = getLastKnownLocation()
        return if (location != null) {
            "Location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Location: Unable to get location"
        }
    }
}
