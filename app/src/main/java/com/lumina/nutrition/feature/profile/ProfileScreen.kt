package com.lumina.nutrition.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaOutline
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondary
import com.lumina.nutrition.core.designsystem.LuminaSecondaryContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.LuminaTertiary
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.usecase.NutritionCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTab: (String) -> Unit = {},
    onNavigateToAddFood: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    Scaffold(
        bottomBar = {
            com.lumina.nutrition.core.navigation.LuminaBottomNavigationBar(
                currentRoute = com.lumina.nutrition.core.navigation.LuminaRoute.Profile.route,
                onNavigateToTab = onNavigateToTab,
                onOpenAddFood = onNavigateToAddFood
            )
        },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onNavigateBack),
                    shape = CircleShape,
                    color = LuminaSurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuminaBackground)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuminaPrimary)
                }
            }
            is ProfileUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuminaBackground)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No profile found. Please complete onboarding.", color = LuminaOnSurfaceVariant)
                }
            }
            is ProfileUiState.Content -> {
                val profile = state.profile
                val goal = state.goal
                val isMetric = profile.units == UnitSystem.METRIC

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuminaBackground)
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = LuminaSurfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                color = LuminaPrimary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "🌱",
                                        fontSize = 28.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lumina Member",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LuminaOnSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${profile.age} yrs • ${if (isMetric) "${profile.heightCm.toInt()} cm" else "${NutritionCalculator.cmToFtIn(profile.heightCm).first}'${NutritionCalculator.cmToFtIn(profile.heightCm).second}\""}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = LuminaOnSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LuminaSecondaryContainer
                                ) {
                                    Text(
                                        text = profile.goal.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = LuminaPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Nutrition Targets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bento Grid: 4 Metric Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCard(
                            label = "Current Weight",
                            value = if (isMetric) "${profile.weightKg.toInt()} kg" else "${NutritionCalculator.kgToLb(profile.weightKg)} lb",
                            modifier = Modifier.weight(1f)
                        )
                        BentoCard(
                            label = "Goal Weight",
                            value = if (isMetric) "${profile.goalWeightKg.toInt()} kg" else "${NutritionCalculator.kgToLb(profile.goalWeightKg)} lb",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoCard(
                            label = "Daily Energy",
                            value = "${goal?.calories ?: 2000} kcal",
                            color = LuminaPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        BentoCard(
                            label = "Daily Water",
                            value = "${goal?.waterLiters ?: 2.5} L",
                            color = LuminaSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Macro distribution row
                    if (goal != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = LuminaSurfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Target Macro Split",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = LuminaOnSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Protein", style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant))
                                        Text(text = "${goal.proteinG.toInt()}g", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminaPrimary))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Carbs", style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant))
                                        Text(text = "${goal.carbsG.toInt()}g", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminaSecondary))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Fat", style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant))
                                        Text(text = "${goal.fatG.toInt()}g", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminaTertiary))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Preferences & Settings List
                    Text(
                        text = "Preferences & Units",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = LuminaSurfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SettingRow(
                                title = "Units of Measurement",
                                value = if (isMetric) "Metric (kg, cm)" else "Imperial (lb, ft/in)",
                                onClick = { viewModel.toggleUnitSystem() }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SettingRow(
                                title = "Activity Level",
                                value = profile.activityLevel.label,
                                onClick = null
                            )

                            if (profile.dietTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Column {
                                    Text(
                                        text = "Dietary Preferences",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = LuminaOnSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        profile.dietTags.forEach { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = LuminaSecondaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = tag.replace("_", " ").replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall.copy(color = LuminaPrimary),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (profile.allergies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Column {
                                    Text(
                                        text = "Allergies & Intolerances",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = LuminaOnSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        profile.allergies.forEach { allergy ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = LuminaSecondaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = allergy.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall.copy(color = LuminaPrimary),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SettingRow(
                                title = "Account",
                                value = when {
                                    !profile.email.isNullOrBlank() -> profile.email
                                    !profile.firebaseUid.isNullOrBlank() -> "Guest (Tap to link)"
                                    else -> "Sign In / Link"
                                },
                                onClick = onNavigateToAuth
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cloud Sync Section (Phase 10)
                    Text(
                        text = "Cloud Sync",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = LuminaSurfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Last sync info
                            val lastSync = viewModel.lastSyncTimestamp
                            val lastSyncText = if (lastSync > 0L) {
                                val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                                "Last synced: ${fmt.format(Date(lastSync))}"
                            } else {
                                "Never synced"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sync Data",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = LuminaOnSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastSyncText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LuminaOnSurfaceVariant
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Sync button
                                val isSyncing = syncState is SyncState.Syncing
                                Surface(
                                    modifier = Modifier
                                        .clickable(enabled = !isSyncing) { viewModel.onSyncNow() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSyncing) LuminaOutline.copy(alpha = 0.3f) else LuminaPrimary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = LuminaOnPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Syncing…",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = LuminaOnSurface
                                                )
                                            )
                                        } else {
                                            Text(
                                                text = "☁ Sync Now",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = LuminaOnPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Status message
                            when (val state = syncState) {
                                is SyncState.Success -> {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = LuminaSecondaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "✓ Sync complete",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = LuminaPrimary
                                                )
                                            )
                                            val pushed = state.result.push.pushed
                                            if (pushed.isNotEmpty()) {
                                                Text(
                                                    text = "Pushed: ${pushed.entries.joinToString { "${it.value} ${it.key.replace("_", " ")}" }}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                                                )
                                            }
                                        }
                                    }
                                }
                                is SyncState.Error -> {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.dismissSyncResult() },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = "✕ ${state.message}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                                else -> { /* Idle or Syncing — no extra message */ }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Backs up your data to the cloud for cross-device access.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LuminaOnSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = LuminaOnSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = LuminaOnSurfaceVariant))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = LuminaOnSurface
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LuminaPrimary
                )
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = " ›", fontSize = 16.sp, color = LuminaOnSurfaceVariant)
            }
        }
    }
}
