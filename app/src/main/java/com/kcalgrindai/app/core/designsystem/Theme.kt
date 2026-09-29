package com.kcalgrindai.app.core.designsystem

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

private val KcalGrindAILightColorScheme: ColorScheme = lightColorScheme(
    primary = KcalGrindPrimary,
    onPrimary = KcalGrindOnPrimary,
    primaryContainer = KcalGrindPrimaryContainer,
    onPrimaryContainer = KcalGrindOnPrimaryContainer,
    inversePrimary = KcalGrindInversePrimary,
    secondary = KcalGrindSecondary,
    onSecondary = KcalGrindOnSecondary,
    secondaryContainer = KcalGrindSecondaryContainer,
    onSecondaryContainer = KcalGrindOnSecondaryContainer,
    tertiary = KcalGrindTertiary,
    onTertiary = KcalGrindOnTertiary,
    tertiaryContainer = KcalGrindTertiaryContainer,
    onTertiaryContainer = KcalGrindOnTertiaryContainer,
    error = KcalGrindError,
    onError = KcalGrindOnError,
    errorContainer = KcalGrindErrorContainer,
    onErrorContainer = KcalGrindOnErrorContainer,
    background = KcalGrindBackground,
    onBackground = KcalGrindOnBackground,
    surface = KcalGrindSurface,
    onSurface = KcalGrindOnSurface,
    surfaceVariant = KcalGrindSurfaceVariant,
    onSurfaceVariant = KcalGrindOnSurfaceVariant,
    surfaceTint = KcalGrindSurfaceTint,
    inverseSurface = KcalGrindInverseSurface,
    inverseOnSurface = KcalGrindInverseOnSurface,
    outline = KcalGrindOutline,
    outlineVariant = KcalGrindOutlineVariant,
    surfaceDim = KcalGrindSurfaceDim,
    surfaceBright = KcalGrindSurfaceBright,
    surfaceContainerLowest = KcalGrindSurfaceContainerLowest,
    surfaceContainerLow = KcalGrindSurfaceContainerLow,
    surfaceContainer = KcalGrindSurfaceContainer,
    surfaceContainerHigh = KcalGrindSurfaceContainerHigh,
    surfaceContainerHighest = KcalGrindSurfaceContainerHighest,
)

private val KcalGrindAIDarkColorScheme: ColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF5CD597),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003822),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF163E2B),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFA8F2CB),
    inversePrimary = KcalGrindPrimary,
    secondary = androidx.compose.ui.graphics.Color(0xFF94D4B1),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF003923),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF1D3628),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFB8F5D2),
    tertiary = androidx.compose.ui.graphics.Color(0xFFA5B4FC),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF1E1B4B),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF282B54),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFDFE0FF),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    background = androidx.compose.ui.graphics.Color(0xFF111413),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF1F3F2),
    surface = androidx.compose.ui.graphics.Color(0xFF161918),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1F3F2),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF232A26),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCAD3CD),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF5CD597),
    inverseSurface = androidx.compose.ui.graphics.Color(0xFFE1E3E2),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFF191C1C),
    outline = androidx.compose.ui.graphics.Color(0xFF8E9991),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF38433C),
    surfaceDim = androidx.compose.ui.graphics.Color(0xFF111413),
    surfaceBright = androidx.compose.ui.graphics.Color(0xFF373A39),
    surfaceContainerLowest = androidx.compose.ui.graphics.Color(0xFF0E1110),
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFF191D1B),
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF1F2422),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF282F2C),
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF323A36),
)

private val RobotoFlex = FontFamily.SansSerif
private val Inter = FontFamily.SansSerif

private val KcalGrindAITypography = Typography(
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

private val KcalGrindAIShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun KcalGrindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KcalGrindAIDarkColorScheme else KcalGrindAILightColorScheme,
        typography = KcalGrindAITypography,
        shapes = KcalGrindAIShapes,
        content = content,
    )
}
