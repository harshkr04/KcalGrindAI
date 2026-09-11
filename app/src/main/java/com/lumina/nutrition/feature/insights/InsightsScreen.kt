package com.lumina.nutrition.feature.insights

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurface
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.navigation.LuminaBottomNavigationBar
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.domain.model.WeightEntry
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToWeightLog: (() -> Unit)? = null,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogWeightDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            InsightsTopBar(
                selectedTab = uiState.selectedTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        },
        bottomBar = {
            LuminaBottomNavigationBar(
                currentRoute = LuminaRoute.Insights.route,
                onNavigateToTab = onNavigateToTab,
                onOpenAddFood = onNavigateToAddFood
            )
        },
        containerColor = LuminaSurface
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LuminaPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.selectedTab) {
                    InsightsTab.CALORIES -> {
                        // 1. Weekly Calorie Summary Card
                        item {
                            WeeklyCalorieSummaryCard(
                                avgCalories = uiState.avgCaloriesPerDay,
                                targetCalories = uiState.targetCaloriesPerDay,
                                daysTracked = uiState.daysTrackedCount,
                                totalDays = uiState.totalDaysCount,
                                trackingRate = uiState.trackingRatePercent,
                                calorieDelta = uiState.calorieDelta
                            )
                        }

                        // 2. Custom 7-Day Calorie Bar Chart
                        item {
                            CalorieBarChartCard(
                                points = uiState.calorieTrend,
                                targetCalories = uiState.targetCaloriesPerDay
                            )
                        }

                        // 3. Macro Adherence Breakdown Card
                        item {
                            MacroAdherenceCard(
                                protein = uiState.proteinAdherence,
                                carbs = uiState.carbsAdherence,
                                fat = uiState.fatAdherence
                            )
                        }

                        // 4. Hydration Summary Card
                        item {
                            HydrationSummaryCard(
                                avgWaterMl = uiState.avgWaterMlPerDay,
                                targetWaterMl = uiState.targetWaterMlPerDay
                            )
                        }
                    }

                    InsightsTab.WEIGHT -> {
                        // 1. Weight Summary Card
                        item {
                            WeightSummaryCard(
                                latestWeight = uiState.latestWeightKg,
                                startWeight = uiState.startWeightKg,
                                goalWeight = uiState.goalWeightKg,
                                weightChange = uiState.weightChangeKg,
                                onLogWeightClick = {
                                    if (onNavigateToWeightLog != null) {
                                        onNavigateToWeightLog()
                                    } else {
                                        showLogWeightDialog = true
                                    }
                                }
                            )
                        }

                        // 2. Weight History List
                        if (uiState.hasWeightData) {
                            item {
                                Text(
                                    text = "Weight History",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LuminaOnSurface
                                    )
                                )
                            }
                            items(uiState.weightEntries) { entry ->
                                WeightHistoryItemRow(entry = entry)
                            }
                        } else {
                            item {
                                EmptyWeightCard(onLogWeightClick = {
                                    if (onNavigateToWeightLog != null) {
                                        onNavigateToWeightLog()
                                    } else {
                                        showLogWeightDialog = true
                                    }
                                })
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showLogWeightDialog) {
        LogWeightDialog(
            initialWeight = uiState.latestWeightKg ?: 70.0,
            onDismiss = { showLogWeightDialog = false },
            onConfirm = { weight ->
                viewModel.logWeight(weight)
                showLogWeightDialog = false
            }
        )
    }
}

@Composable
private fun InsightsTopBar(
    selectedTab: InsightsTab,
    onSelectTab: (InsightsTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LuminaSurface)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Insights & Trends",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = LuminaOnSurface,
                fontSize = 24.sp
            )
        )
        Text(
            text = "Weekly nutrition performance & consistency",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LuminaOnSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LuminaSurfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    title = "Calories & Macros",
                    isSelected = selectedTab == InsightsTab.CALORIES,
                    onClick = { onSelectTab(InsightsTab.CALORIES) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Weight Progress",
                    isSelected = selectedTab == InsightsTab.WEIGHT,
                    onClick = { onSelectTab(InsightsTab.WEIGHT) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) LuminaPrimary else Color.Transparent,
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else LuminaOnSurfaceVariant,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun WeeklyCalorieSummaryCard(
    avgCalories: Int,
    targetCalories: Int,
    daysTracked: Int,
    totalDays: Int,
    trackingRate: Int,
    calorieDelta: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Average Intake",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LuminaOnSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$avgCalories kcal / day",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface,
                            fontSize = 22.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (calorieDelta <= 50) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    val label = if (calorieDelta <= 0) "${-calorieDelta} kcal deficit" else "+$calorieDelta kcal surplus"
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (calorieDelta <= 50) Color(0xFF2E7D32) else Color(0xFFE65100)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking Consistency Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracking Consistency",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "$daysTracked of $totalDays days ($trackingRate%)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (trackingRate / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = LuminaPrimary,
                trackColor = LuminaPrimary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun CalorieBarChartCard(
    points: List<DayCaloriePoint>,
    targetCalories: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Calorie Trend",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Target: $targetCalories kcal",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = LuminaOnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Compose Bar Chart
            val maxCalorie = maxOf(targetCalories * 1.25f, (points.maxOfOrNull { it.calories } ?: targetCalories).toFloat())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { pt ->
                    val fraction = if (maxCalorie > 0) (pt.calories / maxCalorie).coerceIn(0f, 1f) else 0f
                    val isNearTarget = pt.calories in (targetCalories - 200)..(targetCalories + 200)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (pt.hasLogs) {
                            Text(
                                text = "${pt.calories}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuminaOnSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // The Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(fraction.coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    when {
                                        !pt.hasLogs -> LuminaSurfaceContainerHigh
                                        isNearTarget -> LuminaPrimary
                                        pt.calories > targetCalories -> Color(0xFFF59E0B)
                                        else -> Color(0xFF646FD4)
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day of week label
                        Text(
                            text = pt.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (pt.hasLogs) FontWeight.Bold else FontWeight.Normal,
                                color = if (pt.hasLogs) LuminaOnSurface else LuminaOnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroAdherenceCard(
    protein: MacroAdherence,
    carbs: MacroAdherence,
    fat: MacroAdherence
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Macro Adherence",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            MacroAdherenceRow(
                name = "Protein",
                avgGrams = protein.avgGrams,
                targetGrams = protein.targetGrams,
                percent = protein.adherencePercent,
                color = Color(0xFF646FD4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MacroAdherenceRow(
                name = "Carbs",
                avgGrams = carbs.avgGrams,
                targetGrams = carbs.targetGrams,
                percent = carbs.adherencePercent,
                color = Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MacroAdherenceRow(
                name = "Fat",
                avgGrams = fat.avgGrams,
                targetGrams = fat.targetGrams,
                percent = fat.adherencePercent,
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
private fun MacroAdherenceRow(
    name: String,
    avgGrams: Double,
    targetGrams: Double,
    percent: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LuminaOnSurface
                )
            )
            Text(
                text = "${avgGrams.roundToInt()}g / ${targetGrams.roundToInt()}g ($percent%)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun HydrationSummaryCard(
    avgWaterMl: Int,
    targetWaterMl: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💧 Daily Hydration",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$avgWaterMl ml average / day",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LuminaOnSurfaceVariant
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE1F5FE)
            ) {
                Text(
                    text = "Target: $targetWaterMl ml",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WeightSummaryCard(
    latestWeight: Double?,
    startWeight: Double?,
    goalWeight: Double?,
    weightChange: Double?,
    onLogWeightClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Weight",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LuminaOnSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (latestWeight != null) "${"%.1f".format(latestWeight)} kg" else "-- kg",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface,
                            fontSize = 24.sp
                        )
                    )
                }

                Button(
                    onClick = onLogWeightClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log Weight", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeightMetricCol(
                    label = "Goal",
                    value = if (goalWeight != null) "${"%.1f".format(goalWeight)} kg" else "--"
                )
                WeightMetricCol(
                    label = "Change",
                    value = if (weightChange != null) "${if (weightChange > 0) "+" else ""}${"%.1f".format(weightChange)} kg" else "--"
                )
            }
        }
    }
}

@Composable
private fun WeightMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = LuminaOnSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LuminaOnSurface
            )
        )
    }
}

@Composable
private fun WeightHistoryItemRow(entry: WeightEntry) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LuminaSurfaceContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.date,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurface,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "${"%.1f".format(entry.weightKg)} kg",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaPrimary
                )
            )
        }
    }
}

@Composable
private fun EmptyWeightCard(onLogWeightClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⚖️", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No weight entries logged yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log your weight regularly to track progress toward your goal.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LuminaOnSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogWeightClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
            ) {
                Text("Log First Weight Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LogWeightDialog(
    initialWeight: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weightText by remember { mutableStateOf(initialWeight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Log Today's Weight", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(text = "Enter your current weight in kilograms (kg):")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminaPrimary,
                        unfocusedBorderColor = LuminaOnSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: initialWeight
                    onConfirm(w)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LuminaOnSurfaceVariant)
            }
        }
    )
}
