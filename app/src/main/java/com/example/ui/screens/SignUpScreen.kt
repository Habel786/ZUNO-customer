package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState

@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onSignUpClick: (name: String, mobile: String, email: String, pass: String, city: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val indianCities = listOf("Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Kolkata", "Chennai", "Pune", "Ahmedabad", "Jaipur", "Surat")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZunoSurfaceLight)
            .imePadding()
            .testTag("signup_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Create Customer Account",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ZunoNavy
            )
            Text(
                text = "Join ZUNO to hire verified labours across India",
                style = MaterialTheme.typography.bodyMedium,
                color = ZunoTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZunoRoseLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = ZunoRose,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = ZunoRose,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ZunoWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Full Name
                    Text("Full Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; if (uiState.errorMessage != null) onClearError() },
                        placeholder = { Text("e.g. Rahul Sharma", color = ZunoTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ZunoPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZunoPrimary,
                            unfocusedBorderColor = ZunoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mobile Number
                    Text("Mobile Number", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it; if (uiState.errorMessage != null) onClearError() },
                        placeholder = { Text("e.g. +91 98765 43210", color = ZunoTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = ZunoPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZunoPrimary,
                            unfocusedBorderColor = ZunoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_mobile_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Address
                    Text("Email Address", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; if (uiState.errorMessage != null) onClearError() },
                        placeholder = { Text("customer@example.com", color = ZunoTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ZunoPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZunoPrimary,
                            unfocusedBorderColor = ZunoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // City
                    Text("City / Location", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it; if (uiState.errorMessage != null) onClearError() },
                        placeholder = { Text("e.g. Mumbai, Maharashtra", color = ZunoTextMuted) },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = ZunoPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZunoPrimary,
                            unfocusedBorderColor = ZunoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_city_input")
                    )

                    // Quick city chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Mumbai", "Delhi", "Bengaluru", "Pune").forEach { quickCity ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (city.contains(quickCity)) ZunoPrimary else ZunoIce,
                                modifier = Modifier.clickable { city = quickCity }
                            ) {
                                Text(
                                    text = quickCity,
                                    fontSize = 11.sp,
                                    color = if (city.contains(quickCity)) Color.White else ZunoPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    Text("Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ZunoNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; if (uiState.errorMessage != null) onClearError() },
                        placeholder = { Text("Minimum 6 characters", color = ZunoTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ZunoPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = ZunoTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onSignUpClick(name, mobile, email, password, city)
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZunoPrimary,
                            unfocusedBorderColor = ZunoBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSignUpClick(name, mobile, email, password, city)
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZunoPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_submit_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "Complete Registration",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = ZunoTextSecondary
                )
                Text(
                    text = "Log In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZunoPrimary,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("navigate_login_button")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
