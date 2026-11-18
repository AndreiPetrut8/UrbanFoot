package com.example.urbanfoot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = Color.White,
    primaryContainer = PitchGreenDark,
    onPrimaryContainer = Color.White,
    secondary = PitchGreenLight,
    onSecondary = PitchDarkBackground,
    background = PitchDarkBackground,
    onBackground = PitchDarkOnSurface,
    surface = PitchDarkSurface,
    onSurface = PitchDarkOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = PitchGreenDark,
    onPrimary = Color.White,
    primaryContainer = PitchGreenLight,
    onPrimaryContainer = PitchDarkBackground,
    secondary = PitchGreen,
    onSecondary = Color.White,
    background = PitchLightBackground,
    onBackground = PitchLightOnSurface,
    surface = PitchLightSurface,
    onSurface = PitchLightOnSurface,
)

@Composable
fun UrbanFootTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
