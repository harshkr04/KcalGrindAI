package com.kcalgrindai.app.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ThemeTokensTest {

    @Test
    fun `dark theme tokens match approved V3 palette`() {
        assertEquals(Color(0xFF101211), KcalGrindDarkBackground)
        assertEquals(Color(0xFF191D1B), KcalGrindDarkSurface)
        assertEquals(Color(0xFF222825), KcalGrindDarkSurfaceElevated)
        assertEquals(Color(0xFF343B36), KcalGrindDarkBorder)
        assertEquals(Color(0xFFF4F6F4), KcalGrindDarkTextPrimary)
        assertEquals(Color(0xFFB2BAB5), KcalGrindDarkTextSecondary)
        assertEquals(Color(0xFF858F89), KcalGrindDarkTextMuted)
        assertEquals(Color(0xFF38C77B), KcalGrindDarkEmerald)
        assertEquals(Color(0xFF16834D), KcalGrindDarkStrongEmerald)
        assertEquals(Color(0xFF2B332E), KcalGrindDarkProgressTrack)
    }

    @Test
    fun `light theme tokens match approved V3 palette`() {
        assertEquals(Color(0xFFF2F4F2), KcalGrindLightBackground)
        assertEquals(Color(0xFFFFFFFF), KcalGrindLightSurface)
        assertEquals(Color(0xFFE7EBE8), KcalGrindLightSurfaceElevated)
        assertEquals(Color(0xFFD3DAD5), KcalGrindLightBorder)
        assertEquals(Color(0xFF171C19), KcalGrindLightTextPrimary)
        assertEquals(Color(0xFF505A53), KcalGrindLightTextSecondary)
        assertEquals(Color(0xFF68736B), KcalGrindLightTextMuted)
        assertEquals(Color(0xFF16834D), KcalGrindLightEmerald)
        assertEquals(Color(0xFF10663C), KcalGrindLightStrongEmerald)
        assertEquals(Color(0xFFDCE3DE), KcalGrindLightProgressTrack)
    }

    @Test
    fun `dark theme extended colors contain expected properties`() {
        val colors = KcalGrindV3DarkColors
        assertTrue(colors.isDark)
        assertEquals(KcalGrindDarkBackground, colors.background)
        assertEquals(KcalGrindDarkSurface, colors.surface)
        assertEquals(KcalGrindDarkSurfaceElevated, colors.surfaceElevated)
        assertEquals(KcalGrindDarkBorder, colors.subtleBorder)
        assertEquals(KcalGrindDarkTextPrimary, colors.textPrimary)
        assertEquals(KcalGrindDarkEmerald, colors.emeraldAccent)
        assertEquals(KcalGrindDarkProgressTrack, colors.progressTrack)
    }

    @Test
    fun `light theme extended colors contain expected properties`() {
        val colors = KcalGrindV3LightColors
        assertFalse(colors.isDark)
        assertEquals(KcalGrindLightBackground, colors.background)
        assertEquals(KcalGrindLightSurface, colors.surface)
        assertEquals(KcalGrindLightSurfaceElevated, colors.surfaceElevated)
        assertEquals(KcalGrindLightBorder, colors.subtleBorder)
        assertEquals(KcalGrindLightTextPrimary, colors.textPrimary)
        assertEquals(KcalGrindLightEmerald, colors.emeraldAccent)
        assertEquals(KcalGrindLightProgressTrack, colors.progressTrack)
    }

    @Test
    fun `dark and light typography contrast satisfies WCAG AA guidelines`() {
        // WCAG AA requires at least 4.5:1 for normal text, 3.0:1 for large text
        val darkContrast = calculateContrastRatio(KcalGrindDarkTextPrimary, KcalGrindDarkBackground)
        assertTrue("Dark contrast $darkContrast should be >= 4.5", darkContrast >= 4.5)

        val darkSecondaryContrast = calculateContrastRatio(KcalGrindDarkTextSecondary, KcalGrindDarkBackground)
        assertTrue("Dark secondary contrast $darkSecondaryContrast should be >= 4.5", darkSecondaryContrast >= 4.5)

        val lightContrast = calculateContrastRatio(KcalGrindLightTextPrimary, KcalGrindLightBackground)
        assertTrue("Light contrast $lightContrast should be >= 4.5", lightContrast >= 4.5)

        val lightSecondaryContrast = calculateContrastRatio(KcalGrindLightTextSecondary, KcalGrindLightBackground)
        assertTrue("Light secondary contrast $lightSecondaryContrast should be >= 4.5", lightSecondaryContrast >= 4.5)
    }

    private fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val lum1 = calculateLuminance(foreground)
        val lum2 = calculateLuminance(background)
        val brightest = max(lum1, lum2)
        val darkest = min(lum1, lum2)
        return (brightest + 0.05) / (darkest + 0.05)
    }

    private fun calculateLuminance(color: Color): Double {
        fun channelLum(c: Float): Double {
            return if (c <= 0.03928f) {
                c / 12.92
            } else {
                ((c + 0.055) / 1.055).pow(2.4)
            }
        }
        val r = channelLum(color.red)
        val g = channelLum(color.green)
        val b = channelLum(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}
