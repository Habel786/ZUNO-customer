package com.example.data.model

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Booking(
    @DocumentId
    val bookingId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val labourId: String = "",
    val service: String = "",
    val rate: Double = 0.0,
    val status: String = STATUS_PENDING,
    val customerLatitude: Double = 0.0,
    val customerLongitude: Double = 0.0,
    val labourLatitude: Double = 0.0,
    val labourLongitude: Double = 0.0,
    val labourName: String = "",
    val labourMobile: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_COMPLETED = "completed"
    }

    val isPending: Boolean get() = status.equals(STATUS_PENDING, ignoreCase = true)
    val isAccepted: Boolean get() = status.equals(STATUS_ACCEPTED, ignoreCase = true)
    val isRejected: Boolean get() = status.equals(STATUS_REJECTED, ignoreCase = true)
    val isCancelled: Boolean get() = status.equals(STATUS_CANCELLED, ignoreCase = true)
    val isCompleted: Boolean get() = status.equals(STATUS_COMPLETED, ignoreCase = true)

    val formattedDate: String
        get() {
            return try {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                sdf.format(Date(createdAt))
            } catch (e: Exception) {
                "Recent"
            }
        }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "bookingId" to bookingId,
            "customerId" to customerId,
            "customerName" to customerName,
            "customerMobile" to customerMobile,
            "labourId" to labourId,
            "service" to service,
            "rate" to rate,
            "status" to status,
            "customerLatitude" to customerLatitude,
            "customerLongitude" to customerLongitude,
            "labourLatitude" to labourLatitude,
            "labourLongitude" to labourLongitude,
            "labourName" to labourName,
            "labourMobile" to labourMobile,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
