package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreService
import com.example.data.location.LocationHelper
import com.example.data.location.UserLocation
import com.example.data.model.Labour
import com.example.data.model.ServiceCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LabourUiState(
    val isLoading: Boolean = true,
    val labours: List<Labour> = emptyList(),
    val filteredLabours: List<Labour> = emptyList(),
    val selectedCategory: String? = null,
    val userLocation: UserLocation = UserLocation(19.0760, 72.8777, "Mumbai, India", "Current Location", false),
    val hasLocationPermission: Boolean = false,
    val selectedLabour: Labour? = null,
    val isSeeding: Boolean = false,
    val seedMessage: String? = null,
    val errorMessage: String? = null
)

class LabourViewModel(
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabourUiState())
    val uiState: StateFlow<LabourUiState> = _uiState.asStateFlow()

    init {
        listenToLabours()
    }

    private fun listenToLabours() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            firestoreService.observeAvailableLabours().collectLatest { list ->
                val currentLoc = _uiState.value.userLocation
                val sorted = list.sortedBy { labour ->
                    labour.calculateDistanceKm(currentLoc.latitude, currentLoc.longitude)
                }
                _uiState.value = _uiState.value.copy(
                    labours = sorted,
                    filteredLabours = filterAndSort(sorted, _uiState.value.selectedCategory, currentLoc),
                    isLoading = false
                )
            }
        }
    }

    fun selectCategory(category: String?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.value = _uiState.value.copy(
            selectedCategory = newCategory,
            filteredLabours = filterAndSort(_uiState.value.labours, newCategory, _uiState.value.userLocation)
        )
    }

    fun updateUserLocation(location: UserLocation, hasPermission: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            userLocation = location,
            hasLocationPermission = hasPermission,
            filteredLabours = filterAndSort(_uiState.value.labours, _uiState.value.selectedCategory, location)
        )
    }

    fun selectLabour(labour: Labour?) {
        _uiState.value = _uiState.value.copy(selectedLabour = labour)
    }

    fun getLabourById(labourId: String): Labour? {
        return _uiState.value.labours.find { it.uid == labourId }
    }

    fun seedDemoLabours() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSeeding = true)
            val loc = _uiState.value.userLocation
            val count = firestoreService.seedSampleLaboursIfEmpty(loc.latitude, loc.longitude, loc.cityName)
            _uiState.value = _uiState.value.copy(
                isSeeding = false,
                seedMessage = if (count > 0) "Added $count verified workers in ${loc.cityName}" else "Available workers already synced!"
            )
        }
    }

    fun clearSeedMessage() {
        _uiState.value = _uiState.value.copy(seedMessage = null)
    }

    private fun filterAndSort(
        allLabours: List<Labour>,
        category: String?,
        location: UserLocation
    ): List<Labour> {
        val filtered = if (category.isNullOrBlank()) {
            allLabours
        } else {
            allLabours.filter { it.service.equals(category, ignoreCase = true) }
        }
        return filtered.sortedBy { it.calculateDistanceKm(location.latitude, location.longitude) }
    }
}
