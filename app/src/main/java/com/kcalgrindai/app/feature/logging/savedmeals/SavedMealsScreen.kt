package com.kcalgrindai.app.feature.logging.savedmeals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.kcalgrindai.app.core.designsystem.KcalGrindOverBudgetRed
import com.kcalgrindai.app.core.designsystem.KcalGrindProtein
import com.kcalgrindai.app.core.designsystem.KcalGrindCarbs
import com.kcalgrindai.app.core.designsystem.KcalGrindFat
import com.kcalgrindai.app.core.designsystem.components.CircleIconButton
import com.kcalgrindai.app.core.designsystem.components.FoodImageBadge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalgrindai.app.core.designsystem.components.KcalGrindPrimaryButton
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SavedMealWithItems
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun SavedMealsScreen(
    onNavigateBack: () -> Unit,
    onMealLogged: () -> Unit,
    viewModel: SavedMealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val v3Colors = MaterialTheme.v3Colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Rename Dialog
    uiState.renamingMeal?.let { meal ->
        var renameText by remember(meal.savedMeal.id) { mutableStateOf(meal.savedMeal.name) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissRename() },
            title = { Text("Rename Saved Meal", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Meal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmRename(renameText) },
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = v3Colors.emeraldAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRename() }) {
                    Text("Cancel", color = v3Colors.textSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    uiState.deletingMeal?.let { meal ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Saved Meal?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete \"${meal.savedMeal.name}\"? Past logs in your food diary will remain unaffected.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = KcalGrindOverBudgetRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) {
                    Text("Cancel", color = v3Colors.textSecondary)
                }
            }
        )
    }

    // Log Meal Dialog (choose slot and date)
    uiState.loggingMeal?.let { meal ->
        var selectedSlot by remember(meal.savedMeal.id) { mutableStateOf(meal.savedMeal.defaultMealType) }
        val today = remember { LocalDate.now().toString() }

        AlertDialog(
            onDismissRequest = { viewModel.dismissLogMeal() },
            title = { Text("Log to Diary", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Log \"${meal.savedMeal.name}\" (${meal.totalCalories.roundToInt()} kcal):",
                        style = MaterialTheme.typography.bodyMedium.copy(color = v3Colors.textSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Meal Slot",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = v3Colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MealType.entries.forEach { slot ->
                            FilterChip(
                                selected = selectedSlot == slot,
                                onClick = { selectedSlot = slot },
                                label = {
                                    Text(
                                        slot.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = v3Colors.emeraldAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = v3Colors.emeraldAccent
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmLogMeal(
                            targetDate = today,
                            targetSlot = selectedSlot,
                            onLogged = onMealLogged
                        )
                    }
                ) {
                    Text("Log Now", fontWeight = FontWeight.Bold, color = v3Colors.emeraldAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogMeal() }) {
                    Text("Cancel", color = v3Colors.textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = v3Colors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = v3Colors.surface,
                border = BorderStroke(1.dp, v3Colors.subtleBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        onClick = onNavigateBack,
                        visualSize = 38.dp,
                        containerColor = v3Colors.surfaceElevated,
                        contentColor = v3Colors.textPrimary,
                        contentDescription = "Back"
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = v3Colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Saved Meals",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = v3Colors.textPrimary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Quick combos & frequent favorites",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3Colors.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = v3Colors.emeraldAccent)
            }
        } else if (uiState.savedMeals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = v3Colors.surfaceElevated,
                        border = BorderStroke(1.dp, v3Colors.subtleBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = v3Colors.emeraldAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "No Saved Meals Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3Colors.textPrimary,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "Save your favorite meals and combos directly from the Food Diary. Once saved, you can log them here with a single tap.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = v3Colors.textSecondary,
                            lineHeight = 22.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.savedMeals, key = { it.savedMeal.id }) { meal ->
                    SavedMealCard(
                        meal = meal,
                        onLogClick = { viewModel.promptLogMeal(meal) },
                        onRenameClick = { viewModel.promptRename(meal) },
                        onDeleteClick = { viewModel.promptDelete(meal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedMealCard(
    meal: SavedMealWithItems,
    onLogClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3Colors = MaterialTheme.v3Colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = v3Colors.surfaceElevated,
        border = BorderStroke(1.dp, v3Colors.subtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Food Badge + Name + Slot + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodImageBadge(
                        foodName = meal.savedMeal.name,
                        size = 42.dp,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meal.savedMeal.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = v3Colors.textPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = v3Colors.emeraldAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = meal.savedMeal.defaultMealType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = v3Colors.emeraldAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${meal.items.size} items",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = v3Colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRenameClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = v3Colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = KcalGrindOverBudgetRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nutrition Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${meal.totalCalories.roundToInt()} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3Colors.emeraldAccent,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall.copy(color = v3Colors.textMuted)
                )

                Text(
                    text = "P: ${meal.totalProteinG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KcalGrindProtein,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = "C: ${meal.totalCarbsG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KcalGrindCarbs,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = "F: ${meal.totalFatG.roundToInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KcalGrindFat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            // Food items preview
            if (meal.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    meal.items.take(4).forEach { item ->
                        Text(
                            text = "• ${item.name} (${item.servingDescription})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3Colors.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                    if (meal.items.size > 4) {
                        Text(
                            text = "+ ${meal.items.size - 4} more items",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3Colors.textMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log Button
            KcalGrindPrimaryButton(
                text = "Log Meal",
                onClick = onLogClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
