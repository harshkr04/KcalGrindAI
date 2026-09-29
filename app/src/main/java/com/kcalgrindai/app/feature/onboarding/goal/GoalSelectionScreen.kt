package com.kcalgrindai.app.feature.onboarding.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.core.designsystem.KcalGrindPrimary
import com.kcalgrindai.app.core.designsystem.KcalGrindSecondaryContainer
import com.kcalgrindai.app.core.designsystem.components.KcalGrindPrimaryButton
import com.kcalgrindai.app.core.designsystem.components.KcalGrindAISelectableCard
import com.kcalgrindai.app.core.designsystem.components.OnboardingTopBar
import com.kcalgrindai.app.core.designsystem.components.StickyBottomBar
import com.kcalgrindai.app.domain.model.GoalType

@Composable
fun GoalSelectionScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GoalSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                KcalGrindPrimaryButton(
                    text = "Continue",
                    onClick = onNavigateNext
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "What is your main goal?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "We'll tailor your daily energy and macro targets accordingly.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(
                items = uiState.availableGoals,
                key = { it.name }
            ) { goal ->
                val isSelected = goal == uiState.selectedGoal
                val icon = when (goal) {
                    GoalType.LOSE -> androidx.compose.material.icons.Icons.Default.TrendingDown
                    GoalType.MAINTAIN -> androidx.compose.material.icons.Icons.Default.Scale
                    GoalType.GAIN -> androidx.compose.material.icons.Icons.Default.TrendingUp
                    GoalType.MUSCLE -> androidx.compose.material.icons.Icons.Default.FitnessCenter
                    GoalType.HEALTHIER -> androidx.compose.material.icons.Icons.Default.Spa
                    GoalType.NUTRITION -> androidx.compose.material.icons.Icons.Default.BarChart
                }

                KcalGrindAISelectableCard(
                    selected = isSelected,
                    onClick = { viewModel.selectGoal(goal) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = KcalGrindSecondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = KcalGrindPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = goal.blurb,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectGoal(goal) },
                            colors = RadioButtonDefaults.colors(selectedColor = KcalGrindPrimary)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
