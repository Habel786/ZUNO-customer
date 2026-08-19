package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Labour
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.ZunoRadarMap
import com.example.ui.components.ZunoTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingUiState

@Composable
fun BookingDetailsScreen(
    bookingId: String,
    uiState: BookingUiState,
    onCancelBooking: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val booking = uiState.activeBooking
    val labour = uiState.activeLabour
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ZunoTopBar(
                title = "Booking Details",
                subtitle = "Order #${bookingId.takeLast(6)}",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = ZunoSurfaceLight,
        modifier = Modifier.testTag("booking_details_screen")
    ) { innerPadding ->
        if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ZunoPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Status Spotlight Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ZunoWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = booking.service,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = ZunoNavy
                            )
                            Text(
                                text = "Booked on ${booking.formattedDate}",
                                fontSize = 12.sp,
                                color = ZunoTextMuted
                            )
                        }

                        BookingStatusBadge(status = booking.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ZunoBorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Agreed Rate",
                            fontSize = 13.sp,
                            color = ZunoTextSecondary
                        )
                        Text(
                            text = "₹${booking.rate.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ZunoNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pending Status Informational Box
            if (booking.isPending) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZunoAmberLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ZunoAmber,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Waiting for Labour to Accept",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Worker has received your booking. Once accepted, their live GPS location & phone number will be revealed.",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Accepted Labour Contact Card (Revealed ONLY after acceptance)
            if (booking.isAccepted) {
                val workerName = booking.labourName.ifBlank { labour?.name ?: "Verified Worker" }
                val workerMobile = booking.labourMobile.ifBlank { labour?.mobile ?: "+91 98765 43210" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ZunoWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ASSIGNED WORKER DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZunoPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(ZunoEmeraldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = workerName,
                                        tint = ZunoEmerald,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = workerName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ZunoNavy
                                    )
                                    Text(
                                        text = workerMobile,
                                        fontSize = 13.sp,
                                        color = ZunoTextSecondary
                                    )
                                }
                            }

                            // Call Button
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$workerMobile"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ZunoEmerald)
                                    .testTag("call_labour_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Labour",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Map View tracking worker's GPS updates
                Text(
                    text = "LIVE WORKER GPS TRACKING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZunoNavy,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val effectiveLabourLat = labour?.latitude ?: booking.labourLatitude
                val effectiveLabourLng = labour?.longitude ?: booking.labourLongitude
                val currentLabourObj = labour ?: Labour(
                    uid = booking.labourId,
                    name = workerName,
                    mobile = workerMobile,
                    service = booking.service,
                    rate = booking.rate,
                    latitude = effectiveLabourLat,
                    longitude = effectiveLabourLng
                )

                ZunoRadarMap(
                    customerLat = booking.customerLatitude,
                    customerLng = booking.customerLongitude,
                    labours = listOf(currentLabourObj),
                    isLiveTracking = true,
                    acceptedLabour = currentLabourObj
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Customer Booking Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZunoWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Customer Info",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZunoNavy
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer Name", fontSize = 13.sp, color = ZunoTextSecondary)
                        Text(booking.customerName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mobile", fontSize = 13.sp, color = ZunoTextSecondary)
                        Text(booking.customerMobile, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Booking ID", fontSize = 13.sp, color = ZunoTextSecondary)
                        Text(booking.bookingId, fontSize = 12.sp, color = ZunoTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Booking Action (Available if Pending)
            if (booking.isPending) {
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ZunoRose
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancel_booking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = ZunoRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancel Booking Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Cancel Confirmation Dialog
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Booking?") },
                text = { Text("Are you sure you want to cancel this booking request? The worker will be notified.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCancelDialog = false
                            onCancelBooking(bookingId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZunoRose)
                    ) {
                        Text("Yes, Cancel", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Keep Booking")
                    }
                }
            )
        }
    }
}
