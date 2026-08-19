package com.example.data.model

import com.google.firebase.firestore.DocumentId
import kotlin.math.*

data class Labour(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val mobile: String = "",
    val service: String = "General Labour",
    val rate: Double = 0.0,
    val rating: Double = 4.8,
    val dutyStatus: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculates distance to customer location in kilometers using Haversine formula
     */
    fun calculateDistanceKm(customerLat: Double, customerLng: Double): Double {
        if (latitude == 0.0 || longitude == 0.0 || customerLat == 0.0 || customerLng == 0.0) {
            return 0.0
        }
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(latitude - customerLat)
        val dLng = Math.toRadians(longitude - customerLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(customerLat)) * cos(Math.toRadians(latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formattedDistance(customerLat: Double, customerLng: Double): String {
        val dist = calculateDistanceKm(customerLat, customerLng)
        return if (dist < 1.0) {
            "${(dist * 1000).roundToInt()} m away"
        } else {
            String.format("%.1f km away", dist)
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "mobile" to mobile,
            "service" to service,
            "rate" to rate,
            "rating" to rating,
            "dutyStatus" to dutyStatus,
            "latitude" to latitude,
            "longitude" to longitude,
            "city" to city,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
