package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Labour
import com.example.data.model.ServiceCategory
import com.example.ui.components.LabourCard
import com.example.ui.components.ZunoRadarMap
import com.example.ui.components.ZunoTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.LabourUiState

@Composable
fun NearbyLabourScreen(
    uiState: LabourUiState,
    onCategorySelected: (String?) -> Unit,
    onLabourSelected: (Labour) -> Unit,
    onSeedDemoLabours: () -> Unit,
    onClearSeedMessage: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ZunoTopBar(
                title = "Nearby Labour",
                subtitle = uiState.userLocation.cityName
            )
        },
        containerColor = ZunoSurfaceLight,
        modifier = Modifier.testTag("nearby_labour_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Seed Success Feedback
            item {
                AnimatedVisibility(visible = uiState.seedMessage != null) {
                    uiState.seedMessage?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = ZunoEmeraldLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ZunoEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearSeedMessage, modifier = Modifier.size(20.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF065F46),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Radar Map View
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    ZunoRadarMap(
                        customerLat = uiState.userLocation.latitude,
                        customerLng = uiState.userLocation.longitude,
                        labours = uiState.filteredLabours,
                        selectedLabour = uiState.selectedLabour,
                        onLabourSelected = onLabourSelected
                    )
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onCategorySelected(null) },
                        label = { Text("All Services (${uiState.labours.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZunoPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = ZunoWhite,
                            labelColor = ZunoNavy
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_all")
                    )

                    ServiceCategory.ALL_SERVICES.forEach { service ->
                        val count = uiState.labours.count { it.service.equals(service.name, ignoreCase = true) }
                        FilterChip(
                            selected = uiState.selectedCategory == service.name,
                            onClick = { onCategorySelected(service.name) },
                            label = { Text("${service.name} ($count)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZunoPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ZunoWhite,
                                labelColor = ZunoNavy
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("filter_chip_${service.id}")
                        )
                    }
                }
            }

            // List Header & Seeder
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.filteredLabours.size} Available Nearby",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZunoNavy
                    )

                    // Helper to seed sample labours in Firestore if list is empty or for testing
                    TextButton(
                        onClick = onSeedDemoLabours,
                        enabled = !uiState.isSeeding,
                        modifier = Modifier.testTag("seed_labours_button")
                    ) {
                        if (uiState.isSeeding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = ZunoPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ZunoPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "Sync Firestore Workers",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZunoPrimary
                        )
                    }
                }
            }

            // Empty State
            if (uiState.filteredLabours.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZunoWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = ZunoTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No available labour nearby",
                                fontWeight = FontWeight.Bold,
                                color = ZunoNavy,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "No active workers found in this category. Tap 'Sync Firestore Workers' above to populate active verified labour into Cloud Firestore.",
                                fontSize = 13.sp,
                                color = ZunoTextSecondary,
                                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = onSeedDemoLabours,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ZunoPrimary)
                            ) {
                                Text("Populate Firestore Labours", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(uiState.filteredLabours, key = { it.uid }) { labour ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        LabourCard(
                            labour = labour,
                            customerLat = uiState.userLocation.latitude,
                            customerLng = uiState.userLocation.longitude,
                            onBookClick = onLabourSelected
                        )
                    }
                }
            }
        }
    }
}
