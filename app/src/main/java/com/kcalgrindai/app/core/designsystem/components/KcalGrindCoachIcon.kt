package com.kcalgrindai.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean, professional brand vector icon for Kcal Grind AI Nutrition Coach.
 * Combines a sleek coaching dialogue bubble with an organic nutritional leaf curve.
 * Adheres strictly to design guidelines: no sparkles, no stars, no psychology/brain cliches.
 */
@Composable
fun KcalGrindCoachIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp,
    strokeWidthDp: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = strokeWidthDp.toPx()

        // 1. Dialogue Bubble Body
        // Leave room for the speech tail at the bottom-left
        val bubbleHeight = h * 0.76f
        val bubbleWidth = w * 0.90f
        val bubbleLeft = w * 0.05f
        val bubbleTop = h * 0.06f
        val radius = w * 0.22f

        val bubblePath = Path().apply {
            // Main rounded rectangular bubble with integrated dialogue tail
            addRoundRect(
                RoundRect(
                    left = bubbleLeft,
                    top = bubbleTop,
                    right = bubbleLeft + bubbleWidth,
                    bottom = bubbleTop + bubbleHeight,
                    cornerRadius = CornerRadius(radius, radius)
                )
            )
            // Dialogue tail at bottom-left
            moveTo(bubbleLeft + radius * 0.8f, bubbleTop + bubbleHeight)
            lineTo(bubbleLeft + radius * 0.3f, h * 0.94f)
            lineTo(bubbleLeft + radius * 1.6f, bubbleTop + bubbleHeight)
        }

        drawPath(
            path = bubblePath,
            color = tint,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Centered Nutrition Leaf Motif
        // A minimal, clean curved leaf inside the coaching bubble representing nutrition
        val leafPath = Path().apply {
            val startX = w * 0.36f
            val startY = bubbleTop + bubbleHeight * 0.64f
            val tipX = w * 0.64f
            val tipY = bubbleTop + bubbleHeight * 0.32f

            // Left curved contour
            moveTo(startX, startY)
            cubicTo(
                w * 0.32f, bubbleTop + bubbleHeight * 0.36f,
                w * 0.46f, tipY,
                tipX, tipY
            )
            // Right curved contour
            cubicTo(
                w * 0.68f, bubbleTop + bubbleHeight * 0.58f,
                w * 0.54f, startY,
                startX, startY
            )
            // Center vein
            moveTo(startX, startY)
            lineTo(w * 0.52f, bubbleTop + bubbleHeight * 0.46f)
        }

        drawPath(
            path = leafPath,
            color = tint,
            style = Stroke(
                width = stroke * 0.85f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
