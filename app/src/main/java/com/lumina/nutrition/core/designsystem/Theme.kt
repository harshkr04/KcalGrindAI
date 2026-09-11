package com.lumina.nutrition.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LuminaLightColorScheme: ColorScheme = lightColorScheme(
    primary = LuminaPrimary,
    onPrimary = LuminaOnPrimary,
    primaryContainer = LuminaPrimaryContainer,
    onPrimaryContainer = LuminaOnPrimaryContainer,
    inversePrimary = LuminaInversePrimary,
    secondary = LuminaSecondary,
    onSecondary = LuminaOnSecondary,
    secondaryContainer = LuminaSecondaryContainer,
    onSecondaryContainer = LuminaOnSecondaryContainer,
    tertiary = LuminaTertiary,
    onTertiary = LuminaOnTertiary,
    tertiaryContainer = LuminaTertiaryContainer,
    onTertiaryContainer = LuminaOnTertiaryContainer,
    error = LuminaError,
    onError = LuminaOnError,
    errorContainer = LuminaErrorContainer,
    onErrorContainer = LuminaOnErrorContainer,
    background = LuminaBackground,
    onBackground = LuminaOnBackground,
    surface = LuminaSurface,
    onSurface = LuminaOnSurface,
    surfaceVariant = LuminaSurfaceVariant,
    onSurfaceVariant = LuminaOnSurfaceVariant,
    surfaceTint = LuminaSurfaceTint,
    inverseSurface = LuminaInverseSurface,
    inverseOnSurface = LuminaInverseOnSurface,
    outline = LuminaOutline,
    outlineVariant = LuminaOutlineVariant,
    surfaceDim = LuminaSurfaceDim,
    surfaceBright = LuminaSurfaceBright,
    surfaceContainerLowest = LuminaSurfaceContainerLowest,
    surfaceContainerLow = LuminaSurfaceContainerLow,
    surfaceContainer = LuminaSurfaceContainer,
    surfaceContainerHigh = LuminaSurfaceContainerHigh,
    surfaceContainerHighest = LuminaSurfaceContainerHighest,
)

private val RobotoFlex = FontFamily.SansSerif
private val Inter = FontFamily.SansSerif

private val LuminaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W500,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

private val LuminaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun LuminaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LuminaLightColorScheme,
        typography = LuminaTypography,
        shapes = LuminaShapes,
        content = content,
    )
}
