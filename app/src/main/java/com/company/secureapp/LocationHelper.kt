package com.company.secureapp

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LocationHelper(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            callback(null)
            return
        }

        var location: Location? = null
        val latch = CountDownLatch(1)

        val locationListener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                location = loc
                latch.countDown()
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper())
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper())
            
            // Wait for location with timeout
            latch.await(10, TimeUnit.SECONDS)
            callback(location)
        } catch (e: SecurityException) {
            callback(null)
        } catch (e: Exception) {
            callback(null)
        }
    }

    fun stopLocationUpdates() {
        // Cleanup if needed
    }
}
