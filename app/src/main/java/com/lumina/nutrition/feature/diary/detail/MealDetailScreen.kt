package com.lumina.nutrition.feature.diary.detail

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton
import com.lumina.nutrition.core.designsystem.components.LuminaSecondaryButton
import com.lumina.nutrition.domain.model.FoodLogItem
import kotlin.math.roundToInt

@Composable
fun MealDetailScreen(
    mealId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onDuplicateSuccess: () -> Unit,
    viewModel: MealDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mealId) {
        viewModel.loadMeal(mealId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove this meal and all its logged food items.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMeal(onDeleted = onNavigateBack)
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = LuminaOnSurface)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            val mealName = uiState.mealWithItems?.meal?.mealType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Meal"
            MealDetailTopBar(
                title = "$mealName Details",
                onBackClick = onNavigateBack
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
        } else if (uiState.mealWithItems == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Meal not found",
                    style = MaterialTheme.typography.bodyLarge.copy(color = LuminaOnSurfaceVariant)
                )
            }
        } else {
            val mealWithItems = uiState.mealWithItems!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Meal Summary Card
                item {
                    MealDetailSummaryCard(
                        totalCalories = uiState.totalCalories.toInt(),
                        proteinG = uiState.totalProteinG,
                        carbsG = uiState.totalCarbsG,
                        fatG = uiState.totalFatG,
                        formattedDate = uiState.formattedDate,
                        sourceLabel = uiState.sourceLabel
                    )
                }

                // 2. Items Section Header
                item {
                    Text(
                        text = "Logged Items (${mealWithItems.items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                }

                // 3. Itemized List
                items(mealWithItems.items, key = { it.id }) { item ->
                    MealDetailItemCard(
                        item = item,
                        onDeleteItem = { viewModel.deleteFoodItem(item) }
                    )
                }

                // 4. Action Buttons
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LuminaPrimaryButton(
                            text = "Duplicate to Today 📋",
                            onClick = {
                                viewModel.duplicateMealToToday {
                                    onDuplicateSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        LuminaSecondaryButton(
                            text = "Add Food to this Meal ➕",
                            onClick = onNavigateToAddFood,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Meal",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = LuminaSurface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LuminaOnSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface,
                    fontSize = 20.sp
                )
            )
        }
    }
}

@Composable
private fun MealDetailSummaryCard(
    totalCalories: Int,
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
    formattedDate: String,
    sourceLabel: String,
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LuminaOnSurfaceVariant
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LuminaPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = sourceLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro summary chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPill(label = "Protein", amount = "${proteinG.roundToInt()}g", color = Color(0xFF646FD4), modifier = Modifier.weight(1f))
                MacroPill(label = "Carbs", amount = "${carbsG.roundToInt()}g", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                MacroPill(label = "Fat", amount = "${fatG.roundToInt()}g", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroPill(
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
private fun MealDetailItemCard(
    item: FoodLogItem,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.servingGrams.toInt()}g  •  P: ${item.proteinG.roundToInt()}g  C: ${item.carbsG.roundToInt()}g  F: ${item.fatG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant
                    )
                )
                if (item.confidence != null && item.confidence > 0.0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Confidence: ${(item.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LuminaPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.calories.toInt()} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteItem,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = LuminaOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
