package com.kcalgrindai.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalgrindai.app.feature.home.MealCategorySummary
import com.kcalgrindai.app.domain.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Continuous native vertical timeline for Today's Meals.
 * Uses Compose Intrinsic measurements so the connecting vertical track dynamically
 * scales to the exact height of each meal card with zero gaps, broken segments, or hardcoded heights.
 */
@Composable
fun HomeMealsTimeline(
    meals: List<MealCategorySummary>,
    onMealClick: (Long) -> Unit,
    onAddMealClick: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        meals.forEachIndexed { index, category ->
            HomeMealTimelineRow(
                category = category,
                isFirst = index == 0,
                isLast = index == meals.size - 1,
                onMealClick = onMealClick,
                onAddMealClick = { onAddMealClick(category.mealType) }
            )
        }
    }
}

@Composable
private fun HomeMealTimelineRow(
    category: MealCategorySummary,
    isFirst: Boolean,
    isLast: Boolean,
    onMealClick: (Long) -> Unit,
    onAddMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealTitle = category.mealType.label
    val icon: ImageVector = when (category.mealType) {
        MealType.BREAKFAST -> Icons.Default.WbSunny
        MealType.LUNCH -> Icons.Default.Restaurant
        MealType.DINNER -> Icons.Default.DinnerDining
        MealType.SNACK -> Icons.Default.LocalCafe
    }

    val firstMeal = category.meals.firstOrNull()
    val timeFormatted = remember(firstMeal) {
        if (firstMeal != null && firstMeal.meal.loggedAt > 0L) {
            try {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(firstMeal.meal.loggedAt))
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val statusText = when {
        timeFormatted != null -> "$mealTitle • $timeFormatted"
        category.itemCount > 0 -> "$mealTitle • ${category.itemCount} items"
        else -> "$mealTitle • Not logged"
    }

    val foodNames = remember(category.meals) {
        val names = category.meals.flatMap { it.items }.joinToString(", ") { it.name }.trim()
        if (names.isNotBlank()) names else "Tap + to log ${mealTitle.lowercase()}"
    }

    val hasMeals = category.meals.isNotEmpty()
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val activeNodeColor = MaterialTheme.colorScheme.primary
    val inactiveNodeColor = MaterialTheme.colorScheme.outlineVariant

    // IntrinsicSize.Min ensures the left timeline canvas fills the dynamic height of the meal card on the right
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // Continuous Left Timeline Track
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineX = size.width / 2f
                val nodeCenterY = 36.dp.toPx() // Centers with the meal icon inside the card
                val strokeWidthPx = 2.dp.toPx()

                // 1. Vertical timeline line connecting dynamically
                val startY = if (isFirst) nodeCenterY else 0f
                val endY = if (isLast) nodeCenterY else size.height

                drawLine(
                    color = outlineVariantColor,
                    start = Offset(lineX, startY),
                    end = Offset(lineX, endY),
                    strokeWidth = strokeWidthPx
                )
            }

            // Timeline Node Circle (Hollow with theme background fill)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    2.dp,
                    if (hasMeals) activeNodeColor else inactiveNodeColor
                ),
                modifier = Modifier
                    .padding(top = 28.dp)
                    .size(16.dp)
            ) {
                if (hasMeals) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = activeNodeColor,
                            modifier = Modifier.size(6.dp)
                        ) {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Meal Card with bottom padding to visually separate cards while maintaining continuous line
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasMeals) Modifier.clickable { onMealClick(firstMeal!!.meal.id) }
                        else Modifier
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left native meal icon container
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mealTitle,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Middle Text Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = foodNames,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Side: Calorie Count & Add/Edit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${category.totalCalories.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasMeals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "kcal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Circular + Button (Minimum 40dp touch target)
                        Surface(
                            onClick = onAddMealClick,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add to $mealTitle",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
