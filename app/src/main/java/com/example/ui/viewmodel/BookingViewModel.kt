package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreService
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Labour
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

data class BookingUiState(
    val isLoading: Boolean = false,
    val isCreatingBooking: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val activeBooking: Booking? = null,
    val activeLabour: Labour? = null,
    val createdBookingId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BookingViewModel(
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun listenToCustomerBookings(customerId: String) {
        if (customerId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            firestoreService.observeCustomerBookings(customerId).collectLatest { list ->
                _uiState.value = _uiState.value.copy(
                    bookings = list,
                    isLoading = false
                )
            }
        }
    }

    fun listenToBookingDetails(bookingId: String) {
        if (bookingId.isBlank()) return
        viewModelScope.launch {
            firestoreService.observeBooking(bookingId).collectLatest { booking ->
                _uiState.value = _uiState.value.copy(activeBooking = booking)
                if (booking != null && booking.isAccepted && booking.labourId.isNotBlank()) {
                    // Start listening to the accepted labour's live updates (latitude/longitude)
                    listenToActiveLabour(booking.labourId)
                }
            }
        }
    }

    private fun listenToActiveLabour(labourId: String) {
        viewModelScope.launch {
            firestoreService.observeLabour(labourId).collectLatest { labour ->
                _uiState.value = _uiState.value.copy(activeLabour = labour)
            }
        }
    }

    fun createBooking(
        customer: Customer,
        labour: Labour,
        customerLat: Double,
        customerLng: Double,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBooking = true, errorMessage = null)
            val newBooking = Booking(
                bookingId = "",
                customerId = customer.uid,
                customerName = customer.name,
                customerMobile = customer.mobile,
                labourId = labour.uid,
                service = labour.service,
                rate = labour.rate,
                status = Booking.STATUS_PENDING,
                customerLatitude = customerLat,
                customerLongitude = customerLng,
                labourLatitude = labour.latitude,
                labourLongitude = labour.longitude,
                labourName = labour.name,
                labourMobile = labour.mobile,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val result = firestoreService.createBooking(newBooking)
            if (result.isSuccess) {
                val bookingId = result.getOrNull() ?: ""
                _uiState.value = _uiState.value.copy(
                    isCreatingBooking = false,
                    createdBookingId = bookingId,
                    successMessage = "Booking request submitted! Waiting for labour to accept."
                )
                onSuccess(bookingId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isCreatingBooking = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create booking"
                )
            }
        }
    }

    fun cancelBooking(bookingId: String, onCancelled: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = firestoreService.cancelBooking(bookingId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (res.isSuccess) "Booking cancelled" else null,
                errorMessage = if (res.isFailure) "Could not cancel booking" else null
            )
            if (res.isSuccess) {
                onCancelled()
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
