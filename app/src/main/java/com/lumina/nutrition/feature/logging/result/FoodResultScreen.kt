package com.lumina.nutrition.feature.logging.result

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.domain.model.AIFoodItem
import com.lumina.nutrition.domain.model.ConfidenceLevel
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager

@Composable
fun FoodResultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMealConfirm: () -> Unit,
    sessionManager: LoggingSessionManager
) {
    val draftState by sessionManager.state.value.let { remember { mutableFloatStateOf(0f) } }.let { sessionManager.state.collectAsState() }
    val food = draftState.candidateFoods.firstOrNull() ?: AIFoodItem(
        name = "Grilled Chicken Breast",
        estimatedGrams = 150.0,
        calories = 248,
        macros = com.lumina.nutrition.domain.model.AIMacros(46.5, 0.0, 5.4),
        confidence = 0.92
    )

    var portionRatio by remember { mutableFloatStateOf(1.0f) }
    val currentGrams = (food.estimatedGrams * portionRatio).toInt()
    val currentCalories = (food.calories * portionRatio).toInt()
    val currentProtein = (food.macros.protein * portionRatio).toInt()
    val currentCarbs = (food.macros.carbs * portionRatio).toInt()
    val currentFat = (food.macros.fat * portionRatio).toInt()

    val (badgeBg, badgeText, badgeLabel) = when (food.confidenceLevel) {
        ConfidenceLevel.HIGH -> Triple(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981), "✓ High Confidence (${(food.confidence * 100).toInt()}%)")
        ConfidenceLevel.MEDIUM -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "⚠️ Moderate Confidence (${(food.confidence * 100).toInt()}%) - Please Confirm")
        ConfidenceLevel.LOW -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444), "❓ Low Confidence (${(food.confidence * 100).toInt()}%)")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminaBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Bar
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

            Text(
                text = "AI Nutrient Breakdown",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Food Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = LuminaSurfaceContainerLowest
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Confidence Pill Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeLabel,
                        color = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$currentCalories kcal  •  ${currentGrams}g portion",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LuminaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nutrient Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutrientPill("Protein", "${currentProtein}g", LuminaPrimary)
                    NutrientPill("Carbs", "${currentCarbs}g", Color(0xFF60A5FA))
                    NutrientPill("Fat", "${currentFat}g", Color(0xFFFBBF24))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Portion Slider Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = LuminaSurfaceContainerLowest
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adjust Portion",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                    Text(
                        text = "${currentGrams}g",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = portionRatio,
                    onValueChange = { portionRatio = it },
                    valueRange = 0.25f..3.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = LuminaPrimary,
                        activeTrackColor = LuminaPrimary,
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0.25x", color = LuminaOnSurfaceVariant, fontSize = 12.sp)
                    Text("1.0x (Normal)", color = LuminaOnSurfaceVariant, fontSize = 12.sp)
                    Text("3.0x", color = LuminaOnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Log Food CTA Button
        Button(
            onClick = {
                // Update session food with adjusted portion
                sessionManager.updateCandidateFood(
                    0,
                    food.copy(
                        estimatedGrams = currentGrams.toDouble(),
                        calories = currentCalories,
                        macros = com.lumina.nutrition.domain.model.AIMacros(
                            protein = currentProtein.toDouble(),
                            carbs = currentCarbs.toDouble(),
                            fat = currentFat.toDouble()
                        )
                    )
                )
                onNavigateToMealConfirm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
        ) {
            Text("Confirm & Log Food ✨", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun NutrientPill(name: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.width(95.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, fontSize = 12.sp, color = LuminaOnSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
