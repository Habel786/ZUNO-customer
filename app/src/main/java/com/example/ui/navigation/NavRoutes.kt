package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // Main App Bottom Bar Routes
    const val HOME = "home"
    const val NEARBY_LABOUR = "nearby_labour"
    const val MY_BOOKINGS = "my_bookings"
    const val PROFILE = "profile"

    // Detail / Action Routes
    const val BOOKING_CONFIRMATION = "booking_confirmation/{labourId}"
    const val BOOKING_DETAILS = "booking_details/{bookingId}"

    fun bookingConfirmation(labourId: String) = "booking_confirmation/$labourId"
    fun bookingDetails(bookingId: String) = "booking_details/$bookingId"
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : BottomNavItem(
        route = NavRoutes.HOME,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    )

    object Nearby : BottomNavItem(
        route = NavRoutes.NEARBY_LABOUR,
        title = "Nearby",
        selectedIcon = Icons.Filled.NearMe,
        unselectedIcon = Icons.Outlined.NearMe,
        testTag = "nav_nearby"
    )

    object Bookings : BottomNavItem(
        route = NavRoutes.MY_BOOKINGS,
        title = "Bookings",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "nav_bookings"
    )

    object Profile : BottomNavItem(
        route = NavRoutes.PROFILE,
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )

    companion object {
        val items = listOf(Home, Nearby, Bookings, Profile)
    }
}
