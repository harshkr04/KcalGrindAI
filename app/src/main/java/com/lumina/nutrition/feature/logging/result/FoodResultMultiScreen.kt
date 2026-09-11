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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
fun FoodResultMultiScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMealConfirm: () -> Unit,
    sessionManager: LoggingSessionManager
) {
    val draftState by sessionManager.state.collectAsState()
    val candidateFoods = draftState.candidateFoods

    val selectedFoods = candidateFoods.filter { it.isSelected }
    val totalCalories = selectedFoods.sumOf { it.calories }
    val totalProtein = selectedFoods.sumOf { it.macros.protein }.toInt()
    val totalCarbs = selectedFoods.sumOf { it.macros.carbs }.toInt()
    val totalFat = selectedFoods.sumOf { it.macros.fat }.toInt()

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
                    text = "Multiple Foods Detected",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "${candidateFoods.size} items identified by AI",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food Items Checklist
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(candidateFoods) { index, food ->
                MultiFoodItemCard(
                    food = food,
                    onToggle = { sessionManager.toggleCandidateSelection(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Macro Totals Bar Card
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
                    Text(
                        text = "Total Selected",
                        style = MaterialTheme.typography.labelMedium.copy(color = LuminaOnSurfaceVariant)
                    )
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroSummaryPill("P", "${totalProtein}g", LuminaPrimary)
                    MacroSummaryPill("C", "${totalCarbs}g", Color(0xFF60A5FA))
                    MacroSummaryPill("F", "${totalFat}g", Color(0xFFFBBF24))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log Selected CTA
        Button(
            onClick = onNavigateToMealConfirm,
            enabled = selectedFoods.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuminaPrimary,
                disabledContainerColor = LuminaPrimary.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = "Log ${selectedFoods.size} Selected Items ✨",
                color = LuminaOnPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MultiFoodItemCard(
    food: AIFoodItem,
    onToggle: () -> Unit
) {
    val (badgeBg, badgeText, badgeLabel) = when (food.confidenceLevel) {
        ConfidenceLevel.HIGH -> Triple(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981), "✓ High (${(food.confidence * 100).toInt()}%)")
        ConfidenceLevel.MEDIUM -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "⚠️ Confirm (${(food.confidence * 100).toInt()}%)")
        ConfidenceLevel.LOW -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444), "❓ Low (${(food.confidence * 100).toInt()}%)")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .border(
                1.dp,
                if (food.isSelected) LuminaPrimary.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = LuminaSurfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = food.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = LuminaPrimary,
                    uncheckedColor = LuminaOnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "${food.calories} kcal  •  ${food.estimatedGrams.toInt()}g",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg
            ) {
                Text(
                    text = badgeLabel,
                    color = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MacroSummaryPill(label: String, value: String, color: Color) {
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
