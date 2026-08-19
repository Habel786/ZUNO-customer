package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.ZunoTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingUiState

@Composable
fun MyBookingsScreen(
    uiState: BookingUiState,
    onBookingClick: (Booking) -> Unit,
    onExploreNearbyClick: () -> Unit
) {
    Scaffold(
        topBar = {
            ZunoTopBar(
                title = "My Bookings",
                subtitle = "${uiState.bookings.size} Total Orders"
            )
        },
        containerColor = ZunoSurfaceLight,
        modifier = Modifier.testTag("my_bookings_screen")
    ) { innerPadding ->
        if (uiState.isLoading && uiState.bookings.isEmpty()) {
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

        if (uiState.bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ZunoWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ZunoIce),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ZunoPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Bookings Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZunoNavy
                        )

                        Text(
                            text = "You haven't requested any labour yet. Explore nearby verified workers and book instantly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZunoTextSecondary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                        )

                        Button(
                            onClick = onExploreNearbyClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZunoPrimary),
                            modifier = Modifier.testTag("explore_nearby_empty_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Find Nearby Labour", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.bookings, key = { it.bookingId }) { booking ->
                    BookingListItemCard(
                        booking = booking,
                        onClick = { onBookingClick(booking) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingListItemCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("booking_item_${booking.bookingId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZunoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZunoIce),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = booking.service,
                            tint = ZunoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = booking.service,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZunoNavy
                        )
                        Text(
                            text = "Order #${booking.bookingId.takeLast(6)}",
                            fontSize = 11.sp,
                            color = ZunoTextMuted
                        )
                    }
                }

                BookingStatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ZunoBorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (booking.isAccepted && booking.labourName.isNotBlank()) "Worker: ${booking.labourName}" else booking.formattedDate,
                        fontSize = 12.sp,
                        fontWeight = if (booking.isAccepted) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (booking.isAccepted) ZunoPrimaryDark else ZunoTextSecondary
                    )
                }

                Text(
                    text = "₹${booking.rate.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ZunoNavy
                )
            }
        }
    }
}
