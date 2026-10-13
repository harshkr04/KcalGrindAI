package com.kcalgrindai.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Kcal Grind AI V3 — Graphite & Emerald Design Tokens
// ============================================================================

// --- V3 Dark Theme Primary Tokens ---
val KcalGrindDarkBackground = Color(0xFF101211)
val KcalGrindDarkSurface = Color(0xFF191D1B)
val KcalGrindDarkSurfaceElevated = Color(0xFF222825)
val KcalGrindDarkBorder = Color(0xFF343B36)
val KcalGrindDarkTextPrimary = Color(0xFFF4F6F4)
val KcalGrindDarkTextSecondary = Color(0xFFB2BAB5)
val KcalGrindDarkTextMuted = Color(0xFF858F89)
val KcalGrindDarkEmerald = Color(0xFF38C77B)
val KcalGrindDarkStrongEmerald = Color(0xFF16834D)
val KcalGrindDarkProgressTrack = Color(0xFF2B332E)

// --- V3 Light Theme Primary Tokens ---
val KcalGrindLightBackground = Color(0xFFF2F4F2)
val KcalGrindLightSurface = Color(0xFFFFFFFF)
val KcalGrindLightSurfaceElevated = Color(0xFFE7EBE8)
val KcalGrindLightBorder = Color(0xFFD3DAD5)
val KcalGrindLightTextPrimary = Color(0xFF171C19)
val KcalGrindLightTextSecondary = Color(0xFF505A53)
val KcalGrindLightTextMuted = Color(0xFF68736B)
val KcalGrindLightEmerald = Color(0xFF16834D)
val KcalGrindLightStrongEmerald = Color(0xFF10663C)
val KcalGrindLightProgressTrack = Color(0xFFDCE3DE)

// --- Status & Accent Tokens ---
val KcalGrindError = Color(0xFFBA1A1A)
val KcalGrindOnError = Color(0xFFFFFFFF)
val KcalGrindErrorContainer = Color(0xFFFFDAD6)
val KcalGrindOnErrorContainer = Color(0xFF93000A)

val KcalGrindDarkError = Color(0xFFFFB4AB)
val KcalGrindDarkOnError = Color(0xFF690005)
val KcalGrindDarkErrorContainer = Color(0xFF93000A)
val KcalGrindDarkOnErrorContainer = Color(0xFFFFDAD6)

// --- Default / Compatibility Tokens (Mapped to V3 Dark / Emerald) ---
val KcalGrindPrimary = KcalGrindDarkEmerald
val KcalGrindOnPrimary = Color(0xFF101211)
val KcalGrindPrimaryContainer = KcalGrindDarkStrongEmerald
val KcalGrindOnPrimaryContainer = KcalGrindDarkTextPrimary
val KcalGrindInversePrimary = KcalGrindLightEmerald

val KcalGrindPrimaryFixed = Color(0xFFB1F0CE)
val KcalGrindPrimaryFixedDim = Color(0xFF95D4B3)
val KcalGrindOnPrimaryFixed = Color(0xFF002114)
val KcalGrindOnPrimaryFixedVariant = KcalGrindDarkStrongEmerald

val KcalGrindSecondary = KcalGrindDarkStrongEmerald
val KcalGrindOnSecondary = Color(0xFFFFFFFF)
val KcalGrindSecondaryContainer = Color(0xFF1E2822)
val KcalGrindOnSecondaryContainer = KcalGrindDarkEmerald
val KcalGrindSecondaryFixed = Color(0xFFB0F1CC)
val KcalGrindSecondaryFixedDim = Color(0xFF94D4B1)
val KcalGrindOnSecondaryFixed = Color(0xFF002113)
val KcalGrindOnSecondaryFixedVariant = Color(0xFF0C5136)

val KcalGrindTertiary = Color(0xFF4A55B9)
val KcalGrindOnTertiary = Color(0xFFFFFFFF)
val KcalGrindTertiaryContainer = Color(0xFF282B54)
val KcalGrindOnTertiaryContainer = Color(0xFFDFE0FF)
val KcalGrindTertiaryFixed = Color(0xFFDFE0FF)
val KcalGrindTertiaryFixedDim = Color(0xFFBDC2FF)
val KcalGrindOnTertiaryFixed = Color(0xFF000866)
val KcalGrindOnTertiaryFixedVariant = Color(0xFF303B9F)

val KcalGrindBackground = KcalGrindDarkBackground
val KcalGrindOnBackground = KcalGrindDarkTextPrimary
val KcalGrindSurface = KcalGrindDarkSurface
val KcalGrindOnSurface = KcalGrindDarkTextPrimary
val KcalGrindSurfaceVariant = KcalGrindDarkSurfaceElevated
val KcalGrindOnSurfaceVariant = KcalGrindDarkTextSecondary

val KcalGrindOutline = KcalGrindDarkTextMuted
val KcalGrindOutlineVariant = KcalGrindDarkBorder

val KcalGrindSurfaceDim = Color(0xFF131614)
val KcalGrindSurfaceBright = KcalGrindDarkSurfaceElevated
val KcalGrindSurfaceContainerLowest = Color(0xFF0C0E0D)
val KcalGrindSurfaceContainerLow = Color(0xFF141816)
val KcalGrindSurfaceContainer = KcalGrindDarkSurface
val KcalGrindSurfaceContainerHigh = KcalGrindDarkSurfaceElevated
val KcalGrindSurfaceContainerHighest = Color(0xFF2A312D)
val KcalGrindSurfaceTint = KcalGrindDarkEmerald
val KcalGrindInverseSurface = KcalGrindLightSurface
val KcalGrindInverseOnSurface = KcalGrindLightTextPrimary

// ============================================================================
// Extended V3 Color Tokens for Liquid Glass & Custom UI
// ============================================================================
@Immutable
data class KcalGrindV3Colors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val subtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val emeraldAccent: Color,
    val strongEmerald: Color,
    val progressTrack: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val isDark: Boolean
)

val KcalGrindV3DarkColors = KcalGrindV3Colors(
    background = KcalGrindDarkBackground,
    surface = KcalGrindDarkSurface,
    surfaceElevated = KcalGrindDarkSurfaceElevated,
    subtleBorder = KcalGrindDarkBorder,
    textPrimary = KcalGrindDarkTextPrimary,
    textSecondary = KcalGrindDarkTextSecondary,
    textMuted = KcalGrindDarkTextMuted,
    emeraldAccent = KcalGrindDarkEmerald,
    strongEmerald = KcalGrindDarkStrongEmerald,
    progressTrack = KcalGrindDarkProgressTrack,
    glassFill = Color(0xCC191D1B),
    glassBorder = Color(0x66343B36),
    isDark = true
)

val KcalGrindV3LightColors = KcalGrindV3Colors(
    background = KcalGrindLightBackground,
    surface = KcalGrindLightSurface,
    surfaceElevated = KcalGrindLightSurfaceElevated,
    subtleBorder = KcalGrindLightBorder,
    textPrimary = KcalGrindLightTextPrimary,
    textSecondary = KcalGrindLightTextSecondary,
    textMuted = KcalGrindLightTextMuted,
    emeraldAccent = KcalGrindLightEmerald,
    strongEmerald = KcalGrindLightStrongEmerald,
    progressTrack = KcalGrindLightProgressTrack,
    glassFill = Color(0xE6FFFFFF),
    glassBorder = Color(0x80D3DAD5),
    isDark = false
)

val LocalKcalGrindV3Colors = staticCompositionLocalOf { KcalGrindV3DarkColors }
