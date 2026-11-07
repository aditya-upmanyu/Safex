package com.safex.app.presentation.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// SAFEx Production-Grade Design System Color Tokens
// =========================================================================

// Primary Brand Colors
val ShieldBlue = Color(0xFF0F2942)          // Deep, authoritative navy/slate
val ShieldBlueDark = Color(0xFF0A1D30)      // Pressed state / darker headers
val ShieldBlueLight = Color(0xFF1E3A5F)     // Secondary interactive accents
val ShieldBlueContainer = Color(0xFFE8EEF5) // Soft tint for badge containers

// Backgrounds & Surfaces
val CleanSlate = Color(0xFFF4F6F9)          // Soft off-white to reduce eye strain
val SurfaceWhite = Color(0xFFFFFFFF)        // Crisp white card surface
val BorderSubtle = Color(0xFFE2E8F0)        // 1dp subtle structural card border
val SurfaceMuted = Color(0xFFF8FAFC)        // Sub-sections & inputs

// Text Colors
val CharcoalBlack = Color(0xFF1E293B)       // Primary text - high contrast
val MutedSlate = Color(0xFF64748B)          // Secondary labels, timestamps
val SubduedSlate = Color(0xFF94A3B8)        // Placeholder, subtle icons

// Semantic Safety Colors (Authoritative Traffic-Light System)
val TrustEmerald = Color(0xFF10B981)        // Safe Status - Rich emerald green
val TrustEmeraldContainer = Color(0xFFECFDF5)
val TrustEmeraldBorder = Color(0xFFA7F3D0)

val CautionAmber = Color(0xFFF59E0B)        // Warning Status - Warm, high-visibility orange
val CautionAmberContainer = Color(0xFFFFFBEB)
val CautionAmberBorder = Color(0xFFFDE68A)

val AlertCrimson = Color(0xFFEF4444)        // Danger/Alert Status - Urgent, authoritative red
val AlertCrimsonContainer = Color(0xFFFEF2F2)
val AlertCrimsonBorder = Color(0xFFFECACA)

// Backward-compatibility aliases for legacy screens
val Navy = ShieldBlue
val NavyLight = ShieldBlueLight
val Cyan = ShieldBlueLight
val CyanDim = ShieldBlue
val Red = AlertCrimson
val Amber = CautionAmber
val Green = TrustEmerald
val Surface = SurfaceWhite
val Card = SurfaceWhite
val Divider = BorderSubtle
val PrimaryText = CharcoalBlack
val SecondaryText = MutedSlate
val LightBackground = CleanSlate
val LightSurface = SurfaceWhite
val LightCard = SurfaceWhite
val LightPrimaryText = CharcoalBlack
val LightSecondaryText = MutedSlate
val LightDivider = BorderSubtle
