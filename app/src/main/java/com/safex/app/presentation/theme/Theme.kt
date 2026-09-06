package com.safex.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Navy,
    primaryContainer = CyanDim,
    onPrimaryContainer = PrimaryText,
    secondary = Amber,
    onSecondary = Navy,
    secondaryContainer = Amber,
    onSecondaryContainer = Navy,
    tertiary = Green,
    onTertiary = Navy,
    error = Red,
    onError = PrimaryText,
    errorContainer = Red,
    onErrorContainer = PrimaryText,
    background = Navy,
    onBackground = PrimaryText,
    surface = Surface,
    onSurface = PrimaryText,
    surfaceVariant = Card,
    onSurfaceVariant = SecondaryText,
    outline = Divider,
    outlineVariant = Divider,
    scrim = Navy,
    inverseSurface = PrimaryText,
    inverseOnSurface = Navy,
    inversePrimary = CyanDim,
    surfaceTint = Cyan
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDim,
    onPrimary = LightBackground,
    primaryContainer = Cyan,
    onPrimaryContainer = Navy,
    secondary = Amber,
    onSecondary = LightBackground,
    secondaryContainer = Amber,
    onSecondaryContainer = Navy,
    tertiary = Green,
    onTertiary = LightBackground,
    error = Red,
    onError = LightBackground,
    errorContainer = Red,
    onErrorContainer = LightBackground,
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightSurface,
    onSurface = LightPrimaryText,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightSecondaryText,
    outline = LightDivider,
    outlineVariant = LightDivider,
    scrim = Navy,
    inverseSurface = Navy,
    inverseOnSurface = PrimaryText,
    inversePrimary = Cyan,
    surfaceTint = CyanDim
)

@Composable
fun SAFExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SafexTypography,
        shapes = SafexShapes,
        content = content
    )
}
