package com.lumina.nutrition.feature.onboarding.allergies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.components.LuminaFilterChip
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton
import com.lumina.nutrition.core.designsystem.components.OnboardingTopBar
import com.lumina.nutrition.core.designsystem.components.StickyBottomBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllergiesScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AllergiesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 4,
                totalSteps = 10,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                LuminaPrimaryButton(
                    text = "Continue",
                    onClick = onNavigateNext
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
                text = "Any allergies or intolerances?",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Lumina will warn you whenever detected foods contain potential allergens.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.availableAllergens.forEach { allergen ->
                    val isSelected = uiState.selectedAllergies.contains(allergen.id)
                    LuminaFilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleAllergen(allergen.id) },
                        label = "${allergen.icon}  ${allergen.label}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
