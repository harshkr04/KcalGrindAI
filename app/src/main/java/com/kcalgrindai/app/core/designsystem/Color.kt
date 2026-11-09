package com.kcalgrindai.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// KcalGrind V3 — Graphite & Emerald Design Tokens
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

// ============================================================================
// Semantic & Component Color Tokens (Centralized to eliminate hardcoded Color(0x...))
// ============================================================================

// --- Core Nutrition & Macronutrients ---
val KcalGrindProtein = Color(0xFF60A5FA)
val KcalGrindProteinDark = Color(0xFF818CF8)
val KcalGrindProteinLight = Color(0xFF646FD4)
val KcalGrindCarbs = Color(0xFFFBBF24)
val KcalGrindFat = Color(0xFF34D399)
val KcalGrindFatDark = Color(0xFF10B981)
val KcalGrindWaterBlue = Color(0xFF38BDF8)
val KcalGrindWaterBlueDark = Color(0xFF0284C7)
val KcalGrindCaloriesPurple = Color(0xFFA78BFA)
val KcalGrindCaloriesPurpleDark = Color(0xFF8B5CF6)

// --- Badges, Alerts, Status & Feedback ---
val KcalGrindWarning = Color(0xFFF59E0B)
val KcalGrindWarningLightBg = Color(0xFFFFF3CD)
val KcalGrindWarningDarkBg = Color(0x33FFB74D)
val KcalGrindWarningLightBorder = Color(0xFFFFE0B2)
val KcalGrindWarningDarkBorder = Color(0x66FFB74D)
val KcalGrindWarningLightText = Color(0xFFB78103)
val KcalGrindWarningDarkText = Color(0xFFFFCC80)
val KcalGrindWarningOrange = Color(0xFFFF9800)
val KcalGrindWarningOrangeBg = Color(0xFFFFE0B2)
val KcalGrindWarningOrangeDarkBg = Color(0x33FF9800)
val KcalGrindWarningOrangeTextLight = Color(0xFFE65100)
val KcalGrindWarningOrangeTextDark = Color(0xFFFFB74D)

val KcalGrindSuccess = Color(0xFF10B981)
val KcalGrindSuccessLightBg = Color(0xFFE8F5E9)
val KcalGrindSuccessLightText = Color(0xFF2E7D32)
val KcalGrindAiBannerBgLight = Color(0xFFE8F5E9)
val KcalGrindAiBannerTextLight = Color(0xFF2E7D32)
val KcalGrindAiWaterBgLight = Color(0xFFE1F5FE)
val KcalGrindAiWaterTextLight = Color(0xFF0288D1)

val KcalGrindErrorRed = Color(0xFFEF4444)
val KcalGrindDeleteDangerRed = KcalGrindErrorRed
val KcalGrindOverBudgetRed = Color(0xFFE53935)
val KcalGrindOverBudgetLightBg = Color(0x33BA1A1A)
val KcalGrindOverBudgetLightBorder = Color(0x66BA1A1A)
val KcalGrindOverBudgetDarkText = Color(0xFFFF897D)

val KcalGrindStarAmber = Color(0xFFF59E0B)
val KcalGrindStarAmberDark = Color(0xFFE5A93C)

// --- Sources & Badges ---
val KcalGrindSourceVerified = Color(0xFF38B2AC)
val KcalGrindSourceVerifiedBg = Color(0x3338B2AC)
val KcalGrindSourceManual = Color(0xFF60A5FA)
val KcalGrindSourceManualBg = Color(0x3360A5FA)
val KcalGrindSourceCustom = Color(0xFFA78BFA)
val KcalGrindSourceCustomBg = Color(0x33A78BFA)
val KcalGrindSourceRecipe = Color(0xFFFBBF24)
val KcalGrindSourceRecipeBg = Color(0x33FBBF24)

// --- Surfaces, Chips & Container Accents ---
val KcalGrindChipDarkBg = Color(0xFF1F2622)
val KcalGrindChipLightBg = Color(0xFFE5F7ED)
val KcalGrindChipDarkBorder = Color(0xFF2F3C34)
val KcalGrindChipLightBorder = Color(0xFFBCE7D0)

val KcalGrindHighlightDarkBg = Color(0xFF183526)
val KcalGrindHighlightLightBg = Color(0xFFE2F6EB)
val KcalGrindHighlightDarkBorder = Color(0xFF235038)
val KcalGrindHighlightLightBorder = Color(0xFFACDFBF)

val KcalGrindDeepDarkSurface = Color(0xFF142019)
val KcalGrindDeepDarkBorder = Color(0xFF2B4736)
val KcalGrindDeepDarkBorderLight = Color(0xFFA6E0BD)
val KcalGrindDarkForest = Color(0xFF0F5238)
val KcalGrindSurfaceCharcoal = Color(0xFF0E1B13)

val KcalGrindDarkWaterCardBg = Color(0xFF182B20)
val KcalGrindDarkWaterCardBorder = Color(0xFF2B4736)

val KcalGrindDarkPromoBg = Color(0xFF16251D)
val KcalGrindLightPromoBg = Color(0xFFD6F5E4)
val KcalGrindDarkPromoBorder = Color(0xFF254633)
val KcalGrindLightPromoBorder = Color(0xFFA5E6BF)

val KcalGrindDarkBarUnfilled = Color(0xFF222B26)
val KcalGrindLightBarUnfilled = Color(0xFFD6DED9)
val KcalGrindDarkMutedBar = Color(0xFF5E6D64)
val KcalGrindLightMutedBar = Color(0xFF8B9E93)
val KcalGrindDarkSecondaryBar = Color(0xFFA8B5AC)
val KcalGrindLightSecondaryBar = Color(0xFF5B6E62)
val KcalGrindLightDeepText = Color(0xFF191C1C)

// --- Integrations & Services ---
val KcalGrindHealthConnectBlue = Color(0xFF3498DB)
val KcalGrindHealthConnectYellow = Color(0xFFF1C40F)
val KcalGrindCloudSyncTeal = Color(0xFF38B2AC)
val KcalGrindAmberAccent = Color(0xFFD97706)

// --- Avatar Preset Palette ---
val KcalGrindAvatarBrandGreen = Color(0xFF16A34A)
val KcalGrindAvatarIndigo = Color(0xFF4F46E5)
val KcalGrindAvatarTeal = Color(0xFF0D9488)
val KcalGrindAvatarSkyBlue = Color(0xFF0284C7)
val KcalGrindAvatarViolet = Color(0xFF7C3AED)
val KcalGrindAvatarRose = Color(0xFFE11D48)
val KcalGrindAvatarAmber = Color(0xFFEA580C)
val KcalGrindAvatarSlate = Color(0xFF475569)

// --- Widget Preview Onboarding Tokens ---
val KcalGrindWidgetPreviewCardBg = Color(0xFFE8ECE9)
val KcalGrindWidgetPreviewCardBorder = Color(0xFFCCD1CE)
val KcalGrindWidgetPreviewBadgeBg = Color(0xFFB0F1CC)
val KcalGrindWidgetPreviewBadgeBorder = Color(0xFF9AE3B9)
val KcalGrindWidgetPreviewDarkForest = Color(0xFF0F5238)
val KcalGrindWidgetPreviewWaterUnfilled = Color(0xFFC8F7DC)
val KcalGrindWidgetPreviewMacroCarb = Color(0xFF2D6A4F)
val KcalGrindWidgetPreviewMacroProtein = Color(0xFF195E3E)
val KcalGrindWidgetPreviewMacroFat = Color(0xFF407855)
val KcalGrindWidgetPreviewSlate = Color(0xFF404943)
val KcalGrindWidgetPreviewDarkText = Color(0xFF191C1C)
val KcalGrindWidgetPreviewIndigo = Color(0xFF313C9F)

// --- App Widget Glance Providers ---
val KcalGrindWidgetSurfaceDay = Color(0xFFFFFFFF)
val KcalGrindWidgetSurfaceNight = Color(0xFF191D1B)
val KcalGrindWidgetTextPrimaryDay = Color(0xFF171C19)
val KcalGrindWidgetTextPrimaryNight = Color(0xFFF4F6F4)
val KcalGrindWidgetTextSecondaryDay = Color(0xFF505A53)
val KcalGrindWidgetTextSecondaryNight = Color(0xFFB2BAB5)
val KcalGrindWidgetEnergyActiveDay = Color(0xFF16834D)
val KcalGrindWidgetEnergyActiveNight = Color(0xFF38C77B)
val KcalGrindWidgetEnergyTrackDay = Color(0xFFDCE3DE)
val KcalGrindWidgetEnergyTrackNight = Color(0xFF2B332E)
val KcalGrindWidgetActionAddDay = Color(0xFF16834D)
val KcalGrindWidgetActionAddNight = Color(0xFF38C77B)
val KcalGrindWidgetActionAddIconDay = Color(0xFFFFFFFF)
val KcalGrindWidgetActionAddIconNight = Color(0xFF101211)
val KcalGrindWidgetActionAiDay = Color(0xFF313C9F)
val KcalGrindWidgetActionAiNight = Color(0xFFBDC2FF)
val KcalGrindWidgetActionWaterDay = Color(0xFF10663C)
val KcalGrindWidgetActionWaterNight = Color(0xFF16834D)
val KcalGrindWidgetMacroProteinDay = Color(0xFF16834D)
val KcalGrindWidgetMacroProteinNight = Color(0xFF38C77B)
val KcalGrindWidgetMacroCarbsDay = Color(0xFFD97706)
val KcalGrindWidgetMacroCarbsNight = Color(0xFFFBBF24)
val KcalGrindWidgetMacroFatDay = Color(0xFF0284C7)
val KcalGrindWidgetMacroFatNight = Color(0xFF38BDF8)
