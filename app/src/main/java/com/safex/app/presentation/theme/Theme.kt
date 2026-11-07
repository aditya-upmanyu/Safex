package com.safex.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Clean Light Color Scheme (Primary experience for clarity & trust)
private val LightColorScheme = lightColorScheme(
    primary = ShieldBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = ShieldBlueContainer,
    onPrimaryContainer = ShieldBlue,
    inversePrimary = ShieldBlueLight,

    secondary = ShieldBlueLight,
    onSecondary = SurfaceWhite,
    secondaryContainer = CleanSlate,
    onSecondaryContainer = CharcoalBlack,

    tertiary = TrustEmerald,
    onTertiary = SurfaceWhite,
    tertiaryContainer = TrustEmeraldContainer,
    onTertiaryContainer = TrustEmerald,

    error = AlertCrimson,
    onError = SurfaceWhite,
    errorContainer = AlertCrimsonContainer,
    onErrorContainer = AlertCrimson,

    background = CleanSlate,
    onBackground = CharcoalBlack,

    surface = SurfaceWhite,
    onSurface = CharcoalBlack,
    surfaceVariant = CleanSlate,
    onSurfaceVariant = MutedSlate,

    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    scrim = ShieldBlueDark
)

// Clean Dark Color Scheme (Deep, authoritative navy/slate, avoiding harsh pure black)
private val DarkColorScheme = darkColorScheme(
    primary = ShieldBlueLight,
    onPrimary = SurfaceWhite,
    primaryContainer = ShieldBlue,
    onPrimaryContainer = SurfaceWhite,
    inversePrimary = ShieldBlue,

    secondary = SubduedSlate,
    onSecondary = ShieldBlueDark,
    secondaryContainer = ShieldBlue,
    onSecondaryContainer = SurfaceWhite,

    tertiary = TrustEmerald,
    onTertiary = SurfaceWhite,
    tertiaryContainer = TrustEmeraldContainer,
    onTertiaryContainer = TrustEmerald,

    error = AlertCrimson,
    onError = SurfaceWhite,
    errorContainer = AlertCrimsonContainer,
    onErrorContainer = AlertCrimson,

    background = Color(0xFF0B1928),
    onBackground = SurfaceWhite,

    surface = Color(0xFF132235),
    onSurface = SurfaceWhite,
    surfaceVariant = Color(0xFF1A2B40),
    onSurfaceVariant = SubduedSlate,

    outline = Color(0xFF26394F),
    outlineVariant = Color(0xFF26394F),
    scrim = Color(0xFF050C14))

@Composable
fun SAFExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Authoritative clean light theme is preferred for maximum high-contrast readability
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
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
