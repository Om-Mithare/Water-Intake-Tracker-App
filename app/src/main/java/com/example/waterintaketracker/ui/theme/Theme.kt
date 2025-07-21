package com.example.waterintaketracker.ui.theme // << MAKE SURE THIS MATCHES YOUR PROJECT'S PACKAGE NAME

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Assuming you have these defined in your ui.theme.Color.kt
// For the blueish-blackish dark theme:
private val DarkPixelWaterColorScheme = darkColorScheme(
    primary = PixelWaterDarkPrimary,
    onPrimary = PixelWaterDarkOnPrimary,
    primaryContainer = PixelWaterDarkPrimaryContainer,
    onPrimaryContainer = PixelWaterDarkOnPrimaryContainer,
    secondary = PixelWaterDarkSecondary,
    onSecondary = PixelWaterDarkOnSecondary,
    secondaryContainer = PixelWaterDarkSecondaryContainer,
    onSecondaryContainer = PixelWaterDarkOnSecondaryContainer,
    tertiary = PixelWaterDarkTertiary,
    onTertiary = PixelWaterDarkOnTertiary,
    background = PixelWaterDarkBackground,
    onBackground = PixelWaterDarkOnBackground,
    surface = PixelWaterDarkSurface,
    onSurface = PixelWaterDarkOnSurface,
    surfaceVariant = PixelWaterDarkSurfaceVariant,
    onSurfaceVariant = PixelWaterDarkOnSurface, // Or a slightly different variant if needed
    error = PixelWaterDarkError,
    onError = PixelWaterDarkOnError,
    errorContainer = PixelWaterDarkErrorContainer,
    onErrorContainer = PixelWaterDarkOnErrorContainer,
    outline = PixelWaterDarkPrimary.copy(alpha = 0.3f), // Example for outlines
    outlineVariant = PixelWaterDarkSurfaceVariant.copy(alpha = 0.5f) // Example for subtle outlines
)

// Assuming you have these defined in your ui.theme.Color.kt for a contrasting light theme:
private val LightPixelWaterColorScheme = lightColorScheme(
    primary = PixelWaterLightPrimary,
    onPrimary = PixelWaterLightOnPrimary,
    primaryContainer = PixelWaterLightPrimaryContainer,
    onPrimaryContainer = PixelWaterLightOnPrimaryContainer,
    secondary = PixelWaterLightSecondary,
    onSecondary = PixelWaterLightOnSecondary,
    secondaryContainer = PixelWaterLightSecondaryContainer,
    onSecondaryContainer = PixelWaterLightOnSecondaryContainer,
    tertiary = PixelWaterLightTertiary,
    onTertiary = PixelWaterLightOnTertiary,
    background = PixelWaterLightBackground,
    onBackground = PixelWaterLightOnBackground,
    surface = PixelWaterLightSurface,
    onSurface = PixelWaterLightOnSurface,
    surfaceVariant = PixelWaterLightSurfaceVariant,
    onSurfaceVariant = PixelWaterLightOnSurface,
    error = PixelWaterLightError,
    onError = PixelWaterLightOnError,
    errorContainer = PixelWaterLightErrorContainer,
    onErrorContainer = PixelWaterLightOnErrorContainer,
    outline = PixelWaterLightPrimary.copy(alpha = 0.5f),
    outlineVariant = PixelWaterLightSurfaceVariant.copy(alpha = 0.7f)
)

@Composable
fun WaterIntakeTrackerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkPixelWaterColorScheme
    } else {
        LightPixelWaterColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PixelTypography,
        shapes = PixelShapes,
        content = content
    )
}
