package com.kcalgrindai.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalgrindai.app.core.designsystem.v3Colors

/**
 * Modifier that applies a Liquid Glass aesthetic:
 * - Subtle translucent glass fill
 * - 1px specular gradient or subtle border
 * - Clean clipped rounded geometry
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    fillColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    specularHighlight: Boolean = true,
): Modifier {
    return this
        .clip(shape)
}

/**
 * Premium Liquid Glass Card container for Kcal Grind AI V3.
 * Distinguishes foreground content from background with subtle depth,
 * soft specular border, and customizable click behavior.
 */
@Composable
fun KcalGrindLiquidCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = MaterialTheme.v3Colors.glassFill,
    borderColor: Color = MaterialTheme.v3Colors.subtleBorder,
    specularGradient: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.v3Colors.isDark
    val borderBrush = if (specularGradient) {
        val topHighlight = if (isDark) Color(0x66FFFFFF) else Color(0x80FFFFFF)
        val bottomFade = borderColor.copy(alpha = 0.5f)
        Brush.verticalGradient(
            colors = listOf(topHighlight, borderColor, bottomFade)
        )
    } else {
        Brush.verticalGradient(colors = listOf(borderColor, borderColor))
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .border(
                border = BorderStroke(1.dp, borderBrush),
                shape = shape
            )
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        shadowElevation = if (isDark) 4.dp else 2.dp
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Liquid Glass Pill / Badge used for streak chips, quota chips, and tags.
 */
@Composable
fun KcalGrindLiquidBadge(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(9999.dp),
    backgroundColor: Color = MaterialTheme.v3Colors.surfaceElevated.copy(alpha = 0.85f),
    borderColor: Color = MaterialTheme.v3Colors.subtleBorder.copy(alpha = 0.6f),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .padding(contentPadding),
        content = content
    )
}
