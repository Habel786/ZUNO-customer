package com.example.data.firebase

import android.util.Log
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Labour
import com.example.data.model.ServiceCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

class FirestoreService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val customersCollection = firestore.collection("customers")
    private val laboursCollection = firestore.collection("labours")
    private val bookingsCollection = firestore.collection("bookings")

    // --- Customer Profile ---

    suspend fun getCustomerProfile(uid: String): Customer? {
        return try {
            val snapshot = customersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                Customer(
                    uid = snapshot.getString("uid") ?: uid,
                    name = snapshot.getString("name") ?: "",
                    mobile = snapshot.getString("mobile") ?: "",
                    email = snapshot.getString("email") ?: "",
                    city = snapshot.getString("city") ?: "",
                    role = snapshot.getString("role") ?: "customer",
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fetching customer profile", e)
            null
        }
    }

    fun observeCustomerProfile(uid: String): Flow<Customer?> = callbackFlow {
        val listener = customersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreService", "Listen error on customer profile", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val customer = Customer(
                    uid = snapshot.getString("uid") ?: uid,
                    name = snapshot.getString("name") ?: "",
                    mobile = snapshot.getString("mobile") ?: "",
                    email = snapshot.getString("email") ?: "",
                    city = snapshot.getString("city") ?: "",
                    role = snapshot.getString("role") ?: "customer",
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                trySend(customer)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateCustomerProfile(customer: Customer): Result<Unit> {
        return try {
            val updatedMap = customer.toMap().toMutableMap()
            updatedMap["updatedAt"] = System.currentTimeMillis()
            customersCollection.document(customer.uid).update(updatedMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error updating profile", e)
            Result.failure(e)
        }
    }

    // --- Labours / Service Workers ---

    fun observeAvailableLabours(): Flow<List<Labour>> = callbackFlow {
        val query = laboursCollection.whereEqualTo("dutyStatus", true)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreService", "Listen error on labours", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val labours = snapshot.documents.mapNotNull { doc ->
                    try {
                        Labour(
                            uid = doc.id,
                            name = doc.getString("name") ?: "Service Expert",
                            mobile = doc.getString("mobile") ?: "+91 98765 43210",
                            service = doc.getString("service") ?: "General Labour",
                            rate = doc.getDouble("rate") ?: (doc.getLong("rate")?.toDouble() ?: 500.0),
                            rating = doc.getDouble("rating") ?: 4.8,
                            dutyStatus = doc.getBoolean("dutyStatus") ?: true,
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            city = doc.getString("city") ?: "India",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
                trySend(labours)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeLabour(labourId: String): Flow<Labour?> = callbackFlow {
        if (labourId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = laboursCollection.document(labourId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreService", "Listen error on single labour $labourId", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val labour = Labour(
                    uid = snapshot.id,
                    name = snapshot.getString("name") ?: "Service Expert",
                    mobile = snapshot.getString("mobile") ?: "+91 98765 43210",
                    service = snapshot.getString("service") ?: "General Labour",
                    rate = snapshot.getDouble("rate") ?: (snapshot.getLong("rate")?.toDouble() ?: 500.0),
                    rating = snapshot.getDouble("rating") ?: 4.8,
                    dutyStatus = snapshot.getBoolean("dutyStatus") ?: true,
                    latitude = snapshot.getDouble("latitude") ?: 0.0,
                    longitude = snapshot.getDouble("longitude") ?: 0.0,
                    city = snapshot.getString("city") ?: "India",
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                trySend(labour)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    // --- Bookings ---

    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val bookingDoc = if (booking.bookingId.isNotBlank()) {
                bookingsCollection.document(booking.bookingId)
            } else {
                bookingsCollection.document()
            }
            val id = bookingDoc.id
            val finalBooking = booking.copy(
                bookingId = id,
                status = Booking.STATUS_PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            bookingDoc.set(finalBooking.toMap()).await()
            Log.d("FirestoreService", "Created booking doc: $id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error creating booking", e)
            Result.failure(e)
        }
    }

    fun observeCustomerBookings(customerId: String): Flow<List<Booking>> = callbackFlow {
        if (customerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = bookingsCollection
            .whereEqualTo("customerId", customerId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreService", "Error observing bookings", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookings = snapshot.documents.mapNotNull { doc ->
                    try {
                        Booking(
                            bookingId = doc.id,
                            customerId = doc.getString("customerId") ?: "",
                            customerName = doc.getString("customerName") ?: "",
                            customerMobile = doc.getString("customerMobile") ?: "",
                            labourId = doc.getString("labourId") ?: "",
                            service = doc.getString("service") ?: "",
                            rate = doc.getDouble("rate") ?: (doc.getLong("rate")?.toDouble() ?: 0.0),
                            status = doc.getString("status") ?: Booking.STATUS_PENDING,
                            customerLatitude = doc.getDouble("customerLatitude") ?: 0.0,
                            customerLongitude = doc.getDouble("customerLongitude") ?: 0.0,
                            labourLatitude = doc.getDouble("labourLatitude") ?: 0.0,
                            labourLongitude = doc.getDouble("labourLongitude") ?: 0.0,
                            labourName = doc.getString("labourName") ?: "",
                            labourMobile = doc.getString("labourMobile") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }
                trySend(bookings)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeBooking(bookingId: String): Flow<Booking?> = callbackFlow {
        if (bookingId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = bookingsCollection.document(bookingId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreService", "Error listening to booking $bookingId", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val booking = Booking(
                    bookingId = snapshot.id,
                    customerId = snapshot.getString("customerId") ?: "",
                    customerName = snapshot.getString("customerName") ?: "",
                    customerMobile = snapshot.getString("customerMobile") ?: "",
                    labourId = snapshot.getString("labourId") ?: "",
                    service = snapshot.getString("service") ?: "",
                    rate = snapshot.getDouble("rate") ?: (snapshot.getLong("rate")?.toDouble() ?: 0.0),
                    status = snapshot.getString("status") ?: Booking.STATUS_PENDING,
                    customerLatitude = snapshot.getDouble("customerLatitude") ?: 0.0,
                    customerLongitude = snapshot.getDouble("customerLongitude") ?: 0.0,
                    labourLatitude = snapshot.getDouble("labourLatitude") ?: 0.0,
                    labourLongitude = snapshot.getDouble("labourLongitude") ?: 0.0,
                    labourName = snapshot.getString("labourName") ?: "",
                    labourMobile = snapshot.getString("labourMobile") ?: "",
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                trySend(booking)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update(
                mapOf(
                    "status" to Booking.STATUS_CANCELLED,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error cancelling booking", e)
            Result.failure(e)
        }
    }

    /**
     * Demo helper for quick testing:
     * Seeds active verified labours around given coordinates in Firestore
     * so that discovery and real-time tracking work right away in any Indian city.
     */
    suspend fun seedSampleLaboursIfEmpty(centerLat: Double = 19.0760, centerLng: Double = 72.8777, cityName: String = "Mumbai"): Int {
        return try {
            val existing = laboursCollection.limit(1).get().await()
            if (!existing.isEmpty) {
                return 0
            }

            val sampleWorkers = listOf(
                Triple("Ramesh Kumar", "Carpenter", 650.0),
                Triple("Suresh Sharma", "Plumber", 600.0),
                Triple("Rajesh Verma", "Electrician", 600.0),
                Triple("Manoj Yadav", "Painter", 700.0),
                Triple("Sunil Patel", "General Labour", 500.0),
                Triple("Anil Gupta", "Mason", 750.0),
                Triple("Deepak Singh", "Cleaner", 450.0),
                Triple("Vikram Rao", "Driver", 550.0),
                Triple("Mohammad Aslam", "Plumber", 620.0),
                Triple("Santosh Shinde", "Electrician", 650.0),
                Triple("Pooja Devi", "Cleaner", 480.0),
                Triple("Gopal Das", "Carpenter", 680.0)
            )

            var count = 0
            for ((index, worker) in sampleWorkers.withIndex()) {
                val docId = "labour_${index + 101}"
                val latOffset = (Random.nextDouble() - 0.5) * 0.04
                val lngOffset = (Random.nextDouble() - 0.5) * 0.04
                val rating = 4.4 + Random.nextDouble() * 0.5
                val cleanRating = Math.round(rating * 10.0) / 10.0

                val labour = Labour(
                    uid = docId,
                    name = worker.first,
                    mobile = "+91 98${Random.nextInt(10000000, 99999999)}",
                    service = worker.second,
                    rate = worker.third,
                    rating = cleanRating,
                    dutyStatus = true,
                    latitude = centerLat + latOffset,
                    longitude = centerLng + lngOffset,
                    city = cityName,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                laboursCollection.document(docId).set(labour.toMap()).await()
                count++
            }
            Log.d("FirestoreService", "Seeded $count sample labours in Firestore")
            count
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error seeding sample labours", e)
            0
        }
    }
}
