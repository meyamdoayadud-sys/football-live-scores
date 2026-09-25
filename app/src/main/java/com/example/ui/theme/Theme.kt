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

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = Color.Black,
    primaryContainer = PitchGreenDark,
    onPrimaryContainer = PitchGreenLight,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = BkashPink,
    onTertiary = Color.White,
    background = StadiumDarkBg,
    onBackground = TextWhite,
    surface = StadiumSurface,
    onSurface = TextWhite,
    surfaceVariant = StadiumCardBg,
    onSurfaceVariant = TextMuted,
    outline = StadiumCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    tertiary = BkashPink,
    onTertiary = Color.White,
    background = Color(0xFFF3F4F6),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFD1D5DB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek stadium dark mode
    dynamicColor: Boolean = false, // Preserve football theme branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
