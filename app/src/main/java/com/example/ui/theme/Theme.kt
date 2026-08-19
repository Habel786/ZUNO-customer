package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ZunoLightColorScheme = lightColorScheme(
    primary = ZunoPrimary,
    onPrimary = ZunoTextOnPrimary,
    primaryContainer = ZunoIce,
    onPrimaryContainer = ZunoPrimaryDark,
    secondary = ZunoCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = ZunoAmber,
    onTertiary = Color.White,
    background = ZunoSurfaceLight,
    onBackground = ZunoTextPrimary,
    surface = ZunoSurfaceCard,
    onSurface = ZunoTextPrimary,
    surfaceVariant = ZunoIce,
    onSurfaceVariant = ZunoTextSecondary,
    outline = ZunoBorder,
    outlineVariant = ZunoBorderSubtle,
    error = ZunoRose,
    onError = Color.White,
    errorContainer = ZunoRoseLight,
    onErrorContainer = Color(0xFF991B1B)
)

private val ZunoDarkColorScheme = darkColorScheme(
    primary = ZunoSky,
    onPrimary = ZunoNavyDark,
    primaryContainer = ZunoNavy,
    onPrimaryContainer = ZunoSky,
    secondary = ZunoCyan,
    onSecondary = Color.Black,
    background = ZunoNavyDark,
    onBackground = Color.White,
    surface = ZunoNavy,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    error = ZunoRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent ZUNO brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZunoDarkColorScheme else ZunoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
