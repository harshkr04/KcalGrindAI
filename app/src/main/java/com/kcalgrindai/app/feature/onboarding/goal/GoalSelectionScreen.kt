package com.kcalgrindai.app.feature.onboarding.goal

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcalgrindai.app.core.designsystem.KcalGrindOnPrimary
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.R
import com.kcalgrindai.app.core.designsystem.components.KcalGrindLiquidCard
import com.kcalgrindai.app.core.designsystem.components.OnboardingTopBar
import com.kcalgrindai.app.core.designsystem.components.StickyBottomBar
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.domain.model.GoalType

@Composable
fun GoalSelectionScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GoalSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val v3 = MaterialTheme.v3Colors

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 2,
                totalSteps = 14,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onNavigateNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = v3.emeraldAccent,
                            contentColor = KcalGrindOnPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You can adjust your goals or calorie targets anytime in Settings.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(v3.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Headline Section per V3 spec
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "What is your primary fitness mission?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = v3.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sproutling tunes your calorie targets and macro splits accordingly.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = v3.textSecondary,
                        lineHeight = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Interactive Bento Goal Cards
            items(
                items = uiState.availableGoals,
                key = { it.name }
            ) { goal ->
                val isSelected = goal == uiState.selectedGoal
                val icon: ImageVector = when (goal) {
                    GoalType.LOSE -> Icons.Default.LocalFireDepartment
                    GoalType.MAINTAIN -> Icons.Default.Scale
                    GoalType.GAIN -> Icons.AutoMirrored.Filled.TrendingUp
                    GoalType.MUSCLE -> Icons.Default.FitnessCenter
                    GoalType.HEALTHIER -> Icons.Default.Spa
                    GoalType.NUTRITION -> Icons.Default.BarChart
                }

                val tagText: String? = when (goal) {
                    GoalType.LOSE -> "-450 kcal deficit â€¢ High protein"
                    GoalType.MUSCLE -> "+250 kcal surplus â€¢ Lean gains"
                    GoalType.GAIN -> "+350 kcal surplus â€¢ Mass pacing"
                    GoalType.MAINTAIN -> "Maintenance calories â€¢ Balanced macros"
                    GoalType.HEALTHIER -> "Clean nutrition â€¢ Whole foods focus"
                    GoalType.NUTRITION -> "Consistent logging â€¢ Macro awareness"
                }

                val borderAnimColor by animateColorAsState(
                    targetValue = if (isSelected) v3.emeraldAccent else v3.subtleBorder,
                    label = "border"
                )

                val cardShape = RoundedCornerShape(16.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderAnimColor,
                            shape = cardShape
                        )
                        .clickable { viewModel.selectGoal(goal) },
                    shape = cardShape,
                    color = if (isSelected) {
                        if (v3.isDark) v3.emeraldAccent.copy(alpha = 0.12f) else v3.emeraldAccent.copy(alpha = 0.08f)
                    } else {
                        v3.surface
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Icon Box
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) v3.emeraldAccent.copy(alpha = 0.2f) else v3.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) v3.emeraldAccent.copy(alpha = 0.4f) else v3.subtleBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) v3.emeraldAccent else v3.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = goal.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = v3.textPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = goal.blurb,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = v3.textSecondary,
                                        fontSize = 12.sp
                                    )
                                )

                                if (tagText != null && isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = v3.emeraldAccent.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(v3.emeraldAccent)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tagText,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = v3.emeraldAccent,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Custom Radio Checkmark per V3 spec
                        Surface(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape),
                            shape = CircleShape,
                            color = if (isSelected) v3.emeraldAccent else Color.Transparent,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, v3.subtleBorder) else null
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = KcalGrindOnPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Target Metric Live Estimate Preview (Glass Card with Liquid Depth)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                val selectedGoal = uiState.selectedGoal
                val estimatedCals = 2000 + selectedGoal.delta
                val estimatedProtein = (selectedGoal.proteinPerKg * 75.0).toInt()
                val paceText = when {
                    selectedGoal.delta < 0 -> "-20% Deficit"
                    selectedGoal.delta > 0 -> "+15% Surplus"
                    else -> "Maintenance"
                }

                KcalGrindLiquidCard(
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    backgroundColor = v3.surfaceElevated.copy(alpha = 0.9f),
                    borderColor = v3.subtleBorder
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(v3.emeraldAccent)
                                )
                                Text(
                                    text = "ESTIMATED TARGET FOR ${selectedGoal.label.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = v3.textPrimary,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = v3.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, v3.subtleBorder)
                            ) {
                                Text(
                                    text = "AI Calibration",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = v3.emeraldAccent,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Metrics Preview Bento Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Calorie Target
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = v3.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, v3.subtleBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$estimatedCals",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = v3.textPrimary,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = "kcal / day",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = v3.emeraldAccent,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Protein Target
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = v3.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, v3.subtleBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${estimatedProtein}g",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = v3.textPrimary,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = "Protein target",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = v3.textSecondary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Deficit/Surplus Pace
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = v3.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, v3.subtleBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = paceText.substringBefore(" "),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = v3.emeraldAccent,
                                            fontSize = 18.sp
                                        )
                                    )
                                    Text(
                                        text = "Target pace",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = v3.textMuted,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Sproutling Companion Micro-Avatar & AI Coaching Bubble
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = v3.surface.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = v3.emeraldAccent.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.4f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_sproutling_active),
                                            contentDescription = "Sproutling",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Based on your fitness mission, $estimatedCals kcal is your estimated target!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = v3.textPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
