package com.example.chesspulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = DarkAppColors.accent,
    secondary = DarkAppColors.accentSoft,
    tertiary = DarkAppColors.accentSoft,
    background = DarkAppColors.screenBg,
    surface = DarkAppColors.surface,
    onBackground = DarkAppColors.textPrimary,
    onSurface = DarkAppColors.textPrimary,
    onSurfaceVariant = DarkAppColors.textSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightAppColors.accent,
    secondary = LightAppColors.accentSoft,
    tertiary = LightAppColors.accentSoft,
    background = LightAppColors.screenBg,
    surface = LightAppColors.surface,
    onBackground = LightAppColors.textPrimary,
    onSurface = LightAppColors.textPrimary,
    onSurfaceVariant = LightAppColors.textSecondary
)

@Composable
fun ChessPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides (if (darkTheme) DarkAppColors else LightAppColors)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
