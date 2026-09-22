package com.taskmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = BlueDark,
    secondary = BlueDark,
    onSecondary = Color.White,
    secondaryContainer = LightPrimaryContainer,
    onSecondaryContainer = LightOnPrimaryContainer,
    tertiary = Blue,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
)

private val DarkColors = darkColorScheme(
    primary = BlueLight,
    onPrimary = Color.White,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = Blue,
    secondary = BlueLight,
    onSecondary = Color.White,
    secondaryContainer = DarkPrimaryContainer,
    onSecondaryContainer = DarkOnPrimaryContainer,
    tertiary = BlueLight,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DangerDark,
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
)

/** Extra semantic colors not part of the M3 scheme. */
data class ThemeExtras(
    val success: Color,
    val warning: Color,
    val danger: Color,
)

val LocalThemeExtras = staticCompositionLocalOf {
    ThemeExtras(success = Success, warning = Warning, danger = Danger)
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extras = if (darkTheme) {
        ThemeExtras(success = SuccessDark, warning = WarningDark, danger = DangerDark)
    } else {
        ThemeExtras(success = Success, warning = Warning, danger = Danger)
    }
    CompositionLocalProvider(LocalThemeExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

@Composable
fun extras(): ThemeExtras = LocalThemeExtras.current
