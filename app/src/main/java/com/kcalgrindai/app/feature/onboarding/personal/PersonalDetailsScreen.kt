package com.kcalgrindai.app.feature.onboarding.personal

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.core.designsystem.KcalGrindOnPrimary
import com.kcalgrindai.app.core.designsystem.KcalGrindPrimary
import com.kcalgrindai.app.core.designsystem.components.KcalGrindPrimaryButton
import com.kcalgrindai.app.core.designsystem.components.KcalGrindTextField
import com.kcalgrindai.app.core.designsystem.components.OnboardingTopBar
import com.kcalgrindai.app.core.designsystem.components.StickyBottomBar
import com.kcalgrindai.app.domain.model.UnitSystem

@Composable
fun PersonalDetailsScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PersonalDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 6,
                totalSteps = 14,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                KcalGrindPrimaryButton(
                    text = "Continue",
                    enabled = uiState.isValid,
                    onClick = onNavigateNext
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Personal details",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Age and height help determine your basal metabolic rate (BMR).",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // First Name input (Optional - per deliberate deviation from 05_personal_details.html)
            KcalGrindTextField(
                value = uiState.nameInput,
                onValueChange = viewModel::onNameChanged,
                label = "First Name (Optional)",
                placeholder = "e.g. Alex",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Unit toggle (Metric vs Imperial)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isMetric = uiState.units == UnitSystem.METRIC
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { viewModel.setUnits(UnitSystem.METRIC) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMetric) KcalGrindPrimary else MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Metric (cm)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMetric) KcalGrindOnPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { viewModel.setUnits(UnitSystem.IMPERIAL) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isMetric) KcalGrindPrimary else MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Imperial (ft/in)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!isMetric) KcalGrindOnPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Age input
            KcalGrindTextField(
                value = uiState.ageInput,
                onValueChange = viewModel::onAgeChanged,
                label = "Age",
                placeholder = "e.g. 28",
                suffixText = "years",
                errorMessage = uiState.ageError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Height input
            if (uiState.units == UnitSystem.METRIC) {
                KcalGrindTextField(
                    value = uiState.heightCmInput,
                    onValueChange = viewModel::onHeightCmChanged,
                    label = "Height",
                    placeholder = "e.g. 175",
                    suffixText = "cm",
                    errorMessage = uiState.heightError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        KcalGrindTextField(
                            value = uiState.heightFtInput,
                            onValueChange = viewModel::onHeightFtChanged,
                            label = "Feet",
                            placeholder = "5",
                            suffixText = "ft",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        KcalGrindTextField(
                            value = uiState.heightInInput,
                            onValueChange = viewModel::onHeightInChanged,
                            label = "Inches",
                            placeholder = "9",
                            suffixText = "in",
                            errorMessage = uiState.heightError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
