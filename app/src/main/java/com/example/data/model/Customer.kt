package com.example.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Customer(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val city: String = "",
    val role: String = "customer",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "mobile" to mobile,
            "email" to email,
            "city" to city,
            "role" to role,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
