package com.kcalgrindai.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalgrindai.app.core.designsystem.v3Colors

/**
 * Shared circular icon button adhering to Material 3 accessibility standards:
 * - Strict [CircleShape] boundary for all press indicators, ripples, and shadows.
 * - Guaranteed minimum 48dp touch target for accessibility compliance.
 * - Explicit [shadowElevation] shaped with [CircleShape] (never square).
 * - Bounded circular ripple strictly within the circular boundary.
 */
@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    visualSize: Dp = 40.dp,
    containerColor: Color = MaterialTheme.v3Colors.surfaceElevated,
    contentColor: Color = MaterialTheme.v3Colors.textPrimary,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val minTouchTarget = maxOf(size, 48.dp)

    Box(
        modifier = modifier
            .sizeIn(minWidth = minTouchTarget, minHeight = minTouchTarget)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics(mergeDescendants = true) {
                        this.contentDescription = contentDescription
                        this.role = Role.Button
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(visualSize)
                .then(
                    if (shadowElevation > 0.dp) {
                        Modifier.shadow(shadowElevation, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clip(CircleShape),
            enabled = enabled,
            shape = CircleShape,
            color = containerColor,
            contentColor = contentColor,
            border = border,
            shadowElevation = shadowElevation
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    content()
                }
            }
        }
    }
}
