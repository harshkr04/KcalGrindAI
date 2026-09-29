package com.kcalgrindai.app.feature.logging.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalgrindai.app.domain.model.AIFoodItem
import com.kcalgrindai.app.domain.model.ConfidenceLevel
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager

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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.kcalgrindai.app.core.designsystem.components.KcalGrindBackButton(
                onClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "AI Extracted Foods",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Itemized breakdown from description",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "“$raw”",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Nutrition", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroChip("P", "${totalProtein}g", MaterialTheme.colorScheme.primary)
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Continue to Meal Confirm ›", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ReviewFoodCard(food: AIFoodItem) {
    val (badgeBg, badgeText, badgeLabel) = when (food.confidenceLevel) {
        ConfidenceLevel.HIGH -> Triple(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981), "${(food.confidence * 100).toInt()}% match")
        ConfidenceLevel.MEDIUM -> Triple(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B), "${(food.confidence * 100).toInt()}% match")
        ConfidenceLevel.LOW -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444), "${(food.confidence * 100).toInt()}% match")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${food.calories} kcal  •  ${food.estimatedGrams.toInt()}g (P: ${food.macros.protein.toInt()}g, C: ${food.macros.carbs.toInt()}g, F: ${food.macros.fat.toInt()}g)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(text = value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
        }
    }
}
