package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "Mumbai, Maharashtra",
    val address: String = "Current Location",
    val isRealGps: Boolean = false
)

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Default reference location in India (e.g. Central Mumbai/Delhi/Bengaluru)
    val defaultLocation = UserLocation(
        latitude = 19.0760,
        longitude = 72.8777,
        cityName = "Mumbai, India",
        address = "Bandra West, Mumbai",
        isRealGps = false
    )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                val geocoded = getCityAndAddress(location.latitude, location.longitude)
                UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = geocoded.first,
                    address = geocoded.second,
                    isRealGps = true
                )
            } else {
                // Try last known location
                val lastLoc = fusedLocationClient.lastLocation.await()
                if (lastLoc != null) {
                    val geocoded = getCityAndAddress(lastLoc.latitude, lastLoc.longitude)
                    UserLocation(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        cityName = geocoded.first,
                        address = geocoded.second,
                        isRealGps = true
                    )
                } else {
                    defaultLocation
                }
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error obtaining location", e)
            defaultLocation
        }
    }

    private fun getCityAndAddress(lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale("en", "IN"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "India"
                val state = addr.adminArea ?: ""
                val cityName = if (state.isNotBlank() && !locality.contains(state)) "$locality, $state" else locality
                val fullAddress = addr.getAddressLine(0) ?: "$locality, India"
                Pair(cityName, fullAddress)
            } else {
                Pair("India", "Current Location")
            }
        } catch (e: Exception) {
            Pair("India", "Current Location")
        }
    }

    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 0.0
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun formatDistance(distanceKm: Double): String {
            return if (distanceKm < 1.0) {
                "${(distanceKm * 1000).roundToInt()} m"
            } else {
                String.format(Locale.ENGLISH, "%.1f km", distanceKm)
            }
        }
    }
}
