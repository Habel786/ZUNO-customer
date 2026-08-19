package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.location.LocationHelper
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Labour
import com.example.data.model.ServiceCategory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun read_appName_fromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZUNO Customer", appName)
    }

    @Test
    fun test_serviceCategories_count() {
        assertEquals(8, ServiceCategory.ALL_SERVICES.size)
        val names = ServiceCategory.ALL_SERVICES.map { it.name }
        assertTrue(names.contains("General Labour"))
        assertTrue(names.contains("Carpenter"))
        assertTrue(names.contains("Plumber"))
        assertTrue(names.contains("Electrician"))
        assertTrue(names.contains("Painter"))
        assertTrue(names.contains("Mason"))
        assertTrue(names.contains("Cleaner"))
        assertTrue(names.contains("Driver"))
    }

    @Test
    fun test_distanceCalculation() {
        // Mumbai CST to Bandra West (~13-14 km)
        val lat1 = 18.9401
        val lon1 = 72.8347
        val lat2 = 19.0596
        val lon2 = 72.8295

        val distance = LocationHelper.calculateDistanceKm(lat1, lon1, lat2, lon2)
        assertTrue("Distance should be approximately 13-14 km", distance in 12.0..16.0)

        val formatted = LocationHelper.formatDistance(distance)
        assertTrue(formatted.contains("km"))
    }

    @Test
    fun test_customer_model_serialization() {
        val customer = Customer(
            uid = "cust_123",
            name = "Aakash Mehta",
            mobile = "+91 98200 12345",
            email = "aakash@example.com",
            city = "Mumbai",
            role = "customer"
        )
        val map = customer.toMap()
        assertEquals("cust_123", map["uid"])
        assertEquals("Aakash Mehta", map["name"])
        assertEquals("customer", map["role"])
    }

    @Test
    fun test_firebase_configuration() {
        assertEquals("zuno-904d6", com.example.data.firebase.FirebaseConfig.PROJECT_ID)
        assertEquals("AIzaSyAX4sbRjraRJQ9IfiCEe0x9N5WO-Nlx8sI", com.example.data.firebase.FirebaseConfig.API_KEY)
        assertEquals("zuno-904d6.firebaseapp.com", com.example.data.firebase.FirebaseConfig.AUTH_DOMAIN)
        assertEquals("zuno-904d6.firebasestorage.app", com.example.data.firebase.FirebaseConfig.STORAGE_BUCKET)
    }

    @Test
    fun test_booking_status_flags() {
        val pendingBooking = Booking(
            bookingId = "bk_001",
            customerId = "cust_123",
            customerName = "Aakash",
            customerMobile = "+91 98200 12345",
            labourId = "lab_101",
            service = "Electrician",
            rate = 600.0,
            status = Booking.STATUS_PENDING
        )
        assertTrue(pendingBooking.isPending)
        assertFalse(pendingBooking.isAccepted)

        val acceptedBooking = pendingBooking.copy(status = Booking.STATUS_ACCEPTED)
        assertFalse(acceptedBooking.isPending)
        assertTrue(acceptedBooking.isAccepted)
    }
}
