package com.kcalgrindai.app.feature.diary

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalgrindai.app.R
import com.kcalgrindai.app.core.designsystem.components.CircleIconButton
import com.kcalgrindai.app.core.designsystem.components.FoodImageBadge
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.core.designsystem.KcalGrindProtein
import com.kcalgrindai.app.core.designsystem.KcalGrindCarbs
import com.kcalgrindai.app.core.designsystem.KcalGrindFat
import com.kcalgrindai.app.core.designsystem.KcalGrindOverBudgetLightBg
import com.kcalgrindai.app.core.designsystem.KcalGrindOverBudgetLightBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindOverBudgetDarkText
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceVerified
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceVerifiedBg
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceManual
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceManualBg
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceCustom
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceCustomBg
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceRecipe
import com.kcalgrindai.app.core.designsystem.KcalGrindSourceRecipeBg
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.feature.home.MealCategorySummary
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToMealDetail: (Long) -> Unit,
    initialMealSlot: String? = null,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val v3Colors = MaterialTheme.v3Colors
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(initialMealSlot, uiState.mealsGrouped) {
        if (!initialMealSlot.isNullOrBlank() && uiState.mealsGrouped.isNotEmpty()) {
            val normalizedSlot = initialMealSlot.trim().lowercase().let {
                if (it == "snacks") "snack" else it
            }
            val targetIndex = uiState.mealsGrouped.indexOfFirst {
                it.mealType.name.equals(normalizedSlot, ignoreCase = true) ||
                it.mealType.id.equals(normalizedSlot, ignoreCase = true)
            }
            if (targetIndex >= 0) {
                // item 0 is Daily Totals Summary Card; meal section cards start at index 1
                listState.animateScrollToItem(targetIndex + 1)
            }
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = v3Colors.background
    ) { paddingValues ->
        val onAddFoodScoped: (MealType?) -> Unit = { mealType ->
            viewModel.prepareAddFood(mealType)
            onNavigateToAddFood()
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = v3Colors.emeraldAccent)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
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
                            onLogMeal = { onAddFoodScoped(null) }
                        )
                    }
                } else {
                    items(uiState.mealsGrouped.size) { index ->
                        val category = uiState.mealsGrouped[index]
                        DiaryMealSectionCard(
                            category = category,
                            onMealClick = { mealId -> onNavigateToMealDetail(mealId) },
                            onAddMealClick = { onAddFoodScoped(category.mealType) },
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
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = v3Colors.surface,
        border = BorderStroke(1.dp, v3Colors.subtleBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                onClick = onPreviousDay,
                visualSize = 38.dp,
                containerColor = v3Colors.surfaceElevated,
                contentColor = v3Colors.textPrimary,
                contentDescription = "Previous Day"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Day",
                    tint = v3Colors.textPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = v3Colors.surfaceElevated,
                border = BorderStroke(1.dp, v3Colors.subtleBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onResetToToday() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(v3Colors.emeraldAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3Colors.textPrimary,
                            fontSize = 15.sp
                        )
                    )
                    if (!isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Jump to Today",
                            tint = v3Colors.emeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            CircleIconButton(
                onClick = onNextDay,
                visualSize = 38.dp,
                containerColor = v3Colors.surfaceElevated,
                contentColor = v3Colors.textPrimary,
                contentDescription = "Next Day"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Day",
                    tint = v3Colors.textPrimary
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
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = v3Colors.surface,
        border = BorderStroke(1.dp, v3Colors.subtleBorder)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(v3Colors.emeraldAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAILY ENERGY QUOTA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3Colors.textMuted,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCalories / $targetCalories kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3Colors.textPrimary,
                            fontSize = 22.sp
                        )
                    )
                }

                val remaining = targetCalories - totalCalories
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remaining >= 0) v3Colors.emeraldAccent.copy(alpha = 0.15f) else KcalGrindOverBudgetLightBg,
                    border = BorderStroke(
                        1.dp,
                        if (remaining >= 0) v3Colors.emeraldAccent.copy(alpha = 0.35f) else KcalGrindOverBudgetLightBorder
                    )
                ) {
                    Text(
                        text = if (remaining >= 0) "$remaining kcal left" else "${-remaining} kcal over",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (remaining >= 0) v3Colors.emeraldAccent else KcalGrindOverBudgetDarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiaryMacroPill(
                    label = "Protein",
                    amount = "${proteinG.roundToInt()}g",
                    accentColor = KcalGrindProtein,
                    modifier = Modifier.weight(1f)
                )
                DiaryMacroPill(
                    label = "Carbs",
                    amount = "${carbsG.roundToInt()}g",
                    accentColor = KcalGrindCarbs,
                    modifier = Modifier.weight(1f)
                )
                DiaryMacroPill(
                    label = "Fat",
                    amount = "${fatG.roundToInt()}g",
                    accentColor = KcalGrindFat,
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
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = v3Colors.surfaceElevated,
        border = BorderStroke(1.dp, v3Colors.subtleBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = v3Colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3Colors.textPrimary,
                    fontSize = 15.sp
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
    val v3Colors = MaterialTheme.v3Colors
    val mealTitle = category.mealType.name.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = v3Colors.surface,
        border = BorderStroke(1.dp, v3Colors.subtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(v3Colors.emeraldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = v3Colors.emeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = mealTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3Colors.textPrimary,
                            fontSize = 17.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${category.totalCalories.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (category.totalCalories > 0) v3Colors.emeraldAccent else v3Colors.textSecondary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CircleIconButton(
                        onClick = onAddMealClick,
                        visualSize = 36.dp,
                        containerColor = v3Colors.surfaceElevated,
                        contentColor = v3Colors.emeraldAccent,
                        border = BorderStroke(1.dp, v3Colors.subtleBorder),
                        contentDescription = "Add food to $mealTitle"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add food to $mealTitle",
                            tint = v3Colors.emeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Items List
            if (category.meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
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
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = v3Colors.surfaceElevated,
        border = BorderStroke(1.dp, v3Colors.subtleBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodImageBadge(
                foodName = item.name,
                foodId = item.id,
                size = 38.dp,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = v3Colors.textPrimary
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SourceBadge(source = item.source)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.servingGrams.toInt()}g  •  P: ${item.proteinG.roundToInt()}g  C: ${item.carbsG.roundToInt()}g  F: ${item.fatG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3Colors.textSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3Colors.emeraldAccent
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
                        tint = v3Colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceBadge(source: ItemSource) {
    val v3Colors = MaterialTheme.v3Colors
    val (label, bg, fg) = when (source) {
        ItemSource.AI -> Triple("AI", v3Colors.emeraldAccent.copy(alpha = 0.18f), v3Colors.emeraldAccent)
        ItemSource.VERIFIED -> Triple("Barcode", KcalGrindSourceVerifiedBg, KcalGrindSourceVerified)
        ItemSource.MANUAL -> Triple("Manual", KcalGrindSourceManualBg, KcalGrindSourceManual)
        ItemSource.USER_ENTERED -> Triple("Custom", KcalGrindSourceCustomBg, KcalGrindSourceCustom)
        ItemSource.RECIPE, ItemSource.CURATED_RECIPE -> Triple("Recipe", KcalGrindSourceRecipeBg, KcalGrindSourceRecipe)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
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
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = v3Colors.surface,
        border = BorderStroke(1.dp, v3Colors.subtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(v3Colors.emeraldAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_sproutling_active),
                    contentDescription = "Sproutling waiting for fuel",
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isToday) "No meals logged today" else "No meals logged for this date",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3Colors.textPrimary,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Keep Sproutling energized! Tap below to track your fuel and hit today's targets.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = v3Colors.textSecondary,
                    fontSize = 13.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                onClick = onLogMeal,
                shape = RoundedCornerShape(16.dp),
                color = v3Colors.emeraldAccent
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (v3Colors.isDark) v3Colors.background else v3Colors.surface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Meal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (v3Colors.isDark) v3Colors.background else v3Colors.surface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
