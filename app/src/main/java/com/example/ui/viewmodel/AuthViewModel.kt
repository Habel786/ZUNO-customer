package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthResult
import com.example.data.firebase.AuthService
import com.example.data.firebase.FirestoreService
import com.example.data.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentCustomer: Customer? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isInitialized: Boolean = false
)

class AuthViewModel(
    private val authService: AuthService = AuthService(),
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val user = authService.currentUser
            if (user != null) {
                _uiState.value = _uiState.value.copy(isAuthenticated = true, isLoading = true)
                // Listen to customer profile
                firestoreService.observeCustomerProfile(user.uid).collectLatest { customer ->
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentCustomer = customer ?: Customer(uid = user.uid, email = user.email ?: "", name = user.displayName ?: "Customer"),
                        isLoading = false,
                        isInitialized = true
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = false,
                    currentCustomer = null,
                    isLoading = false,
                    isInitialized = true
                )
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authService.login(email, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentCustomer = result.data,
                        errorMessage = null
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                AuthResult.Loading -> {}
            }
        }
    }

    fun signUp(
        name: String,
        mobile: String,
        email: String,
        pass: String,
        city: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || mobile.isBlank() || email.isBlank() || pass.isBlank() || city.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all registration fields")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters long")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authService.signUp(name, mobile, email, pass, city)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentCustomer = result.data,
                        errorMessage = null
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                AuthResult.Loading -> {}
            }
        }
    }

    fun sendPasswordReset(email: String, onComplete: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = authService.sendPasswordReset(email)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset email sent to $email. Please check your inbox.",
                        errorMessage = null
                    )
                    onComplete()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                AuthResult.Loading -> {}
            }
        }
    }

    fun updateProfile(name: String, mobile: String, city: String, onSuccess: () -> Unit) {
        val current = _uiState.value.currentCustomer ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val updated = current.copy(name = name.trim(), mobile = mobile.trim(), city = city.trim())
            val res = firestoreService.updateCustomerProfile(updated)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentCustomer = updated,
                    successMessage = "Profile updated successfully!"
                )
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Failed to update profile"
                )
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authService.logout()
        _uiState.value = AuthUiState(isInitialized = true)
        onLoggedOut()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
