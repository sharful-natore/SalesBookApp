package com.example.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PremiumDarkPrimary,
    onPrimary = PremiumDarkOnPrimary,
    primaryContainer = PremiumDarkPrimaryContainer,
    onPrimaryContainer = PremiumDarkOnPrimaryContainer,
    secondary = PremiumDarkSecondary,
    onSecondary = PremiumDarkOnSecondary,
    secondaryContainer = PremiumDarkSecondaryContainer,
    onSecondaryContainer = PremiumDarkOnSecondaryContainer,
    tertiary = PremiumDarkTertiary,
    onTertiary = PremiumDarkOnTertiary,
    tertiaryContainer = PremiumDarkTertiaryContainer,
    onTertiaryContainer = PremiumDarkOnTertiaryContainer,
    background = PremiumDarkBackground,
    onBackground = PremiumDarkOnBackground,
    surface = PremiumDarkSurface,
    onSurface = PremiumDarkOnSurface,
    surfaceVariant = PremiumDarkSurfaceVariant,
    onSurfaceVariant = PremiumDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumLightPrimary,
    onPrimary = PremiumLightOnPrimary,
    primaryContainer = PremiumLightPrimaryContainer,
    onPrimaryContainer = PremiumLightOnPrimaryContainer,
    secondary = PremiumLightSecondary,
    onSecondary = PremiumLightOnSecondary,
    secondaryContainer = PremiumLightSecondaryContainer,
    onSecondaryContainer = PremiumLightOnSecondaryContainer,
    tertiary = PremiumLightTertiary,
    onTertiary = PremiumLightOnTertiary,
    tertiaryContainer = PremiumLightTertiaryContainer,
    onTertiaryContainer = PremiumLightOnTertiaryContainer,
    background = PremiumLightBackground,
    onBackground = PremiumLightOnBackground,
    surface = PremiumLightSurface,
    onSurface = PremiumLightOnSurface,
    surfaceVariant = PremiumLightSurfaceVariant,
    onSurfaceVariant = PremiumLightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom theme colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Configures status bar icons to be white/light in dark mode and dark in light mode
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
