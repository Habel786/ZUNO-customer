package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.location.LocationHelper
import com.example.ui.components.ZunoBottomBar
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.BookingViewModel
import com.example.ui.viewmodel.LabourViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZunoCustomerApp()
            }
        }
    }
}

@Composable
fun ZunoCustomerApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationHelper = remember { LocationHelper(context) }

    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val labourViewModel: LabourViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    val authState by authViewModel.uiState.collectAsState()
    val labourState by labourViewModel.uiState.collectAsState()
    val bookingState by bookingViewModel.uiState.collectAsState()

    // Location Permission Handler
    fun checkAndRequestLocation() {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val isGranted = finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            coroutineScope.launch {
                val loc = locationHelper.getCurrentLocation()
                labourViewModel.updateUserLocation(loc, hasPermission = true)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                val loc = locationHelper.getCurrentLocation()
                labourViewModel.updateUserLocation(loc, hasPermission = true)
                snackbarHostState.showSnackbar("Location updated: ${loc.cityName}")
            }
        }
    }

    LaunchedEffect(Unit) {
        checkAndRequestLocation()
    }

    // Connect Customer Bookings once authenticated
    LaunchedEffect(authState.currentCustomer?.uid) {
        authState.currentCustomer?.uid?.let { uid ->
            bookingViewModel.listenToCustomerBookings(uid)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.SPLASH

    val bottomBarRoutes = listOf(
        NavRoutes.HOME,
        NavRoutes.NEARBY_LABOUR,
        NavRoutes.MY_BOOKINGS,
        NavRoutes.PROFILE
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                ZunoBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable(NavRoutes.SPLASH) {
                SplashScreen(
                    isAuthenticated = authState.isAuthenticated,
                    isInitialized = authState.isInitialized,
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    uiState = authState,
                    onLoginClick = { email, pass ->
                        authViewModel.login(email, pass) {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToSignUp = {
                        authViewModel.clearMessages()
                        navController.navigate(NavRoutes.SIGN_UP)
                    },
                    onNavigateToForgotPassword = {
                        authViewModel.clearMessages()
                        navController.navigate(NavRoutes.FORGOT_PASSWORD)
                    },
                    onClearError = { authViewModel.clearMessages() }
                )
            }

            // Sign Up Screen
            composable(NavRoutes.SIGN_UP) {
                SignUpScreen(
                    uiState = authState,
                    onSignUpClick = { name, mobile, email, pass, city ->
                        authViewModel.signUp(name, mobile, email, pass, city) {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.SIGN_UP) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToLogin = {
                        authViewModel.clearMessages()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.SIGN_UP) { inclusive = true }
                        }
                    },
                    onClearError = { authViewModel.clearMessages() }
                )
            }

            // Forgot Password Screen
            composable(NavRoutes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    uiState = authState,
                    onResetPasswordClick = { email ->
                        authViewModel.sendPasswordReset(email) {}
                    },
                    onNavigateBack = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onClearMessages = { authViewModel.clearMessages() }
                )
            }

            // Home Screen
            composable(NavRoutes.HOME) {
                HomeScreen(
                    customer = authState.currentCustomer,
                    cityName = labourState.userLocation.cityName,
                    hasLocationPermission = labourState.hasLocationPermission,
                    onAllowLocationClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    availableLabours = labourState.filteredLabours,
                    recentBookings = bookingState.bookings,
                    selectedCategory = labourState.selectedCategory,
                    onCategoryClick = { category ->
                        labourViewModel.selectCategory(category)
                        navController.navigate(NavRoutes.NEARBY_LABOUR)
                    },
                    onViewAllNearbyClick = {
                        navController.navigate(NavRoutes.NEARBY_LABOUR)
                    },
                    onLabourClick = { labour ->
                        labourViewModel.selectLabour(labour)
                        navController.navigate(NavRoutes.bookingConfirmation(labour.uid))
                    },
                    onBookingClick = { booking ->
                        bookingViewModel.listenToBookingDetails(booking.bookingId)
                        navController.navigate(NavRoutes.bookingDetails(booking.bookingId))
                    },
                    customerLat = labourState.userLocation.latitude,
                    customerLng = labourState.userLocation.longitude
                )
            }

            // Nearby Labour Screen
            composable(NavRoutes.NEARBY_LABOUR) {
                NearbyLabourScreen(
                    uiState = labourState,
                    onCategorySelected = { labourViewModel.selectCategory(it) },
                    onLabourSelected = { labour ->
                        labourViewModel.selectLabour(labour)
                        navController.navigate(NavRoutes.bookingConfirmation(labour.uid))
                    },
                    onSeedDemoLabours = { labourViewModel.seedDemoLabours() },
                    onClearSeedMessage = { labourViewModel.clearSeedMessage() }
                )
            }

            // Booking Confirmation Screen
            composable(
                route = NavRoutes.BOOKING_CONFIRMATION,
                arguments = listOf(navArgument("labourId") { type = NavType.StringType })
            ) { backStackEntry ->
                val labourId = backStackEntry.arguments?.getString("labourId") ?: ""
                val labour = labourViewModel.getLabourById(labourId) ?: labourState.selectedLabour

                BookingConfirmationScreen(
                    labour = labour,
                    customer = authState.currentCustomer,
                    customerLat = labourState.userLocation.latitude,
                    customerLng = labourState.userLocation.longitude,
                    cityName = labourState.userLocation.cityName,
                    bookingState = bookingState,
                    onConfirmBooking = { customer, selectedLabour ->
                        bookingViewModel.createBooking(
                            customer = customer,
                            labour = selectedLabour,
                            customerLat = labourState.userLocation.latitude,
                            customerLng = labourState.userLocation.longitude
                        ) { newBookingId ->
                            bookingViewModel.listenToBookingDetails(newBookingId)
                            navController.navigate(NavRoutes.bookingDetails(newBookingId)) {
                                popUpTo(NavRoutes.HOME)
                            }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // My Bookings Screen
            composable(NavRoutes.MY_BOOKINGS) {
                MyBookingsScreen(
                    uiState = bookingState,
                    onBookingClick = { booking ->
                        bookingViewModel.listenToBookingDetails(booking.bookingId)
                        navController.navigate(NavRoutes.bookingDetails(booking.bookingId))
                    },
                    onExploreNearbyClick = {
                        navController.navigate(NavRoutes.NEARBY_LABOUR)
                    }
                )
            }

            // Booking Details Screen
            composable(
                route = NavRoutes.BOOKING_DETAILS,
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""

                LaunchedEffect(bookingId) {
                    bookingViewModel.listenToBookingDetails(bookingId)
                }

                BookingDetailsScreen(
                    bookingId = bookingId,
                    uiState = bookingState,
                    onCancelBooking = { id ->
                        bookingViewModel.cancelBooking(id) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Booking cancelled successfully")
                            }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Profile Screen
            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    uiState = authState,
                    onUpdateProfile = { name, mobile, city ->
                        authViewModel.updateProfile(name, mobile, city) {}
                    },
                    onLogout = {
                        authViewModel.logout {
                            navController.navigate(NavRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onClearMessages = { authViewModel.clearMessages() }
                )
            }
        }
    }
}
