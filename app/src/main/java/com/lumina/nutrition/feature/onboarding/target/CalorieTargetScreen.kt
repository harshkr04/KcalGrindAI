package com.lumina.nutrition.feature.onboarding.target

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondary
import com.lumina.nutrition.core.designsystem.LuminaSecondaryContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.LuminaTertiary
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton
import com.lumina.nutrition.core.designsystem.components.LuminaProgressRing
import com.lumina.nutrition.core.designsystem.components.LuminaSecondaryButton
import com.lumina.nutrition.core.designsystem.components.OnboardingTopBar
import com.lumina.nutrition.core.designsystem.components.StickyBottomBar

@Composable
fun CalorieTargetScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CalorieTargetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 8,
                totalSteps = 10,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LuminaPrimaryButton(
                        text = "Continue",
                        onClick = onNavigateNext
                    )
                    LuminaSecondaryButton(
                        text = "Customize Targets",
                        onClick = onNavigateNext
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CalorieTargetUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuminaBackground)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = LuminaPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Calculating personalized nutrition plan...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = LuminaOnSurfaceVariant)
                        )
                    }
                }
            }
            is CalorieTargetUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuminaBackground)
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your daily target",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.explanation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LuminaOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Animated Progress Ring
                    LuminaProgressRing(calories = state.calories)

                    Spacer(modifier = Modifier.height(28.dp))

                    // Macro Split Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroCard(
                            label = "Protein",
                            amount = "${state.proteinG.toInt()}g",
                            color = LuminaPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            label = "Carbs",
                            amount = "${state.carbsG.toInt()}g",
                            color = LuminaSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            label = "Fat",
                            amount = "${state.fatG.toInt()}g",
                            color = LuminaTertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hydration & Metabolic Rate Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = LuminaSurfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "💧 Water", style = MaterialTheme.typography.labelMedium.copy(color = LuminaOnSurfaceVariant))
                                Text(
                                    text = "${state.waterLiters}L (${state.waterGlasses} glasses)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = LuminaOnSurface)
                                )
                            }
                            if (state.bmr != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "⚡ Base BMR", style = MaterialTheme.typography.labelMedium.copy(color = LuminaOnSurfaceVariant))
                                    Text(
                                        text = "${state.bmr.toInt()} kcal",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = LuminaOnSurface)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MacroCard(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = LuminaOnSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
        }
    }
}
