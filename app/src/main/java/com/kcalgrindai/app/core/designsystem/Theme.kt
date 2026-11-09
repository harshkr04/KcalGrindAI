package com.kcalgrindai.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// KcalGrind V3 — Material 3 Color Schemes
// ============================================================================

private val KcalGrindAILightColorScheme: ColorScheme = lightColorScheme(
    primary = KcalGrindLightEmerald,
    onPrimary = KcalGrindLightSurface,
    primaryContainer = KcalGrindLightStrongEmerald,
    onPrimaryContainer = KcalGrindLightSurface,
    inversePrimary = KcalGrindDarkEmerald,
    secondary = KcalGrindLightStrongEmerald,
    onSecondary = KcalGrindLightSurface,
    secondaryContainer = Color(0xFFE2EFE7),
    onSecondaryContainer = KcalGrindLightEmerald,
    tertiary = Color(0xFF313C9F),
    onTertiary = KcalGrindLightSurface,
    tertiaryContainer = Color(0xFFE0E3FF),
    onTertiaryContainer = Color(0xFF141B6B),
    error = KcalGrindError,
    onError = KcalGrindOnError,
    errorContainer = KcalGrindErrorContainer,
    onErrorContainer = KcalGrindOnErrorContainer,
    background = KcalGrindLightBackground,
    onBackground = KcalGrindLightTextPrimary,
    surface = KcalGrindLightSurface,
    onSurface = KcalGrindLightTextPrimary,
    surfaceVariant = KcalGrindLightSurfaceElevated,
    onSurfaceVariant = KcalGrindLightTextSecondary,
    surfaceTint = KcalGrindLightEmerald,
    inverseSurface = KcalGrindDarkSurface,
    inverseOnSurface = KcalGrindDarkTextPrimary,
    outline = KcalGrindLightTextMuted,
    outlineVariant = KcalGrindLightBorder,
    surfaceDim = Color(0xFFE4E8E5),
    surfaceBright = KcalGrindLightSurface,
    surfaceContainerLowest = KcalGrindLightSurface,
    surfaceContainerLow = Color(0xFFEDF1EE),
    surfaceContainer = Color(0xFFE7EBE8),
    surfaceContainerHigh = Color(0xFFE1E6E2),
    surfaceContainerHighest = Color(0xFFDCE2DD),
)

private val KcalGrindAIDarkColorScheme: ColorScheme = darkColorScheme(
    primary = KcalGrindDarkEmerald,
    onPrimary = KcalGrindDarkBackground,
    primaryContainer = KcalGrindDarkStrongEmerald,
    onPrimaryContainer = KcalGrindDarkTextPrimary,
    inversePrimary = KcalGrindLightEmerald,
    secondary = KcalGrindDarkStrongEmerald,
    onSecondary = KcalGrindDarkTextPrimary,
    secondaryContainer = Color(0xFF1A2620),
    onSecondaryContainer = KcalGrindDarkEmerald,
    tertiary = Color(0xFF9CA5FF),
    onTertiary = Color(0xFF1A227E),
    tertiaryContainer = Color(0xFF282B54),
    onTertiaryContainer = Color(0xFFDFE0FF),
    error = KcalGrindDarkError,
    onError = KcalGrindDarkOnError,
    errorContainer = KcalGrindDarkErrorContainer,
    onErrorContainer = KcalGrindDarkOnErrorContainer,
    background = KcalGrindDarkBackground,
    onBackground = KcalGrindDarkTextPrimary,
    surface = KcalGrindDarkSurface,
    onSurface = KcalGrindDarkTextPrimary,
    surfaceVariant = KcalGrindDarkSurfaceElevated,
    onSurfaceVariant = KcalGrindDarkTextSecondary,
    surfaceTint = KcalGrindDarkEmerald,
    inverseSurface = KcalGrindLightSurface,
    inverseOnSurface = KcalGrindLightTextPrimary,
    outline = KcalGrindDarkTextMuted,
    outlineVariant = KcalGrindDarkBorder,
    surfaceDim = Color(0xFF121514),
    surfaceBright = KcalGrindDarkSurfaceElevated,
    surfaceContainerLowest = Color(0xFF0D0F0E),
    surfaceContainerLow = Color(0xFF151817),
    surfaceContainer = KcalGrindDarkSurface,
    surfaceContainerHigh = KcalGrindDarkSurfaceElevated,
    surfaceContainerHighest = Color(0xFF2C3430),
)

// ============================================================================
// Typography System (Roboto Flex for Headlines, Inter for Data/Body)
// ============================================================================

private val RobotoFlex = FontFamily.SansSerif
private val Inter = FontFamily.SansSerif

private val KcalGrindAITypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 54.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

// ============================================================================
// Shape Tokens (Organic & approachably rounded)
// ============================================================================

private val KcalGrindAIShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Extension for easy access to V3 custom tokens from MaterialTheme
val MaterialTheme.v3Colors: KcalGrindV3Colors
    @Composable
    @ReadOnlyComposable
    get() = LocalKcalGrindV3Colors.current

@Composable
fun KcalGrindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val v3Colors = if (darkTheme) KcalGrindV3DarkColors else KcalGrindV3LightColors
    val colorScheme = if (darkTheme) KcalGrindAIDarkColorScheme else KcalGrindAILightColorScheme

    CompositionLocalProvider(LocalKcalGrindV3Colors provides v3Colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KcalGrindAITypography,
            shapes = KcalGrindAIShapes,
            content = content,
        )
    }
}
