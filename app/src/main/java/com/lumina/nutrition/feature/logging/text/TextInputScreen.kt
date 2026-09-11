package com.lumina.nutrition.feature.logging.text

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextInputScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToManualSearch: () -> Unit,
    viewModel: TextInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val examples = listOf(
        "2 eggs, whole wheat toast with butter",
        "Grilled salmon, brown rice, broccoli",
        "Oatmeal with berries and protein powder",
        "Chipotle chicken burrito bowl with guac"
    )

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
                    text = "Describe Meal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Natural language AI nutrient extraction",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input Field
        OutlinedTextField(
            value = uiState.queryText,
            onValueChange = viewModel::onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = {
                Text(
                    text = "e.g. \"I had a double espresso, 2 boiled eggs, and a small banana with peanut butter...\"",
                    color = LuminaOnSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LuminaPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
                focusedContainerColor = LuminaSurfaceContainerLowest,
                unfocusedContainerColor = LuminaSurfaceContainerLowest
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Example Prompt Chips
        Text(
            text = "Try an example:",
            style = MaterialTheme.typography.labelMedium.copy(
                color = LuminaOnSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            examples.forEach { example ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.selectExample(example) },
                    shape = RoundedCornerShape(20.dp),
                    color = LuminaSurfaceContainerHigh
                ) {
                    Text(
                        text = example,
                        color = LuminaOnSurface,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Offline / Network Error State Banner
        if (uiState.isNetworkError) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = LuminaError.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AI logging needs a connection",
                        color = LuminaError,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unable to reach the AI server. You can try manual food search instead.",
                        color = LuminaOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToManualSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Search Manual Database", color = LuminaOnPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Analyze Button
        Button(
            onClick = { viewModel.analyzeMeal(onNavigateToReview) },
            enabled = uiState.queryText.isNotBlank() && !uiState.isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuminaPrimary,
                disabledContainerColor = LuminaPrimary.copy(alpha = 0.4f)
            )
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    color = LuminaOnPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing Meal with AI...", color = LuminaOnPrimary, fontWeight = FontWeight.Bold)
            } else {
                Text("Analyze Meal ✨", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
