package com.lumina.nutrition.feature.diary

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.navigation.LuminaBottomNavigationBar
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.feature.home.MealCategorySummary
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToMealDetail: (Long) -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.undoMessage) {
        val msg = uiState.undoMessage
        if (msg != null) {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    Scaffold(
        topBar = {
            DiaryDateHeader(
                formattedDate = uiState.selectedDateFormatted,
                isToday = uiState.isToday,
                onPreviousDay = { viewModel.previousDay() },
                onNextDay = { viewModel.nextDay() },
                onResetToToday = { viewModel.resetToToday() }
            )
        },
        bottomBar = {
            LuminaBottomNavigationBar(
                currentRoute = LuminaRoute.Diary.route,
                onNavigateToTab = onNavigateToTab,
                onOpenAddFood = onNavigateToAddFood
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Daily Totals Summary Banner
                item {
                    DiaryDailySummaryCard(
                        totalCalories = uiState.totalCalories,
                        targetCalories = uiState.targetCalories,
                        proteinG = uiState.totalProteinG,
                        carbsG = uiState.totalCarbsG,
                        fatG = uiState.totalFatG
                    )
                }

                // 2. Meal Categories or Empty State
                if (!uiState.hasMeals) {
                    item {
                        DiaryEmptyDayCard(
                            isToday = uiState.isToday,
                            onLogMeal = onNavigateToAddFood
                        )
                    }
                } else {
                    items(uiState.mealsGrouped.size) { index ->
                        val category = uiState.mealsGrouped[index]
                        DiaryMealSectionCard(
                            category = category,
                            onMealClick = { mealId -> onNavigateToMealDetail(mealId) },
                            onAddMealClick = onNavigateToAddFood,
                            onDeleteItem = { item -> viewModel.deleteFoodItem(item) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DiaryDateHeader(
    formattedDate: String,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = LuminaSurface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Day",
                    tint = LuminaOnSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onResetToToday() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface,
                        fontSize = 17.sp
                    )
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Jump to Today",
                        tint = LuminaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Day",
                    tint = LuminaOnSurface
                )
            }
        }
    }
}

@Composable
private fun DiaryDailySummaryCard(
    totalCalories: Int,
    targetCalories: Int,
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
    modifier: Modifier = Modifier
) {
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Total",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LuminaOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "$totalCalories / $targetCalories kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                }

                val remaining = targetCalories - totalCalories
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remaining >= 0) LuminaPrimary.copy(alpha = 0.12f) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (remaining >= 0) "$remaining kcal left" else "${-remaining} kcal over",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (remaining >= 0) LuminaPrimary else Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Macro Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiaryMacroPill(
                    label = "Protein",
                    amount = "${proteinG.roundToInt()}g",
                    color = Color(0xFF646FD4),
                    modifier = Modifier.weight(1f)
                )
                DiaryMacroPill(
                    label = "Carbs",
                    amount = "${carbsG.roundToInt()}g",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                DiaryMacroPill(
                    label = "Fat",
                    amount = "${fatG.roundToInt()}g",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DiaryMacroPill(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = LuminaOnSurfaceVariant,
                    fontSize = 11.sp
                )
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun DiaryMealSectionCard(
    category: MealCategorySummary,
    onMealClick: (Long) -> Unit,
    onAddMealClick: () -> Unit,
    onDeleteItem: (FoodLogItem) -> Unit,
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
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(LuminaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = LuminaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = mealTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
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
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add food",
                                tint = LuminaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Items List
            if (category.meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    category.meals.forEach { mealWithItems ->
                        mealWithItems.items.forEach { item ->
                            DiaryFoodItemRow(
                                item = item,
                                onClick = { onMealClick(mealWithItems.meal.id) },
                                onDelete = { onDeleteItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryFoodItemRow(
    item: FoodLogItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LuminaOnSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SourceBadge(source = item.source)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.servingGrams.toInt()}g  •  P: ${item.proteinG.roundToInt()}g  C: ${item.carbsG.roundToInt()}g  F: ${item.fatG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaPrimary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = LuminaOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceBadge(source: ItemSource) {
    val (label, bg, fg) = when (source) {
        ItemSource.AI -> Triple("AI", Color(0xFFEDE7F6), Color(0xFF673AB7))
        ItemSource.VERIFIED -> Triple("Barcode", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        ItemSource.MANUAL -> Triple("Manual", Color(0xFFE3F2FD), Color(0xFF1976D2))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = fg
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun DiaryEmptyDayCard(
    isToday: Boolean,
    onLogMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                text = if (isToday) "🥑" else "📅",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isToday) "No meals logged today" else "No meals logged for this date",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Use the button below or tap '+' to record your food.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                onClick = onLogMeal,
                shape = RoundedCornerShape(14.dp),
                color = LuminaPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Food",
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
