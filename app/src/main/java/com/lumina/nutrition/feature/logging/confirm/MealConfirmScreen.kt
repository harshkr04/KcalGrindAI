package com.lumina.nutrition.feature.logging.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.MealType

@Composable
fun MealConfirmScreen(
    onNavigateBack: () -> Unit,
    onMealLogged: () -> Unit,
    viewModel: MealConfirmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalProtein = uiState.items.sumOf { it.proteinG }.toInt()
    val totalCarbs = uiState.items.sumOf { it.carbsG }.toInt()
    val totalFat = uiState.items.sumOf { it.fatG }.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminaBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateBack),
                shape = CircleShape,
                color = LuminaSurfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Confirm Meal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Source: ${uiState.loggingSource}",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Meal Type Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK).forEach { type ->
                val isSelected = uiState.mealType == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setMealType(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) LuminaPrimary else LuminaSurfaceContainerHigh
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) LuminaOnPrimary else LuminaOnSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(uiState.items) { index, item ->
                ConfirmFoodItemCard(
                    item = item,
                    onDelete = { viewModel.removeItem(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Macro Totals Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LuminaSurfaceContainerLowest
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Calories", color = LuminaOnSurfaceVariant, fontSize = 12.sp)
                    Text(
                        text = "${uiState.totalCalories} kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroConfirmPill("P", "${totalProtein}g", LuminaPrimary)
                    MacroConfirmPill("C", "${totalCarbs}g", Color(0xFF60A5FA))
                    MacroConfirmPill("F", "${totalFat}g", Color(0xFFFBBF24))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log Meal Button
        Button(
            onClick = { viewModel.logMeal(onMealLogged) },
            enabled = uiState.items.isNotEmpty() && !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color = LuminaOnPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving to Diary...", color = LuminaOnPrimary, fontWeight = FontWeight.Bold)
            } else {
                Text("Log Meal to Diary ✨", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ConfirmFoodItemCard(
    item: FoodLogItem,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = LuminaSurfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.calories.toInt()} kcal  •  ${item.servingGrams.toInt()}g  (P: ${item.proteinG.toInt()}g, C: ${item.carbsG.toInt()}g, F: ${item.fatG.toInt()}g)",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                )
            }

            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete),
                shape = CircleShape,
                color = Color.Gray.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "✕", fontSize = 14.sp, color = LuminaOnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MacroConfirmPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, color = color, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontWeight = FontWeight.SemiBold, color = LuminaOnSurface, fontSize = 11.sp)
        }
    }
}
