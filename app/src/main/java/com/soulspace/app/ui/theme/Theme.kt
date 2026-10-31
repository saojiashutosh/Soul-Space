package com.soulspace.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SageLight,
    onPrimaryContainer = SageDark,
    secondary = TerracottaClay,
    onSecondary = Color.White,
    secondaryContainer = SoftBlush,
    onSecondaryContainer = TerracottaClay,
    tertiary = OliveMuted,
    onTertiary = Color.White,
    background = SanctuaryCream,
    onBackground = CharcoalDeep,
    surface = SanctuarySurface,
    onSurface = CharcoalDeep,
    surfaceVariant = SanctuarySurfaceVariant,
    onSurfaceVariant = CharcoalMuted,
    outline = CharcoalDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = NightAccent,
    onPrimary = NightBackground,
    primaryContainer = SageDark,
    onPrimaryContainer = Color.White,
    secondary = TerracottaLight,
    onSecondary = NightBackground,
    secondaryContainer = TerracottaClay,
    onSecondaryContainer = Color.White,
    tertiary = OliveMuted,
    onTertiary = Color.White,
    background = NightBackground,
    onBackground = NightTextPrimary,
    surface = NightSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextSecondary,
    outline = Color(0xFF2C3831)
)

@Composable
fun SoulSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SoulSpaceTypography,
        shapes = SoulSpaceShapes,
        content = content
    )
}
