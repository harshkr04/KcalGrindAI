package com.kcalgrindai.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalgrindai.app.core.designsystem.v3Colors

@Composable
fun KcalGrindBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentDescription: String = "Navigate back"
) {
    val v3 = MaterialTheme.v3Colors
    val resolvedContainer = containerColor ?: v3.surfaceElevated
    val resolvedContent = contentColor ?: v3.textPrimary

    CircleIconButton(
        onClick = onClick,
        modifier = modifier,
        visualSize = 38.dp,
        containerColor = resolvedContainer,
        contentColor = resolvedContent,
        border = androidx.compose.foundation.BorderStroke(1.dp, v3.subtleBorder),
        shadowElevation = 1.dp,
        contentDescription = contentDescription
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = resolvedContent,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun KcalGrindTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                KcalGrindBackButton(onClick = onBackClick)
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = v3.textPrimary
                ),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int = 14,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                KcalGrindBackButton(onClick = onBackClick)
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = v3.emeraldAccent
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // True Segmented Capsule Bar per V3 design system
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (step in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            if (step <= currentStep) v3.emeraldAccent
                            else v3.progressTrack
                        )
                        .semantics {
                            contentDescription = "Step segment $step of $totalSteps"
                        }
                )
            }
        }
    }
}
