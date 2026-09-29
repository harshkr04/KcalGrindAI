package com.kcalgrindai.app.feature.insights

import java.util.Locale
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import com.kcalgrindai.app.domain.model.Recipe
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.IconButton
import com.kcalgrindai.app.core.navigation.KcalGrindRoute
import com.kcalgrindai.app.domain.model.WeightEntry
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToWeightLog: (() -> Unit)? = null,
    onNavigateToRecipeDetail: ((String) -> Unit)? = null,
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
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

                        // 5. Weekly Step / Activity Summary Card
                        item {
                            WeeklyStepSummaryCard(avgSteps = uiState.avgStepsPerDay)
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
                                entryCount = uiState.weightEntries.size,
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
                                        color = MaterialTheme.colorScheme.onSurface
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

                    InsightsTab.RECIPES -> {
                        item {
                            RecipeSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) }
                            )
                        }

                        // Sort Tabs: All | Popular | Favorites
                        item {
                            RecipeSortSection(
                                selectedSort = uiState.selectedSortOption,
                                onSelectSort = { viewModel.setSortOption(it) }
                            )
                        }

                        // Filter Row 1: Meal Type
                        item {
                            RecipeMealTypeFilterRow(
                                selectedMealType = uiState.selectedMealTypeFilter,
                                onSelectMealType = { viewModel.setMealTypeFilter(it) }
                            )
                        }

                        // Filter Row 2: Diet / Goal Tag
                        item {
                            RecipeDietTagFilterRow(
                                selectedDietTag = uiState.selectedDietTagFilter,
                                onSelectDietTag = { viewModel.setDietTagFilter(it) }
                            )
                        }

                        if (uiState.filteredRecipes.isEmpty()) {
                            item {
                                EmptyRecipesCard(
                                    onResetFilters = {
                                        viewModel.setMealTypeFilter(null)
                                        viewModel.setDietTagFilter(null)
                                        viewModel.setSortOption(RecipeSortOption.ALL)
                                    }
                                )
                            }
                        } else {
                            items(uiState.filteredRecipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onCardClick = { onNavigateToRecipeDetail?.invoke(recipe.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(recipe.id) }
                                )
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Insights & Trends",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp
            )
        )
        Text(
            text = "Weekly nutrition performance & consistency",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    title = "Calories",
                    isSelected = selectedTab == InsightsTab.CALORIES,
                    onClick = { onSelectTab(InsightsTab.CALORIES) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Weight",
                    isSelected = selectedTab == InsightsTab.WEIGHT,
                    onClick = { onSelectTab(InsightsTab.WEIGHT) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Recipes",
                    isSelected = selectedTab == InsightsTab.RECIPES,
                    onClick = { onSelectTab(InsightsTab.RECIPES) },
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
    val animatedBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 160, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "tabBg"
    )
    val animatedTextColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 160, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "tabText"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = animatedBgColor,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = animatedTextColor,
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Average Intake",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (daysTracked > 0) "$avgCalories kcal / day" else "Not enough data yet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (daysTracked > 0) 22.sp else 19.sp
                        )
                    )
                }

                if (daysTracked > 0) {
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) {
                            if (calorieDelta <= 50) Color(0xFF1E4B35) else Color(0xFF4E2C10)
                        } else {
                            if (calorieDelta <= 50) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        }
                    ) {
                        val label = if (calorieDelta <= 0) "${-calorieDelta} kcal deficit" else "+$calorieDelta kcal surplus"
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) {
                                    if (calorieDelta <= 50) Color(0xFF81C784) else Color(0xFFFFB74D)
                                } else {
                                    if (calorieDelta <= 50) Color(0xFF2E7D32) else Color(0xFFE65100)
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "$daysTracked of $totalDays days ($trackingRate%)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val conclusion = when {
                daysTracked >= 2 -> "Your average intake was $avgCalories kcal over the last 7 days."
                daysTracked == 1 -> "Logged for 1 day this week. Continue logging to reveal multi-day trends."
                else -> "Log your daily meals to reveal your 7-day intake averages and deficit trends."
            }
            Text(
                text = conclusion,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Target: $targetCalories kcal",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        !pt.hasLogs -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        isNearTarget -> MaterialTheme.colorScheme.primary
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
                                color = if (pt.hasLogs) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            MacroAdherenceRow(
                name = "Protein",
                avgGrams = protein.avgGrams,
                targetGrams = protein.targetGrams,
                percent = protein.adherencePercent,
                color = if (isDark) Color(0xFF818CF8) else Color(0xFF646FD4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MacroAdherenceRow(
                name = "Carbs",
                avgGrams = carbs.avgGrams,
                targetGrams = carbs.targetGrams,
                percent = carbs.adherencePercent,
                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MacroAdherenceRow(
                name = "Fat",
                avgGrams = fat.avgGrams,
                targetGrams = fat.targetGrams,
                percent = fat.adherencePercent,
                color = if (isDark) Color(0xFF34D399) else Color(0xFF10B981)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "${avgGrams.roundToInt()}g / ${targetGrams.roundToInt()}g ($percent%)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        color = MaterialTheme.colorScheme.surfaceContainer,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Daily Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$avgWaterMl ml average / day",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF003852) else Color(0xFFE0F2FE)
            ) {
                Text(
                    text = "Target: $targetWaterMl ml",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF78D1FF) else Color(0xFF0284C7)
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
    entryCount: Int = 0,
    onLogWeightClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (latestWeight != null) "${"%.1f".format(latestWeight)} kg" else "-- kg",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 24.sp
                        )
                    )
                }

                Button(
                    onClick = onLogWeightClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log Weight", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeightMetricCol(
                    label = "Target Goal",
                    value = if (goalWeight != null) "${"%.1f".format(goalWeight)} kg" else "--"
                )
                WeightMetricCol(
                    label = "Net Change",
                    value = if (entryCount >= 2 && weightChange != null) {
                        "${if (weightChange > 0) "+" else ""}${"%.1f".format(weightChange)} kg"
                    } else {
                        "--"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val weightConclusion = when {
                entryCount >= 2 && weightChange != null -> "Your weight changed by ${if (weightChange > 0) "+" else ""}${"%.1f".format(weightChange)} kg across $entryCount logged entries."
                latestWeight != null -> "1 weight entry recorded. Log your weight consistently over time to calculate trends."
                else -> "Not enough data yet. Log your weight to begin tracking progress toward your target."
            }

            Text(
                text = weightConclusion,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun WeeklyStepSummaryCard(avgSteps: Long?) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (avgSteps != null) {
                            "${java.text.NumberFormat.getNumberInstance(Locale.US).format(avgSteps)} steps / day"
                        } else {
                            "Not enough step data yet"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (avgSteps != null) {
                            "Synced from Google Health Connect"
                        } else {
                            "Connect Health Connect to track your 7-day movement trend"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun WeightHistoryItemRow(entry: WeightEntry) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "${"%.1f".format(entry.weightKg)} kg",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun EmptyWeightCard(onLogWeightClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Scale,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No weight entries logged yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log your weight regularly to track progress toward your goal.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogWeightClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Log First Weight Entry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
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
            Text(text = "Log Today's Weight", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column {
                Text(text = "Enter your current weight in kilograms (kg):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun RecipeLoggedBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun RecipeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search 40+ recipes, ingredients, tags...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun RecipeSortSection(
    selectedSort: RecipeSortOption,
    onSelectSort: (RecipeSortOption) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RecipeSortOption.entries.forEach { option ->
                val isSelected = selectedSort == option
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSort(option) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeMealTypeFilterRow(
    selectedMealType: String?,
    onSelectMealType: (String?) -> Unit
) {
    val mealTypes = listOf(
        "All" to null,
        "Breakfast" to "breakfast",
        "Lunch" to "lunch",
        "Dinner" to "dinner",
        "Snacks" to "snack",
        "Desserts" to "dessert"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Meal Type",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mealTypes) { (label, value) ->
                val isSelected = selectedMealType == value
                RecipeFilterChip(
                    label = label,
                    isSelected = isSelected,
                    onClick = { onSelectMealType(value) }
                )
            }
        }
    }
}

@Composable
private fun RecipeDietTagFilterRow(
    selectedDietTag: String?,
    onSelectDietTag: (String?) -> Unit
) {
    val dietTags = listOf(
        "All" to null,
        "High-Protein" to "High-Protein",
        "Indian" to "Indian",
        "Low-Calorie" to "Low-Calorie",
        "Low-Carb" to "Low-Carb",
        "Weight Loss" to "Weight Loss",
        "Muscle Gain" to "Muscle Gain",
        "Vegetarian" to "Vegetarian",
        "Vegan" to "Vegan",
        "Quick <15min" to "Quick <15min",
        "High Fiber" to "High Fiber",
        "Gluten Free" to "Gluten Free",
        "Post Workout" to "Post Workout",
        "Mediterranean" to "Mediterranean"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Diet & Goal",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dietTags) { (label, value) ->
                val isSelected = selectedDietTag == value
                RecipeFilterChip(
                    label = label,
                    isSelected = isSelected,
                    onClick = { onSelectDietTag(value) }
                )
            }
        }
    }
}

@Composable
private fun RecipeFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun EmptyRecipesCard(
    onResetFilters: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No matching recipes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try adjusting your search query, meal-type, or diet filters to see more recipes.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onResetFilters,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Reset Filters & Search",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                val mealIcon = when (recipe.mealType.uppercase()) {
                    "BREAKFAST" -> Icons.Filled.WbSunny
                    "LUNCH" -> Icons.Filled.Restaurant
                    "DINNER" -> Icons.Filled.DinnerDining
                    "SNACK" -> Icons.Filled.LocalCafe
                    else -> Icons.Filled.RestaurantMenu
                }
                Icon(
                    imageVector = mealIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = recipe.mealType.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "⏱ ${recipe.prepTimeMinutes} mins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    if (recipe.popularityScore >= 90.0) {
                        val isDarkCard = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Editor's Pick",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkCard) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${recipe.totalCalories.roundToInt()} kcal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "${recipe.proteinG}g protein",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Favorite Star Toggle
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = if (recipe.isFavorite) "Favorited" else "Favorite",
                    tint = if (recipe.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
