package com.lumina.nutrition.feature.logging.photo

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
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.domain.model.AIAnalysisResult

@Composable
fun PhotoReviewScreen(
    onNavigateBack: () -> Unit,
    onAnalysisComplete: (AIAnalysisResult) -> Unit,
    onNavigateToManualSearch: () -> Unit,
    viewModel: PhotoReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminaBackground)
            .statusBarsPadding()
            .padding(20.dp)
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

            Text(
                text = "Review Meal Photo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Image Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(LuminaSurfaceContainerLowest)
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🥗",
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Meal Image Captured",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ready to identify ingredients and extract calories & macros with Vision AI.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Offline / Error Banner
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
                        text = "Unable to reach the AI server. Try manual search instead.",
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

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LuminaSurfaceContainerHigh)
            ) {
                Text("Retake", color = LuminaOnSurface, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { viewModel.analyzePhoto(onAnalysisComplete) },
                enabled = !uiState.isAnalyzing,
                modifier = Modifier
                    .weight(2f)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing...", color = LuminaOnPrimary, fontWeight = FontWeight.Bold)
                } else {
                    Text("Analyze Photo ✨", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
