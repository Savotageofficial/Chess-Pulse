package com.example.chesspulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Semantic app palette. Every screen reads colors from here via [appColors]
 * instead of hardcoding Color(0x...) so the whole UI switches with system dark mode.
 */
@Immutable
data class AppColors(
    // Home / list screens background (blue-ish)
    val screenBg: Color,
    // Settings background (warm cream)
    val settingsBg: Color,
    // Cards / headers (white in light)
    val surface: Color,
    // Inner card background (slightly tinted)
    val cardBg: Color,
    // Primary text
    val textPrimary: Color,
    // Secondary / muted text
    val textSecondary: Color,
    // Brand dark brown (borders, icon boxes, accents)
    val accent: Color,
    // Soft accent (icon tint, dividers, unfocused borders)
    val accentSoft: Color,
    // Progress track
    val track: Color,
    // Log out button background
    val logoutBg: Color,
    // Login / signup gradient (top, bottom)
    val gradientTop: Color,
    val gradientBottom: Color,
    // Learn screen panel gradient
    val learnGradientTop: Color,
    val learnGradientBottom: Color,
    // Text inputs
    val inputBg: Color,
    val inputText: Color,
    val inputPlaceholder: Color,
    val inputIndicator: Color,
    val inputCursor: Color,
    // Auth toggle pill
    val toggleBg: Color,
    val toggleSelectedBg: Color,
    val toggleSelectedText: Color,
    val toggleUnselectedText: Color,
    // Panel border on Learn screen
    val panelBorder: Color
)

val LightAppColors = AppColors(
    screenBg = Color(0xFFcfdce4),
    settingsBg = Color(0xFFFAF3EF),
    surface = Color(0xFFFFFFFF),
    cardBg = Color(0xFFFDF9F7),
    textPrimary = Color(0xFF361F1A),
    textSecondary = Color(0xFF8D6E63),
    accent = Color(0xFF4E342E),
    accentSoft = Color(0xFFC19C94),
    track = Color(0xFFE5D5CC),
    logoutBg = Color(0xFFFFE5E5),
    gradientTop = Color(0xFFC47A40),
    gradientBottom = Color(0xFF4C2318),
    learnGradientTop = Color(0xFFFFC194),
    learnGradientBottom = Color(0xFF99643C),
    inputBg = Color(0xFFE6F1F5),
    inputText = Color(0xFF000000),
    inputPlaceholder = Color(0xFF6098AA),
    inputIndicator = Color(0xFF30190c),
    inputCursor = Color(0xFFFF7D19),
    toggleBg = Color(0xFFDAE9EE),
    toggleSelectedBg = Color(0xFFFFFFFF),
    toggleSelectedText = Color(0xFF38494C),
    toggleUnselectedText = Color(0xFF535356),
    panelBorder = Color(0xFF000000)
)

val DarkAppColors = AppColors(
    screenBg = Color(0xFF141A1E),
    settingsBg = Color(0xFF191310),
    surface = Color(0xFF241C18),
    cardBg = Color(0xFF2A211C),
    textPrimary = Color(0xFFEFE2DB),
    textSecondary = Color(0xFFBCA79E),
    accent = Color(0xFF8D6E63),
    accentSoft = Color(0xFF6B5248),
    track = Color(0xFF3E2F28),
    logoutBg = Color(0xFF3A1D1D),
    gradientTop = Color(0xFF8A5424),
    gradientBottom = Color(0xFF241108),
    learnGradientTop = Color(0xFF7A5433),
    learnGradientBottom = Color(0xFF3A2517),
    inputBg = Color(0xFF2A3439),
    inputText = Color(0xFFF0F0F0),
    inputPlaceholder = Color(0xFF8FA8B4),
    inputIndicator = Color(0xFFC47A40),
    inputCursor = Color(0xFFFF7D19),
    toggleBg = Color(0xFF2A3439),
    toggleSelectedBg = Color(0xFF3D4A52),
    toggleSelectedText = Color(0xFFE8EEF1),
    toggleUnselectedText = Color(0xFF9FB0B9),
    panelBorder = Color(0xFFC19C94)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Current palette - flips automatically with system dark mode. */
@Composable
fun appColors(): AppColors = LocalAppColors.current

/** Convenience: true when the system is in dark mode. */
@Composable
fun isAppInDarkTheme(): Boolean = isSystemInDarkTheme()
