package com.lumina.nutrition.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.components.LuminaProgressRing
import com.lumina.nutrition.core.navigation.LuminaBottomNavigationBar
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.domain.model.MealType

@Composable
fun HomeScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToMealDetail: (Long) -> Unit,
    onNavigateToAiCoach: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.createPermissionRequestContract()
    ) {
        viewModel.refreshActivity()
    }

    Scaffold(
        bottomBar = {
            LuminaBottomNavigationBar(
                currentRoute = LuminaRoute.Home.route,
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Top Header (Greeting & AI Coach Button)
                item {
                    HomeHeader(
                        userName = uiState.userName,
                        dateFormatted = uiState.currentDateFormatted,
                        onOpenAiCoach = onNavigateToAiCoach
                    )
                }

                // 2. Calorie Summary Card with Progress Ring
                item {
                    HomeCalorieSummaryCard(
                        consumedCalories = uiState.consumedCalories,
                        targetCalories = uiState.targetCalories,
                        remainingCalories = uiState.remainingCalories,
                        progressFraction = uiState.calorieProgressFraction
                    )
                }

                // 3. Macro Breakdown Card
                item {
                    HomeMacroSummaryCard(
                        protein = uiState.protein,
                        carbs = uiState.carbs,
                        fat = uiState.fat
                    )
                }

                // 4. Daily Hydration Card
                item {
                    HomeWaterCard(
                        water = uiState.water,
                        onLogWater = { amount -> viewModel.logWater(amount) },
                        onUndoWater = { viewModel.undoLatestWaterLog() }
                    )
                }

                // 5. Activity & Steps Card (Health Connect)
                item {
                    HomeActivityCard(
                        activity = uiState.activity,
                        onRequestPermission = {
                            permissionLauncher.launch(viewModel.healthConnectManager.requiredPermissions)
                        }
                    )
                }

                // 6. Today's Meals Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Meals",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                        )
                        Text(
                            text = "${uiState.consumedCalories} kcal logged",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LuminaOnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // 5. Empty State or Grouped Meals List
                if (!uiState.hasLoggedMealsToday) {
                    item {
                        HomeEmptyMealsCard(onLogFirstMeal = onNavigateToAddFood)
                    }
                } else {
                    items(uiState.mealsGrouped.size) { index ->
                        val category = uiState.mealsGrouped[index]
                        HomeMealCategoryCard(
                            category = category,
                            onMealClick = { mealId -> onNavigateToMealDetail(mealId) },
                            onAddMealClick = onNavigateToAddFood
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    dateFormatted: String,
    onOpenAiCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back 👋",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface,
                    fontSize = 22.sp
                )
            )
            Text(
                text = "$userName  •  $dateFormatted",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                )
            )
        }

        // AI Coach Button
        Surface(
            onClick = onOpenAiCoach,
            shape = RoundedCornerShape(16.dp),
            color = LuminaPrimary.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI Coach",
                    tint = LuminaPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeCalorieSummaryCard(
    consumedCalories: Int,
    targetCalories: Int,
    remainingCalories: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Progress Ring
            LuminaProgressRing(
                calories = consumedCalories,
                label = "consumed",
                progress = progressFraction,
                size = 140.dp,
                strokeWidth = 12.dp
            )

            // Right: Breakdown numbers
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                CalorieMetricRow(
                    label = "Target Goal",
                    value = "$targetCalories kcal",
                    color = LuminaOnSurface
                )
                CalorieMetricRow(
                    label = "Remaining",
                    value = "$remainingCalories kcal",
                    color = if (remainingCalories >= 0) LuminaPrimary else Color(0xFFE53935),
                    isHighlight = true
                )
                CalorieMetricRow(
                    label = "Progress",
                    value = "${(progressFraction * 100).toInt()}%",
                    color = LuminaOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalorieMetricRow(
    label: String,
    value: String,
    color: Color,
    isHighlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = LuminaOnSurfaceVariant,
                fontSize = 12.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = color,
                fontSize = if (isHighlight) 18.sp else 16.sp
            )
        )
    }
}

@Composable
private fun HomeMacroSummaryCard(
    protein: MacroProgress,
    carbs: MacroProgress,
    fat: MacroProgress,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MacroColumnItem(
                name = "Protein",
                currentGrams = protein.currentGrams.toInt(),
                targetGrams = protein.targetGrams.toInt(),
                progress = protein.progressFraction,
                color = Color(0xFF646FD4) // Periwinkle
            )
            MacroColumnItem(
                name = "Carbs",
                currentGrams = carbs.currentGrams.toInt(),
                targetGrams = carbs.targetGrams.toInt(),
                progress = carbs.progressFraction,
                color = Color(0xFFF59E0B) // Amber
            )
            MacroColumnItem(
                name = "Fat",
                currentGrams = fat.currentGrams.toInt(),
                targetGrams = fat.targetGrams.toInt(),
                progress = fat.progressFraction,
                color = Color(0xFF10B981) // Emerald
            )
        }
    }
}

@Composable
private fun MacroColumnItem(
    name: String,
    currentGrams: Int,
    targetGrams: Int,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LuminaOnSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${currentGrams}g / ${targetGrams}g",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = LuminaOnSurface,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun HomeMealCategoryCard(
    category: MealCategorySummary,
    onMealClick: (Long) -> Unit,
    onAddMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealTitle = category.mealType.name.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LuminaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = LuminaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = mealTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                        )
                        Text(
                            text = if (category.itemCount > 0) "${category.itemCount} items logged" else "Not logged yet",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LuminaOnSurfaceVariant
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category.totalCalories.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (category.totalCalories > 0) LuminaPrimary else LuminaOnSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onAddMealClick,
                        shape = CircleShape,
                        color = LuminaSurfaceContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add food",
                                tint = LuminaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // If meals logged, show item summary preview
            if (category.meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    category.meals.forEach { mealWithItems ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMealClick(mealWithItems.meal.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = LuminaSurfaceContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val foodNames = mealWithItems.items.joinToString(", ") { it.name }.ifBlank { "Logged meal" }
                                Text(
                                    text = foodNames,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LuminaOnSurface,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${mealWithItems.meal.totalCalories.toInt()} kcal",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LuminaPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "View Details",
                                        tint = LuminaOnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyMealsCard(
    onLogFirstMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🥗",
                fontSize = 44.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No meals logged yet today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Track your breakfast, lunch, or a snack to see your progress update live.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = onLogFirstMeal,
                shape = RoundedCornerShape(16.dp),
                color = LuminaPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Your First Meal ✨",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeWaterCard(
    water: WaterProgress,
    onLogWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💧", fontSize = 18.sp)
                    Text(
                        text = "Daily Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                }

                val currentGlasses = kotlin.math.round((water.consumedMl / 250.0)).toInt()
                Text(
                    text = "${water.consumedMl} / ${water.targetMl} ml ($currentGlasses/${water.targetGlasses} glasses)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            LinearProgressIndicator(
                progress = { water.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00B4D8),
                trackColor = Color(0xFF00B4D8).copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { onLogWater(250) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00B4D8).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "+250 ml (1 glass)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF0077B6),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        onClick = { onLogWater(500) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00B4D8).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "+500 ml",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF0077B6),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                if (water.consumedMl > 0 && water.latestLogId != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onUndoWater,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Undo",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = LuminaOnSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActivityCard(
    activity: ActivityProgress,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏃", fontSize = 16.sp)
                    }
                }
                Column {
                    Text(
                        text = "Steps & Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                    Text(
                        text = activity.statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LuminaOnSurfaceVariant
                        )
                    )
                }
            }

            if (activity.isAvailable && activity.stepsToday == 0L && activity.statusMessage.contains("connect", ignoreCase = true)) {
                Surface(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LuminaPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else if (!activity.isAvailable) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceContainer
                ) {
                    Text(
                        text = "Read-Only",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LuminaOnSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
