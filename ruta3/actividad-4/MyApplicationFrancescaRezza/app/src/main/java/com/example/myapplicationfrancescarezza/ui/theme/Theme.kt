package com.example.myapplicationfrancescarezza.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FlightPrimaryDark,
    onPrimary = FlightOnPrimary,
    secondary = FlightSecondary,
    onSecondary = Color.Black,
    tertiary = FlightSecondary,
    surface = Color(0xFF111B2E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2A3C),
    onSurfaceVariant = Color(0xFFB0BEC5),
    background = Color(0xFF0D1524),
    onBackground = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = FlightPrimary,
    onPrimary = FlightOnPrimary,
    secondary = FlightSecondary,
    onSecondary = Color.Black,
    tertiary = FlightSecondary,
    surface = FlightSurface,
    onSurface = FlightOnSurface,
    surfaceVariant = FlightSurfaceVariant,
    onSurfaceVariant = FlightOnSurfaceVariant,
    background = FlightSurface,
    onBackground = FlightOnSurface
)

@Composable
fun MyApplicationFrancescaRezzaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}