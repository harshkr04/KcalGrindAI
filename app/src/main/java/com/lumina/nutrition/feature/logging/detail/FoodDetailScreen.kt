package com.lumina.nutrition.feature.logging.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondaryContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLow
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.domain.model.FoodSource
import com.lumina.nutrition.domain.model.MealType

@Composable
fun FoodDetailScreen(
    onNavigateBack: () -> Unit,
    onFoodLogged: () -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val food = uiState.food

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateBack),
                    shape = CircleShape,
                    color = LuminaSurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Food Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LuminaSurfaceContainerLowest,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Button(
                        onClick = { viewModel.logFood(onFoodLogged) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = food != null && !uiState.isLogging,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuminaPrimary,
                            contentColor = LuminaOnPrimary
                        )
                    ) {
                        if (uiState.isLogging) {
                            CircularProgressIndicator(
                                color = LuminaOnPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Log ${uiState.scaledCalories} kcal to ${uiState.selectedMealType.label}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (food == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No food selected.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = LuminaOnSurfaceVariant)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LuminaBackground)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Source Badge
                val sourceLabel = when (food.source) {
                    FoodSource.USDA -> "USDA FoodData Central"
                    FoodSource.OPEN_FOOD_FACTS -> "Open Food Facts"
                    FoodSource.CUSTOM -> "Custom Food"
                    else -> "Verified Food"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LuminaSecondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "✓ $sourceLabel",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LuminaPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Food Title
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface,
                        fontSize = 24.sp
                    )
                )

                if (!food.brand.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Brand: ${food.brand}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LuminaOnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Calorie Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LuminaSurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.scaledCalories}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaPrimary,
                                fontSize = 48.sp
                            )
                        )
                        Text(
                            text = "TOTAL CALORIES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = LuminaOnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Serving: ${uiState.scaledGrams}g (${food.servingDescription})",
                            style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macro breakdown grid (3 cards)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NutrientCard(
                        label = "Protein",
                        value = "${uiState.scaledProtein}g",
                        color = LuminaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientCard(
                        label = "Carbs",
                        value = "${uiState.scaledCarbs}g",
                        color = LuminaOnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientCard(
                        label = "Fat",
                        value = "${uiState.scaledFat}g",
                        color = LuminaOnSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fiber pill
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LuminaSurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dietary Fiber",
                            style = MaterialTheme.typography.bodyMedium.copy(color = LuminaOnSurface)
                        )
                        Text(
                            text = "${uiState.scaledFiber}g",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Serving Multiplier Section
                Text(
                    text = "Serving Size / Quantity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { mult ->
                        val isSelected = uiState.servingsMultiplier == mult
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { viewModel.setServings(mult) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LuminaPrimary else LuminaSurfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${mult}x",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) LuminaOnPrimary else LuminaOnSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Meal Type Selection
                Text(
                    text = "Log to Meal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { meal ->
                        FilterChip(
                            selected = uiState.selectedMealType == meal,
                            onClick = { viewModel.setMealType(meal) },
                            label = { Text(meal.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuminaSecondaryContainer,
                                selectedLabelColor = LuminaPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
fun NutrientCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = LuminaOnSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 18.sp
                )
            )
        }
    }
}
