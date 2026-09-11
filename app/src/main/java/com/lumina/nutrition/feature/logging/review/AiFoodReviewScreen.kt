package com.lumina.nutrition.feature.logging.review

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
fun AiFoodReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToConfirm: () -> Unit,
    sessionManager: LoggingSessionManager
) {
    val draftState by sessionManager.state.collectAsState()
    val candidateFoods = draftState.candidateFoods

    val totalCalories = candidateFoods.sumOf { it.calories }
    val totalProtein = candidateFoods.sumOf { it.macros.protein }.toInt()
    val totalCarbs = candidateFoods.sumOf { it.macros.carbs }.toInt()
    val totalFat = candidateFoods.sumOf { it.macros.fat }.toInt()

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
                    text = "AI Extracted Foods",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Itemized breakdown from description",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaOnSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Raw input query banner if present
        if (!draftState.pendingTranscript.isNullOrBlank() || !draftState.prefilledSearchQuery.isNullOrBlank()) {
            val raw = draftState.pendingTranscript ?: draftState.prefilledSearchQuery ?: ""
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = LuminaSurfaceContainerHigh
            ) {
                Text(
                    text = "“$raw”",
                    color = LuminaOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Items list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(candidateFoods) { index, food ->
                ReviewFoodCard(food = food)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Totals Card
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
                    Text(text = "Total Nutrition", color = LuminaOnSurfaceVariant, fontSize = 12.sp)
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroChip("P", "${totalProtein}g", LuminaPrimary)
                    MacroChip("C", "${totalCarbs}g", Color(0xFF60A5FA))
                    MacroChip("F", "${totalFat}g", Color(0xFFFBBF24))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Proceed to Log Button
        Button(
            onClick = onNavigateToConfirm,
            enabled = candidateFoods.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary)
        ) {
            Text("Continue to Meal Confirm ›", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ReviewFoodCard(food: AIFoodItem) {
    val (badgeBg, badgeText, badgeLabel) = when (food.confidenceLevel) {
        ConfidenceLevel.HIGH -> Triple(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981), "✓ ${(food.confidence * 100).toInt()}%")
        ConfidenceLevel.MEDIUM -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "⚠️ ${(food.confidence * 100).toInt()}%")
        ConfidenceLevel.LOW -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444), "❓ ${(food.confidence * 100).toInt()}%")
    }

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
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${food.calories} kcal  •  ${food.estimatedGrams.toInt()}g (P: ${food.macros.protein.toInt()}g, C: ${food.macros.carbs.toInt()}g, F: ${food.macros.fat.toInt()}g)",
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
private fun MacroChip(label: String, value: String, color: Color) {
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
