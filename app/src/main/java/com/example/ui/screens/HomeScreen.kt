package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Labour
import com.example.data.model.ServiceCategory
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.LabourCard
import com.example.ui.components.ZunoBrandHeader
import com.example.ui.components.ZunoRadarMap
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    customer: Customer?,
    cityName: String,
    hasLocationPermission: Boolean,
    onAllowLocationClick: () -> Unit,
    availableLabours: List<Labour>,
    recentBookings: List<Booking>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit,
    onViewAllNearbyClick: () -> Unit,
    onLabourClick: (Labour) -> Unit,
    onBookingClick: (Booking) -> Unit,
    customerLat: Double,
    customerLng: Double
) {
    val activeBooking = recentBookings.find { it.isPending || it.isAccepted }
    val closestLabour = availableLabours.firstOrNull()

    Scaffold(
        topBar = {
            ZunoBrandHeader(
                customerName = customer?.name ?: "Rahul Sharma",
                cityName = cityName,
                onAllowLocationClick = onAllowLocationClick,
                hasLocationPermission = hasLocationPermission
            )
        },
        containerColor = PolishBackground,
        modifier = Modifier.testTag("home_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Service Categories Section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Service Categories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            modifier = Modifier
                                .clickable { onViewAllNearbyClick() }
                                .testTag("see_all_categories_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val chunks = ServiceCategory.ALL_SERVICES.chunked(4)
                    chunks.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { service ->
                                val isSelected = selectedCategory == service.name
                                ProfessionalCategoryItem(
                                    service = service,
                                    isSelected = isSelected,
                                    onClick = { onCategoryClick(service.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Nearby Available Labour Header + Map section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby Available Labour",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = PolishTextPrimary
                        )

                        Surface(
                            color = PolishGreenLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${availableLabours.size} ONLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Radar Map with Glassmorphism Closest Provider Card
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ZunoRadarMap(
                            customerLat = customerLat,
                            customerLng = customerLng,
                            labours = availableLabours,
                            selectedLabour = closestLabour,
                            onLabourSelected = onLabourClick
                        )

                        // Glassmorphism Closest Provider Card
                        if (closestLabour != null) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .fillMaxWidth()
                                    .clickable { onLabourClick(closestLabour) }
                                    .testTag("closest_provider_card"),
                                color = Color.White.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 6.dp,
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PolishBluePastel),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "₹",
                                                color = PolishPrimary,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "CLOSEST PROVIDER",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PolishTextMuted,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "${closestLabour.service} • ${closestLabour.formattedDistance(customerLat, customerLng)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PolishTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onLabourClick(closestLabour) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PolishPrimary,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("quick_book_closest_button")
                                    ) {
                                        Text("BOOK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Booking Spotlight (if any)
            if (activeBooking != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                        Text(
                            text = "ACTIVE BOOKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookingClick(activeBooking) }
                                .testTag("active_booking_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishSurface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PolishBluePastel),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Handyman,
                                            contentDescription = null,
                                            tint = PolishPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = activeBooking.service,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = PolishTextPrimary
                                        )
                                        Text(
                                            text = "₹${activeBooking.rate.toInt()} • ${activeBooking.formattedDate}",
                                            fontSize = 12.sp,
                                            color = PolishTextSecondary
                                        )
                                    }
                                }

                                BookingStatusBadge(status = activeBooking.status)
                            }
                        }
                    }
                }
            }

            // Available Labour List Header & Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory != null) "Verified $selectedCategory" else "Available Verified Workers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PolishTextPrimary
                    )

                    TextButton(
                        onClick = onViewAllNearbyClick,
                        modifier = Modifier.testTag("view_all_nearby_button")
                    ) {
                        Text(
                            text = "View All",
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Labour Cards
            items(availableLabours.take(4)) { labour ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    LabourCard(
                        labour = labour,
                        customerLat = customerLat,
                        customerLng = customerLng,
                        onBookClick = onLabourClick
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalCategoryItem(
    service: ServiceCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, iconColor, icon) = when (service.name) {
        "General Labour" -> Triple(PolishBluePastel, PolishPrimary, Icons.Default.Handyman)
        "Carpenter" -> Triple(PolishOrangePastel, Color(0xFFEA580C), Icons.Default.Construction)
        "Plumber" -> Triple(PolishCyanPastel, Color(0xFF0891B2), Icons.Default.Plumbing)
        "Electrician" -> Triple(PolishYellowPastel, Color(0xFFCA8A04), Icons.Default.Bolt)
        "Painter" -> Triple(PolishPurplePastel, Color(0xFF9333EA), Icons.Default.FormatPaint)
        "Mason" -> Triple(PolishAmberLight, Color(0xFFD97706), Icons.Default.HomeRepairService)
        "Cleaner" -> Triple(PolishEmeraldPastel, Color(0xFF16A34A), Icons.Default.CleaningServices)
        "Driver" -> Triple(PolishRosePastel, Color(0xFFE11D48), Icons.Default.DirectionsCar)
        else -> Triple(PolishBluePastel, PolishPrimary, Icons.Default.Build)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("category_item_${service.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) PolishPrimary else bgColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PolishPrimary else PolishBorder,
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = service.name,
                tint = if (isSelected) Color.White else iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = service.name.split(" ").firstOrNull() ?: service.name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PolishPrimary else PolishTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
