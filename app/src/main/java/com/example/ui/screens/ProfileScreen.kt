package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.Customer
import com.example.ui.components.ZunoTopBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState

@Composable
fun ProfileScreen(
    uiState: AuthUiState,
    onUpdateProfile: (name: String, mobile: String, city: String) -> Unit,
    onLogout: () -> Unit,
    onClearMessages: () -> Unit
) {
    val customer = uiState.currentCustomer
    var isEditing by remember { mutableStateOf(false) }
    var name by remember(customer) { mutableStateOf(customer?.name ?: "") }
    var mobile by remember(customer) { mutableStateOf(customer?.mobile ?: "") }
    var city by remember(customer) { mutableStateOf(customer?.city ?: "") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            ZunoTopBar(
                title = "My Profile",
                actions = {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                onUpdateProfile(name, mobile, city)
                                isEditing = false
                            },
                            modifier = Modifier.testTag("save_profile_button")
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = ZunoPrimary)
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = ZunoNavy
                            )
                        }
                    }
                }
            )
        },
        containerColor = ZunoSurfaceLight,
        modifier = Modifier.testTag("profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ZunoIce),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (customer?.name?.take(1) ?: "C").uppercase(),
                    color = ZunoPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = customer?.name ?: "Customer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ZunoNavy
            )

            Surface(
                color = ZunoIce,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Customer Account • India",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZunoPrimaryDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Success feedback
            AnimatedVisibility(visible = uiState.successMessage != null) {
                uiState.successMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = ZunoEmeraldLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZunoEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = Color(0xFF065F46), fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = onClearMessages, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Customer Profile Form / Details
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
                    if (isEditing) {
                        Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_mobile_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("City", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_city_input")
                        )
                    } else {
                        ProfileInfoRow(icon = Icons.Default.Person, label = "Full Name", value = customer?.name ?: "")
                        HorizontalDivider(color = ZunoBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow(icon = Icons.Default.Email, label = "Email Address", value = customer?.email ?: "")
                        HorizontalDivider(color = ZunoBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow(icon = Icons.Default.Phone, label = "Mobile Number", value = customer?.mobile ?: "+91 Not Provided")
                        HorizontalDivider(color = ZunoBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow(icon = Icons.Default.LocationCity, label = "City", value = customer?.city ?: "India")
                        HorizontalDivider(color = ZunoBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        ProfileInfoRow(icon = Icons.Default.Fingerprint, label = "Firebase UID", value = customer?.uid?.take(16) ?: "")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Firebase Integration Info Card
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
                        text = "System Integration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZunoNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Firebase Project", fontSize = 12.sp, color = ZunoTextSecondary)
                        Text("zuno-904d6", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZunoPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Authentication", fontSize = 12.sp, color = ZunoTextSecondary)
                        Text("Firebase Email/Password", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ZunoEmerald)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Database", fontSize = 12.sp, color = ZunoTextSecondary)
                        Text("Cloud Firestore", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ZunoEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZunoRoseLight,
                    contentColor = Color(0xFF991B1B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFF991B1B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Sign Out") },
                text = { Text("Are you sure you want to sign out of ZUNO Customer?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZunoRose)
                    ) {
                        Text("Sign Out", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ZunoPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ZunoTextMuted)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ZunoNavy
            )
        }
    }
}
