package com.company.secureapp

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Looper
import com.google.android.gms.location.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LocationHelper(private val context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            callback(null)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                fusedLocationClient.removeLocationUpdates(this)
                callback(currentLocation)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Таймаут на случай, если локация не придет
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                fusedLocationClient.removeLocationUpdates(locationCallback)
                callback(currentLocation)
            }, 10000)
            
        } catch (e: SecurityException) {
            callback(null)
        } catch (e: Exception) {
            callback(null)
        }
    }

    fun getLastKnownLocation(): Location? {
        return if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?:
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun stopLocationUpdates() {
        // Cleanup if needed
    }
}
