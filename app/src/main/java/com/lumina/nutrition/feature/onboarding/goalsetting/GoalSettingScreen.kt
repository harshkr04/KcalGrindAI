package com.lumina.nutrition.feature.onboarding.goalsetting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.LuminaTertiary
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton
import com.lumina.nutrition.core.designsystem.components.OnboardingTopBar
import com.lumina.nutrition.core.designsystem.components.StickyBottomBar

@Composable
fun GoalSettingScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GoalSettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 9,
                totalSteps = 10,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                LuminaPrimaryButton(
                    text = "Confirm & Continue",
                    onClick = {
                        viewModel.onConfirm()
                        onNavigateNext()
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LuminaBackground)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Fine-tune targets",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Adjust your daily macro distribution. We'll verify that your macros align with your total calorie target.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Calories & Balance status card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = LuminaSurfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Calories",
                            style = MaterialTheme.typography.labelLarge.copy(color = LuminaOnSurfaceVariant)
                        )
                        Text(
                            text = "${uiState.calories} kcal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Macro Sum",
                            style = MaterialTheme.typography.bodyMedium.copy(color = LuminaOnSurfaceVariant)
                        )
                        Text(
                            text = "${uiState.sumCalories} kcal (${if (uiState.diffCalories >= 0) "+" else ""}${uiState.diffCalories})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.withinTolerance) LuminaPrimary else LuminaError
                            )
                        )
                    }

                    if (!uiState.withinTolerance) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Macro sum differs significantly from total calorie target.",
                            style = MaterialTheme.typography.bodySmall.copy(color = LuminaError)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Protein Slider
            MacroSliderCard(
                label = "Protein",
                grams = uiState.proteinG.toInt(),
                pct = uiState.proteinPct,
                color = LuminaPrimary,
                value = uiState.proteinG.toFloat(),
                valueRange = 40f..300f,
                onValueChange = { viewModel.onProteinChanged(it.toDouble()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Carbs Slider
            MacroSliderCard(
                label = "Carbs",
                grams = uiState.carbsG.toInt(),
                pct = uiState.carbsPct,
                color = LuminaSecondary,
                value = uiState.carbsG.toFloat(),
                valueRange = 20f..450f,
                onValueChange = { viewModel.onCarbsChanged(it.toDouble()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fat Slider
            MacroSliderCard(
                label = "Fat",
                grams = uiState.fatG.toInt(),
                pct = uiState.fatPct,
                color = LuminaTertiary,
                value = uiState.fatG.toFloat(),
                valueRange = 20f..180f,
                onValueChange = { viewModel.onFatChanged(it.toDouble()) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MacroSliderCard(
    label: String,
    grams: Int,
    pct: Int,
    color: androidx.compose.ui.graphics.Color,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "${grams}g  ($pct%)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color
                )
            )
        }
    }
}
