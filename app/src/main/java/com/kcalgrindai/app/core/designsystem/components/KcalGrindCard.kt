package com.kcalgrindai.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.kcalgrindai.app.core.designsystem.v3Colors

@Composable
fun KcalGrindAISelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val v3 = MaterialTheme.v3Colors
    val borderColor by animateColorAsState(
        targetValue = if (selected) v3.emeraldAccent else v3.subtleBorder,
        label = "cardBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            if (v3.isDark) v3.emeraldAccent.copy(alpha = 0.12f) else v3.emeraldAccent.copy(alpha = 0.08f)
        } else {
            v3.surface
        },
        label = "cardContainer"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
